package com.wy.display.detector;
/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/5 12:34
 */

import com.wy.display.config.ConfigController;
import com.wy.model.data.DataSource;
import com.wy.model.data.Rectangle;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcChannel;
import com.wy.model.decetor.LtpcDetector;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wy.display.config.ConfigController.colors;


/**
 * @program: LTPC2020-3-4-version2
 * @description:
 * @author: WuYe
 * @create: 2020-03-05 12:34
 **/
public class DetectorPaintController {
    private static DataSource dataSource;
    private static int triggerNum;
    private double scalingFactor = 2;
    //以下两个参数描述拖拽的位置
    private double dragX = 0;
    private double dragY = 0;
    @FXML
    private VBox ColorBlocks;
    @FXML
    private AnchorPane detectorPane;
    @FXML
    private AnchorPane root;

    @FXML
    private void initialize() throws Exception {
        List<LtpcChannel> channels = LtpcDetector.channels;
        setDetectorPane(channels);
    }

    public static void main(String[] args) {

    }

    //添加坐标轴和探测器模型
    private void addComponent(List<LtpcChannel> channels) {

        detectorPane.getChildren().clear();
        //坐标轴设置
        NumberAxis axisX = new NumberAxis(0, 100, 100);
        axisX.setTickLabelsVisible(false);
        axisX.setSide(Side.TOP);
        axisX.setPrefHeight(0);
        axisX.setPrefWidth(detectorPane.getPrefWidth());
        axisX.setUpperBound(detectorPane.getPrefWidth() / 2 - dragX);
        axisX.setLowerBound(-detectorPane.getPrefWidth() / 2 - dragX);
        axisX.setLayoutY(detectorPane.getPrefHeight() / 2 + dragY);

        NumberAxis axisY = new NumberAxis(0, 100, 100);
        axisY.setTickLabelsVisible(false);
        axisY.setSide(Side.LEFT);
        axisY.setPrefWidth(0);
        axisY.setPrefHeight(detectorPane.getPrefHeight());
        axisY.setUpperBound(detectorPane.getPrefHeight() / 2 - dragY);
        axisY.setLowerBound(-detectorPane.getPrefHeight() / 2 - dragY);
        axisY.setLayoutX(detectorPane.getPrefWidth() / 2 + dragX);

        detectorPane.getChildren().addAll(axisX, axisY);

        Affine affine = new Affine(1, 0, detectorPane.getPrefWidth() / 2 + dragX, 0, -1, detectorPane.getPrefHeight() / 2 + dragY);
        channels.forEach(c -> {
            double x_center = c.getX_center() * scalingFactor;
            double y_center = c.getY_center() * scalingFactor;
            double wdith = c.getWdith() * scalingFactor;
            double heigh = c.getHeigh() * scalingFactor;
            int slope = c.getSlope();
            Rectangle rectangle = new Rectangle(x_center - wdith / 2, y_center - heigh / 2, wdith, heigh);
            rectangle.getTransforms().add(affine);

            Rotate rotate = new Rotate();
            rotate.setAngle(-slope);
            rotate.setPivotX(x_center);
            rotate.setPivotY(y_center);
            rectangle.getTransforms().add(rotate);

            rectangle.setStrokeWidth(0.3);
            rectangle.setStyle("-fx-stroke-type:outside");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sourceBoard: ").append(c.getSourceBoardNum()).append(", ChannelNum: ").append(c.getChannelId()).append(" ,trackNums: ").append(Arrays.toString(c.getTrackNums()));
            Tooltip id = new Tooltip(stringBuilder.toString());
            Tooltip.install(rectangle, id);
            c.setRectangle(rectangle);
            detectorPane.getChildren().add(rectangle);
            if (c.getSourceBoardNum() != 0) {
                if (c.getColor() < 0) {
                    rectangle.setFill(Color.WHITE);
                } else {
                    rectangle.setFill(colors[c.getColor()]);
                }
                rectangle.setStroke(Color.BLACK);
            }
            if (c.getSourceBoardNum() == 0) {
                rectangle.setFill(Color.GRAY);
            }
        });
    }

    private LineChart<Number, Number> setWaveChart(String points, int splitStart, int tracker, String color) {
        String[] pointsInt = points.trim().split(";");
        NumberAxis X = new NumberAxis(splitStart-5, splitStart+pointsInt.length+5, 1);
        X.setLabel("Time/25ns");
        NumberAxis Y = new NumberAxis();
        Y.setLabel("charge");
        LineChart<Number, Number> wave = new LineChart<Number, Number>(X, Y);
        wave.setStyle("CHART_COLOR_1: " + color + " ;");

        XYChart.Series<Number, Number> waveData = new XYChart.Series<>();
        waveData.setName("startIndex:" + splitStart + " ,trackId:" + tracker);


        for (int i = 0; i < pointsInt.length; i++) {
            waveData.getData().add(new XYChart.Data<>(splitStart + i, Integer.valueOf(pointsInt[i])));
        }
        wave.getData().add(waveData);
        return wave;
    }

    private void setDetectorPane(List<LtpcChannel> channels) {
        //平移变换
        AtomicInteger startX = new AtomicInteger(0);
        AtomicInteger startY = new AtomicInteger(0);
        AtomicBoolean mouseDragStart = new AtomicBoolean(false);

        detectorPane.setOnMousePressed(e -> {
            startX.set((int) e.getX());
            startY.set((int) e.getY());
            mouseDragStart.set(true);
//            System.out.println("press x= "+startX.get()+" y= "+startY.get());
        });
        detectorPane.setOnMouseDragged(e -> {
            if (mouseDragStart.get()) {
//                System.out.println(" Startx= "+startX+"  StartY= "+startY);
                dragX += e.getX() - startX.get();
                dragY += e.getY() - startY.get();
                startX.set((int) e.getX());
                startY.set((int) e.getY());
//                System.out.println("!!!!!!!!------------------------>drag x= "+dragX+" dragY= "+dragY);
                addComponent(channels);
            }
        });
        detectorPane.setOnMouseReleased(event -> {
            startX.set(0);
            startY.set(0);
//            System.out.println("release");
            mouseDragStart.set(false);
        });

        detectorPane.setOnScroll(event -> {
            //缩放因子
            double scaleX = detectorPane.getScaleX();
            double scaleY = detectorPane.getScaleY();
            if (event.getDeltaY() > 0) {
                detectorPane.setScaleX(scaleX + 0.5);
                detectorPane.setScaleY(scaleY + 0.5);
                addComponent(channels);
            } else {
                if (scaleX > 1 && scaleY > 0.5) {
                    detectorPane.setScaleX(scaleX - 0.5);
                    detectorPane.setScaleY(scaleY - 0.5);
                    addComponent(channels);
                }
            }
        });

        root.widthProperty().addListener((observer, oldValue, newValue) -> {
            detectorPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(0), BorderWidths.DEFAULT)));
            detectorPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), new Insets(0))));
            //画板父组件设置
            if (oldValue.doubleValue() != 0) {
                detectorPane.getChildren().clear();
                detectorPane.setPrefWidth(newValue.doubleValue() - oldValue.doubleValue() + detectorPane.getPrefWidth());
                addComponent(channels);
            }
        });
        root.heightProperty().addListener((observer, oldValue, newValue) -> {
            if (oldValue.doubleValue() != 0) {
                detectorPane.getChildren().clear();
                detectorPane.setPrefHeight(newValue.doubleValue() - oldValue.doubleValue() + detectorPane.getPrefHeight());
                addComponent(channels);
            }
        });
        detectorPane.setOnMouseClicked(e -> {
            double x = e.getX() - detectorPane.getPrefWidth() * 0.5 - dragX;
            double y = detectorPane.getPrefHeight() * 0.5 - e.getY() + dragY;
            System.out.println("click: x:" + x + " , y:" + y);
            LtpcDetector.sourceBoardChannelIdChannelMap.values().forEach(c -> {
                if (c.getRectangle().contains(x, y)) {
                    System.out.println(c.toString() + "contains");
                    Dataset<Row> dr = ConfigController.ds.select("splitstart", "points", "tracker").where("trigger=" + ConfigController.trigger + " and board=" + c.getSourceBoardNum() + " and channelId=" + c.getChannelId());
                    List<Row> rows = dr.collectAsList();

                    VBox vBox = new VBox();
                    vBox.setSpacing(20);
                    rows.forEach(row -> {
                        LineChart<Number, Number> waveChart = setWaveChart(row.getString(1), row.getInt(0), row.getInt(2), c.getRectangle().getFill().toString().substring(2, 8));
                        vBox.getChildren().add(waveChart);
                    });

                    Stage stage = new Stage();
                    stage.setScene(new Scene(vBox));
                    stage.setTitle(" 触发号: " + triggerNum + "源板号" + c.getSourceBoardNum() + " ,通道: " + c.getChannelId() + " 的采样数据波形图");
                    stage.show();
                }
            });
//                rectangleMap.forEach((k, v) -> {
//                    if (v.contains(x, y)) {
//                        Optional<SimplifyData> middle = dataSource.getSdList().stream().filter(l -> l.getPID() == k).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//                        middle.ifPresent(simplifyData -> {
//                            SimplifyData sd = middle.get();
//                            Optional<SimplifyData> right = dataSource.getSdList().stream()
//                                    .filter(l -> l.getPID() == k - 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//                            Optional<SimplifyData> left = dataSource.getSdList().stream()
//                                    .filter(l -> l.getPID() == k + 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//                            Paint fill = v.getFill();
//                            String middleColor = fill.toString();
//                            Rectangle rectR = rectangleMap.get(k - 1);
//                            String rightColor = rectR.getFill().toString();
//                            Rectangle rectL = rectangleMap.get(k + 1);
//                            String leftColor = rectL.getFill().toString();
//
//
//                            VBox vBox = new VBox();
//                            vBox.setSpacing(20);
//
//                            LineChart<Number, Number> waveChart1 = setWaveChart(middle, "#" + middleColor.substring(2, 8));
//                            if (channels.get(k - 1).getCol() == sd.getLtpcChannel().getCol()) {
//                                LineChart<Number, Number> waveChart0 = setWaveChart(right, "#" + rightColor.substring(2, 8));
//                                vBox.getChildren().add(waveChart0);
//                            }
//
//                            vBox.getChildren().add(waveChart1);
//
//                            if (channels.get(k + 1).getCol() == sd.getLtpcChannel().getCol()) {
//                                LineChart<Number, Number> waveChart2 = setWaveChart(left, "#" + leftColor.substring(2, 8));
//                                vBox.getChildren().add(waveChart2);
//                            }
//
//                            Stage stage = new Stage();
//                            stage.setScene(new Scene(vBox));
//                            stage.setTitle(" 触发号: " + triggerNum + " ,通道: " + middle.get().getPID() + " 的采样数据波形图");
//                            stage.show();
//                        });
//
//                    }
//                });
        });
    }

    public static void fillRect(ArrayList<SimplifyData> spfd) {
        fillReset();
        int chargeMax = 2000;
        int chargeMin = 0;
        Color[] colors = ConfigController.colors;
        int piece = (chargeMax - chargeMin) / colors.length;
        spfd.forEach(
                simplifyData -> {
                    LtpcChannel ltpcChannel = simplifyData.getLtpcChannel();
                    int charge = simplifyData.getCharge();
                    int index = (charge - chargeMin) % piece;
                    if (index > colors.length - 1) {
                        index = colors.length - 1;
                    }
                    Tuple2<Integer, Integer> integerIntegerTuple2 = new Tuple2<>(ltpcChannel.getSourceBoardNum(), ltpcChannel.getChannelId());
                    if (LtpcDetector.sourceBoardChannelIdChannelMap.containsKey(integerIntegerTuple2)) {
                        LtpcDetector.sourceBoardChannelIdChannelMap.get(integerIntegerTuple2).setColor(index);
                    }
                }
        );
    }

    public static void fillRectOPT(ResultSet resultSet, TextArea ConfigLog) throws SQLException {
        fillReset();
        int chargeMax = 2000;
        int chargeMin = 0;
        Color[] colors = ConfigController.colors;
        int piece = (chargeMax - chargeMin) / colors.length;
        int i = 0;
        ConfigLog.appendText("\n sourceBoard , channelId , MaxPoint");
        while (resultSet.next()) {
            i++;
            int sourceBoard = resultSet.getInt(1);
            int channelId = resultSet.getInt(2);
            int charge = resultSet.getInt(3);
            int index = (charge - chargeMin) % piece;
            ConfigLog.appendText(String.format("\n %d , %d , %d", sourceBoard, channelId, charge));
            if (index > colors.length - 1) {
                index = colors.length - 1;
            }
            if (LtpcDetector.sourceBoardChannelIdChannelMap.containsKey(sourceBoard + "," + channelId)) {
                LtpcDetector.sourceBoardChannelIdChannelMap.get(sourceBoard + "," + channelId).setColor(index);
                LtpcDetector.sourceBoardChannelIdChannelMap.get(sourceBoard + "," + channelId).getRectangle().setFill(colors[index]);
            }
        }
        ConfigLog.appendText("\n Hive sql compeleted！Result :" + i);
    }

    public static void fillRectOPT(List<Row> rows, TextArea ConfigLog) throws SQLException {
        fillReset();
        Integer chargeMax = rows.stream().map(row -> row.getInt(2)).max(Integer::compareTo).get();
        Integer chargeMin = rows.stream().map(row -> row.getInt(2)).min(Integer::compareTo).get();
        ConfigController.chargMax = chargeMax;
        ConfigController.chargMin = chargeMin;
        ConfigLog.appendText("\n chargeMax :" + chargeMax + " chargeMin :" + chargeMin);
        Color[] colors = ConfigController.colors;
        int piece = (chargeMax - chargeMin) / colors.length;
        ConfigLog.appendText("\n sourceBoard , channelId , MaxPoint");
        rows.forEach(row -> {
            int sourceBoard = row.getInt(0);
            int channelId = row.getInt(1);
            int charge = row.getInt(2);
            int index = (charge - chargeMin) / piece;
            ConfigLog.appendText(String.format("\n %d , %d , %d", sourceBoard, channelId, charge));
            if (index > colors.length - 1) {
                index = colors.length - 1;
            }
            if (LtpcDetector.sourceBoardChannelIdChannelMap.containsKey(sourceBoard + "," + channelId)) {
                LtpcDetector.sourceBoardChannelIdChannelMap.get(sourceBoard + "," + channelId).setColor(index);
                LtpcDetector.sourceBoardChannelIdChannelMap.get(sourceBoard + "," + channelId).getRectangle().setFill(colors[index]);
            }
        });
    }

    private static void fillReset() {
        LtpcDetector.sourceBoardChannelIdChannelMap.forEach((k, v) -> {
            if (v.getSourceBoardNum() != 0) {
                v.setColor(-1);
                v.getRectangle().setFill(Color.WHITE);
            }
        });
    }


    public static void setDataSource(DataSource dataSource) {
        DetectorPaintController.dataSource = dataSource;
    }


    public static void setTriggerNum(int triggerNum) {
        DetectorPaintController.triggerNum = triggerNum;
    }
}

