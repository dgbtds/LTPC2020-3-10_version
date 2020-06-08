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
import com.wy.model.decetor.PlaneWithTrack;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

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
    private static HashMap<Integer, Rectangle> rectangleMap = new HashMap<>();
    //以下两个参数描述拖拽的位置
    private double dragX = 0;
    private double dragY = 0;
    private static boolean isFilled = false;
    @FXML
    private VBox ColorBlocks;
    @FXML
    private AnchorPane detectorPane;
    @FXML
    private AnchorPane root;

    @FXML
    private void initialize() throws Exception {
        List<LtpcChannel> channels = ConfigController.getLtpcDetector().getChannels();
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
            stringBuilder.append("PID: ").append(c.getPid()).append(", ChannelNum: ").append(c.getPid()).append(" ,Board: ").append(c.getSourceBoardNum()).append(" ,Tracker: ");
            PlaneWithTrack[] planeWithTracks = c.getPlaneWithTracks();
            for (PlaneWithTrack pwt : planeWithTracks) {
                stringBuilder.append(pwt.getTracker().trackerNum).append(" ");
            }
            Tooltip id = new Tooltip(stringBuilder.toString());
            Tooltip.install(rectangle, id);
            c.setRectangle(rectangle);
            detectorPane.getChildren().add(rectangle);
            if (c.getSourceBoardNum() != 0) {
                rectangleMap.put(c.getPid(), rectangle);
                rectangle.setFill(colors[c.getColor()]);
                rectangle.setStroke(Color.BLACK);
            }
            if (c.getSourceBoardNum() == 0) {
                rectangle.setFill(Color.GRAY);
            }
        });
//        if (dataSource != null) {
//            Optional<SimplifyData> middle = dataSource.getSdList().stream().filter(l -> l.getChannelNum() == c.getPid()).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//            middle.ifPresent(simplifyData -> {
//                SimplifyData sd = middle.get();
//                Optional<SimplifyData> right = dataSource.getSdList().stream()
//                        .filter(l -> l.getChannelNum() == c.getPid() - 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//                Optional<SimplifyData> left = dataSource.getSdList().stream()
//                        .filter(l -> l.getChannelNum() == c.getPid() + 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
//                rectangle.setOnMouseClicked(event -> {
//                    Paint fill = rectangle.getFill();
//                    String middleColor = fill.toString();
//                    Rectangle rectR = rectangleMap.get(c.getPid() - 1);
//                    String rightColor = rectR.getFill().toString();
//                    Rectangle rectL = rectangleMap.get(c.getPid() + 1);
//                    String leftColor = rectL.getFill().toString();
//
//
//                    HBox hBox = new HBox();
//                    hBox.setSpacing(20);
//
//                    LineChart<Number, Number> waveChart1 = setWaveChart(middle, "#" + middleColor.substring(2, 8));
//                    if (channels.get(c.getPid() - 1).getCol() == sd.getLtpcChannel().getCol()) {
//                        LineChart<Number, Number> waveChart0 = setWaveChart(right, "#" + rightColor.substring(2, 8));
//                        hBox.getChildren().add(waveChart0);
//                    }
//
//                    hBox.getChildren().add(waveChart1);
//
//                    if (channels.get(c.getPid() + 1).getCol() == sd.getLtpcChannel().getCol()) {
//                        LineChart<Number, Number> waveChart2 = setWaveChart(left, "#" + leftColor.substring(2, 8));
//                        hBox.getChildren().add(waveChart2);
//                    }
//
//                    Stage stage = new Stage();
//                    stage.setScene(new Scene(hBox));
//                    stage.setTitle(" 触发号: " + triggerNum + " ,通道: " + middle.get().getChannelNum() + " 的采样数据波形图");
//                    stage.show();
//                });
            //点击弹出通道波形

//        }
    }

    private LineChart<Number, Number> setWaveChart(Optional<SimplifyData> first, String color) {
        NumberAxis X = new NumberAxis(0, 300, 1);
        X.setLabel("Time/25ns");
        NumberAxis Y = new NumberAxis(0, dataSource.getChargeMax(), 20);
        Y.setLabel("charge");
        LineChart<Number, Number> wave = new LineChart<Number, Number>(X, Y);
        wave.setStyle("CHART_COLOR_1: " + color + " ;");

        short[] shorts;
        XYChart.Series<Number, Number> waveData = new XYChart.Series<>();

        if (first.isPresent()) {
            SimplifyData simplifyData = first.get();
            shorts = simplifyData.getShorts();
            waveData.setName(simplifyData.getPID() + "");
        } else {
            shorts = new short[300];
            waveData.setName("No Clicked");
        }
        for (int i = 0; i < shorts.length; i++) {
            waveData.getData().add(new XYChart.Data<>(i, shorts[i]));
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
            double x=e.getX()-detectorPane.getPrefWidth()*0.5-dragX;
            double y=detectorPane.getPrefHeight()*0.5-e.getY()+dragY;
            System.out.println("click: x:"+x+" , y:"+y);
            if (dataSource!=null) {
                rectangleMap.forEach((k, v) -> {
                    if (v.contains(x, y)) {
                        Optional<SimplifyData> middle = dataSource.getSdList().stream().filter(l -> l.getPID() == k).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
                        middle.ifPresent(simplifyData -> {
                            SimplifyData sd = middle.get();
                            Optional<SimplifyData> right = dataSource.getSdList().stream()
                                    .filter(l -> l.getPID() == k - 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
                            Optional<SimplifyData> left = dataSource.getSdList().stream()
                                    .filter(l -> l.getPID() == k + 1).filter(l -> l.getTriggerNum() == triggerNum).findFirst();
                            Paint fill = v.getFill();
                            String middleColor = fill.toString();
                            Rectangle rectR = rectangleMap.get(k - 1);
                            String rightColor = rectR.getFill().toString();
                            Rectangle rectL = rectangleMap.get(k + 1);
                            String leftColor = rectL.getFill().toString();


                            HBox hBox = new HBox();
                            hBox.setSpacing(20);

                            LineChart<Number, Number> waveChart1 = setWaveChart(middle, "#" + middleColor.substring(2, 8));
                            if (channels.get(k - 1).getCol() == sd.getLtpcChannel().getCol()) {
                                LineChart<Number, Number> waveChart0 = setWaveChart(right, "#" + rightColor.substring(2, 8));
                                hBox.getChildren().add(waveChart0);
                            }

                            hBox.getChildren().add(waveChart1);

                            if (channels.get(k + 1).getCol() == sd.getLtpcChannel().getCol()) {
                                LineChart<Number, Number> waveChart2 = setWaveChart(left, "#" + leftColor.substring(2, 8));
                                hBox.getChildren().add(waveChart2);
                            }

                            Stage stage = new Stage();
                            stage.setScene(new Scene(hBox));
                            stage.setTitle(" 触发号: " + triggerNum + " ,通道: " + middle.get().getPID() + " 的采样数据波形图");
                            stage.show();
                        });

                    }
                });
            }
        });
    }

    public static void fillRect(ArrayList<SimplifyData> spfd) {
        fillReset();
        isFilled = true;
        int chargeMax = dataSource.getChargeMax();
        int chargeMin = dataSource.getChargeMin();
        Color[] colors = ConfigController.colors;
        int piece = (chargeMax - chargeMin) / colors.length;
        spfd.forEach(
                simplifyData -> {
                    LtpcChannel ltpcChannel = simplifyData.getLtpcChannel();
                    int charge = simplifyData.getCharge();
                    int index = (charge - chargeMin) % piece;
                    if (index > colors.length-1) {
                        index = colors.length-1;
                    }
                   if (LtpcDetector.getPidChannelMap().containsKey(ltpcChannel.getPid())){
                       LtpcDetector.getPidChannelMap().get(ltpcChannel.getPid()).setColor(index);
                   }
                    if (rectangleMap.containsKey(ltpcChannel.getPid())){
                        rectangleMap.get(ltpcChannel.getPid()).setFill(colors[index]) ;
                    }
                }
        );
    }

    private static void fillReset() {
        rectangleMap.forEach((k,v)->{
            v.setFill(colors[0]);
        });
        LtpcDetector.getPidChannelMap().forEach((k,v)->{
            if (v.getSourceBoardNum()!=0) {
                v.setColor(0);
            }
        });
    }



    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        DetectorPaintController.dataSource = dataSource;
    }


    public static int getTriggerNum() {
        return triggerNum;
    }

    public static void setTriggerNum(int triggerNum) {
        DetectorPaintController.triggerNum = triggerNum;
    }
}

