package com.wy.display.detector;
/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/5 12:34
 */

import com.wy.display.config.ConfigController;
import com.wy.model.data.DataSource;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcChannel;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-03-05 12:34
 **/
public class DetectorPaintController {
    private  static DataSource dataSource;
    private  static int triggerNum;
    private  static ArrayList<SimplifyData> sdList;
    private  static List<LtpcChannel> channels;
    private double scalingFactor=2;
    //以下两个参数描述拖拽的位置
    private double dragX = 0;
    private double dragY = 0;
    private static boolean isFilled=false;
    @FXML
    private VBox ColorBlocks;
    @FXML
    private AnchorPane detectorPane;
    @FXML
    private AnchorPane root;
    @FXML
    private void initialize() throws Exception {
//        ReadConfig.setDetectorByXlxs(new File("C:/javaProject/idealProject/LTPC2020-3-4-version2/src/main/resources/detector.xlsx"));
//        channels = ReadConfig.getLtpcDetector().getChannels();
        setDetectorPane();
    }

    public static void main(String[] args) {

    }

    //添加坐标轴和探测器模型
    private void addComponent(){

        detectorPane.getChildren().clear();
        //坐标轴设置
        NumberAxis axisX = new NumberAxis(0,100,100);
        axisX.setTickLabelsVisible(false);
        axisX.setSide(Side.TOP);
        axisX.setPrefHeight(0);
        axisX.setPrefWidth(detectorPane.getPrefWidth());
        axisX.setUpperBound(detectorPane.getPrefWidth()/2-dragX);
        axisX.setLowerBound(-detectorPane.getPrefWidth()/2-dragX);
        axisX.setLayoutY(detectorPane.getPrefHeight()/2+dragY);

        NumberAxis axisY = new NumberAxis(0,100,100);
        axisY.setTickLabelsVisible(false);
        axisY.setSide(Side.LEFT);
        axisY.setPrefWidth(0);
        axisY.setPrefHeight(detectorPane.getPrefHeight());
        axisY.setUpperBound(detectorPane.getPrefHeight()/2-dragY);
        axisY.setLowerBound(-detectorPane.getPrefHeight()/2-dragY);
        axisY.setLayoutX(detectorPane.getPrefWidth()/2+dragX);

        detectorPane.getChildren().addAll(axisX,axisY);

        Affine affine = new Affine(1,0,detectorPane.getPrefWidth()/2+dragX,0,-1,detectorPane.getPrefHeight()/2+dragY);
        HashMap<Integer, Rectangle> rectangleMap = new HashMap<>();
        channels.forEach(c->{
            double x_center = c.getX_center()*scalingFactor;
            double y_center = c.getY_center()*scalingFactor;
            double wdith = c.getWdith()*scalingFactor;
            double heigh = c.getHeigh()*scalingFactor;
            int slope = c.getSlope();
            Rectangle rectangle = new Rectangle(x_center - wdith / 2, y_center - heigh / 2, wdith, heigh);
            rectangle.getTransforms().add(affine);

            Rotate rotate = new Rotate();
            rotate.setAngle(-slope);
            rotate.setPivotX(x_center);
            rotate.setPivotY(y_center);
            rectangle.getTransforms().add(rotate);

            rectangle.setFill(Color.WHITE);
            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(0.3);
            rectangle.setStyle("-fx-stroke-type:outside");
            Tooltip id = new Tooltip("ChannelNum : "+c.getPid());
            Tooltip.install(rectangle,id);
            c.setRectangle(rectangle);
            detectorPane.getChildren().add(rectangle);
            rectangleMap.put(c.getPid(),rectangle);
            if (c.getSourceBoardNum()==0){
                rectangle.setFill(Color.GRAY);;
            }
            else if (dataSource!=null){
                Optional<SimplifyData> first = dataSource.getSdList().stream().filter(l -> l.getChannelNum() == c.getPid()).filter(l->l.getTriggerNum()==triggerNum).findFirst();
                if ( first.get()!=null) {
                    rectangle.setOnMouseClicked(event -> {
                        LineChart<Number, Number> waveChart = setWaveChart(first);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(waveChart));
                        stage.setTitle(" 触发号: "+triggerNum+" ,通道: "+first.get().getChannelNum()+" 的采样数据波形图");
                        stage.show();
                    });
                }
            }
        });
        if (isFilled&&dataSource!=null&&sdList!=null){
            fillRect();
        }
    }
    private LineChart<Number, Number> setWaveChart( Optional<SimplifyData> first){
        NumberAxis X = new NumberAxis(0,300,1);
        X.setLabel("Time/25ns");
        NumberAxis Y = new NumberAxis(0,dataSource.getChargeMax(),20);
        Y.setLabel("charge");
        LineChart<Number, Number> wave = new LineChart<Number, Number>(X,Y);
        SimplifyData simplifyData = first.get();
        short[] shorts = simplifyData.getShorts();
        XYChart.Series<Number, Number> waveData = new XYChart.Series<>();
        for(int i=0;i<shorts.length;i++){
            waveData.getData().add(new XYChart.Data<Number, Number>(i,shorts[i]));
        }
        wave.getData().add(waveData);
        return wave;
    }
    private void setDetectorPane(){
        //平移变换
        AtomicInteger startX=new AtomicInteger(0);
        AtomicInteger startY=new AtomicInteger(0);
        AtomicBoolean mouseDragStart= new AtomicBoolean(false);

        detectorPane.setOnMousePressed(e-> {
            startX.set((int) e.getX());
            startY.set((int) e.getY());
            mouseDragStart.set(true);
//            System.out.println("press x= "+startX.get()+" y= "+startY.get());
        });
        detectorPane.setOnMouseDragged(e -> {
            if (mouseDragStart.get()) {
//                System.out.println(" Startx= "+startX+"  StartY= "+startY);
                dragX+=e.getX()-startX.get();
                dragY+=e.getY()-startY.get();
                startX.set((int) e.getX());
                startY.set((int) e.getY());
//                System.out.println("!!!!!!!!------------------------>drag x= "+dragX+" dragY= "+dragY);
                addComponent();
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
            if (event.getDeltaY()>0){
                detectorPane.setScaleX(scaleX+0.5);
                detectorPane.setScaleY(scaleY+0.5);
                addComponent();
            }
            else {
                if (scaleX>1&&scaleY>0.5) {
                    detectorPane.setScaleX(scaleX-0.5);
                    detectorPane.setScaleY(scaleY-0.5);
                    addComponent();
                }
            }
        });

        root.widthProperty().addListener((observer,oldValue,newValue)->{
            detectorPane.setBorder(new Border(new BorderStroke(Color.RED,BorderStrokeStyle.SOLID,new CornerRadii(0),BorderWidths.DEFAULT)));
            detectorPane.setBackground(new Background(new BackgroundFill(Color.WHITE,new CornerRadii(0),new Insets(0))));
            //画板父组件设置
            if (oldValue.doubleValue()!=0) {
                detectorPane.getChildren().clear();
                detectorPane.setPrefWidth(newValue.doubleValue()-oldValue.doubleValue()+detectorPane.getPrefWidth());
                addComponent();
            }
        });
        root.heightProperty().addListener((observer,oldValue,newValue)->{
            if (oldValue.doubleValue()!=0) {
                detectorPane.getChildren().clear();
                detectorPane.setPrefHeight(newValue.doubleValue()-oldValue.doubleValue()+detectorPane.getPrefHeight());
                addComponent();
            }
        });
    }
    public  static void fillRect(){
        fillReset();
        isFilled=true;
        int chargeMax = dataSource.getChargeMax();
        int chargeMin = dataSource.getChargeMin();
        Color[] colors = ConfigController.colors;
        int piece = (chargeMax - chargeMin) / colors.length;
        sdList.forEach(
                simplifyData -> {
                    Rectangle rectangle = simplifyData.getLtpcChannel().getRectangle();
                    int charge = simplifyData.getCharge();
                    int index = (charge - chargeMin) % piece;
                    if (index>=12){
                        index=12;
                    }
                    rectangle.setFill(colors[index]);
                }
        );
    }
    public static void fillReset(){
        channels.forEach(c->{
            if (c.getSourceBoardNum()!=0){
            Rectangle rectangle = c.getRectangle();
            rectangle.setFill(Color.WHITE);
            }
        });
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        DetectorPaintController.dataSource = dataSource;
    }

    public static ArrayList<SimplifyData> getSdList() {
        return sdList;
    }

    public static void setSdList(ArrayList<SimplifyData> sdList) {
        DetectorPaintController.sdList = sdList;
    }

    public static List<LtpcChannel> getChannels() {
        return channels;
    }

    public static void setChannels(List<LtpcChannel> channels) {
        DetectorPaintController.channels = channels;
    }

    public static int getTriggerNum() {
        return triggerNum;
    }

    public static void setTriggerNum(int triggerNum) {
        DetectorPaintController.triggerNum = triggerNum;
    }
}

