package com.wy.display.config;

import com.wy.Main;
import com.wy.display.config.creatData.CreateData;
import com.wy.display.config.readData.ScalaAnalyse;
import com.wy.display.config.readXML.ReadConfig;
import com.wy.display.statistics.StatisticsController;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcDetector;
import com.wy.display.detector.DetectorPaintController;
import com.wy.model.decetor.Tracker;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.wy.model.data.DataSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dgbtds
 */
public class ConfigController {
    public static Color[] colors = {
            Color.WHITE,
             Color.BEIGE,Color.BISQUE,Color.PINK, Color.CYAN,
            Color.ORANGE ,Color.BLUEVIOLET,Color.RED, Color.YELLOW,
            Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE
    };
    @FXML
    private TextField triggerCount;

    @FXML
    private TextField channelNum;
    @FXML
    private TextField trackerNum;
    @FXML
    private TextField planeNum;
    @FXML
    private TextField triggerNum;
    @FXML
    private Button image;
    @FXML
    private ProgressBar fileProgressBar;
    @FXML
    private TextArea ConfigLog;
    private  DataSource dataSource;
    private static LtpcDetector ltpcDetector;

    private boolean isConfiged=false;
    @FXML
    private void initialize(){
        ConfigLog.setFont(Font.font(20));
        fileProgressBar.setProgress(0);
        Tooltip triggerTip = new Tooltip("填写触发号,必填");
        Tooltip planeTip = new Tooltip("填写平面号，必填");
        Tooltip trackerTip = new Tooltip("填写路径号，可不选");
        Tooltip channelTip = new Tooltip("填写通道号，可不填");
        Tooltip imageTip = new Tooltip("打开参考图片");
        triggerNum.setTooltip(triggerTip);
        trackerNum.setTooltip(trackerTip);
        planeNum.setTooltip(planeTip);
        channelNum.setTooltip(channelTip);
        image.setTooltip(imageTip);

        setIntegerFormatter(triggerNum);
        setIntegerFormatter(trackerNum);
        setIntegerFormatter(planeNum);
        setIntegerFormatter(channelNum);

        Tooltip triggerC = new Tooltip("模拟数据触发数");
        triggerCount.setTooltip(triggerC);
        setIntegerFormatter(triggerCount);
    }
    @FXML
    public void creatDataAction(){
        if (isConfiged) {
            String text = triggerCount.getText();
            if (!"".equals(text)) {
                FileChooser fileChooser = FileChooseBuild();
                fileChooser.setTitle("Save File");
                File file = fileChooser.showSaveDialog(new Stage());

                fileProgressBar.progressProperty().unbind();
                fileProgressBar.setProgress(0);

                if (file!=null) {
                    CreateData createData = new CreateData(file, Integer.parseInt(text));

                    //bind bar
                    fileProgressBar.progressProperty().bind(createData.progressProperty());

                    ConfigLog.appendText("\nData Create Start：Path:"+file.getAbsolutePath());

                    createData.start();
                    createData.setOnSucceeded(event -> {
                        ConfigLog.appendText("\nData Create Succeed。Path:"+file.getAbsolutePath());
                    });
                }
                else {
                    ConfigLog.appendText("\nNot choose fileSavePath");
                }
            }
            else {
                ConfigLog.appendText("\nTrigger Cannot be null");
            }
        }
        else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.appendText("\nPlease Import ConfigFile");
        }
    }
    private void setIntegerFormatter(TextField t){
        TextFormatter<Integer> integerTextFormatter = new TextFormatter<>(
                change -> {
                    String text = change.getControlNewText();
                    if (text.matches("\\d*\\.?\\d*")) {
                        return change;
                    } else {
                        return null;
                    }
                }
        );
        t.setTextFormatter(integerTextFormatter);
    }
    @FXML
    private void controlButtonAction() throws Exception {
        FileChooser fileChooser = FileChooseBuild();
        fileChooser.setTitle("Please Choose ConfigFile");
        File file = fileChooser.showOpenDialog(new Stage());
//        File file =new File(Main.class.getResource("/detector.xlsx").getFile());
        if (file!=null) {
            ConfigLog.setStyle("-fx-text-fill:green");
            ConfigLog.setText("---------Initial Detector Info--------- \n Path: "+file.getAbsolutePath()+"\n\n");
            ReadConfig.setDetectorByXlxs(file);
            ltpcDetector = ReadConfig.getLtpcDetector();
//            String s = ltpcDetector.toString();
//            ConfigLog.appendText(s);

            Main.showDetector();
            isConfiged=true;
        }
        else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.setText("\nPlease Import ConfigFile");
        }
    }
    @FXML
    private void imageAction(){
       VBox vBox = new VBox();
        ImageView imageView = new ImageView(Main.class.getResource("/DetectorInfo.png").toExternalForm());
        String s = Tracker.getInfo();
        TextArea textArea = new TextArea(s);
        textArea.setFont(Font.font(20));
        textArea.setStyle("-fx-border-color: red");
        textArea.setStyle("-fx-text-fill: blueviolet");
        textArea.setPrefWidth(vBox.getWidth());
        textArea.setWrapText(true);
        vBox.getChildren().addAll(textArea,imageView);

        Stage stage = new Stage();
        stage.setTitle("Route Info");
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setScene(new Scene(vBox));
        stage.show();
    }
    @FXML
    private void FileOpenAction() throws IOException {
        if (isConfiged) {
            FileChooser fileChooser = FileChooseBuild();
            File file = fileChooser.showOpenDialog(new Stage());
            if (file==null){
                ConfigLog.setStyle("-fx-text-fill:red");
                ConfigLog.appendText("\n\nNot choose file");
            }
            else {
                ConfigLog.appendText("\n\n##################Start Analyse##################\n");
                ConfigLog.setStyle("-fx-text-fill:mediumvioletred");
                ConfigLog.appendText("Path:"+file.getAbsolutePath());
                long start = System.currentTimeMillis();

                fileProgressBar.progressProperty().unbind();
                fileProgressBar.setProgress(0);

                ScalaAnalyse scalaAnalyse = new ScalaAnalyse(file.getAbsolutePath(),"",fileProgressBar,ConfigLog);
                scalaAnalyse.start();

                scalaAnalyse.setOnSucceeded(event -> {
                    this.dataSource = scalaAnalyse.getValue();
                    if (this.dataSource !=null){
                        long end = System.currentTimeMillis();
                        long UseSeconds = (end - start) / 1000;
                        ConfigLog.appendText("\nData Analyse Over,Use Time: "+UseSeconds+" s");
                        ConfigLog.appendText("\n##################End Analyse##################\n");
                        StatisticsController.setDataSource(this.dataSource);
                        try {
                            Main.showFileResult();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.appendText("\n请先配置参数文件");
        }
    }
    public static FileChooser FileChooseBuild(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有类型","*.*"),
                new FileChooser.ExtensionFilter("Excel类型","*.xlsx"),
                new FileChooser.ExtensionFilter("数据类型","*.bin")
        );
        File file = new File("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp");
        if (file.exists()){
            fileChooser.setInitialDirectory(file);
        }
        return fileChooser;
    }
    @FXML
    private void ColorButtonAction(){

        VBox ColorBlock = new VBox();
        ColorBlock.setPrefHeight(560);
        ColorBlock.setPrefWidth(120);
        ColorBlock.setBorder(new Border(
                new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID,new CornerRadii(10), BorderWidths.DEFAULT)
        ));

        double wdith = ColorBlock.getPrefWidth();
        double height = ColorBlock.getPrefHeight();
        int count = colors.length + 1;
        for(int i=0;i<colors.length;i++){
            Rectangle rectangle = new Rectangle(0, (double) height  / (double)count, wdith, (double) height  / (double)count);
            rectangle.setFill(colors[colors.length-1-i]);
            rectangle.setStyle("-fx-stroke-type:outside");
            ColorBlock.getChildren().add(rectangle);
        }
        Label label = new Label("颜色标尺");
        label.setPrefWidth(wdith);
        label.setFont(Font.font(20));
        label.setPrefHeight((double)height/(double)count);
        ColorBlock.getChildren().add(label);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("  颜色标尺 ");
        stage.setScene(new Scene(ColorBlock));
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.show();
    }
    @FXML
    private void fillButtonAction() {

        String triggerS = triggerNum.getText();
        String planeS = planeNum.getText();
        String trackerS = trackerNum.getText();
        String channelS = channelNum.getText();
        Integer trigger = null, plane = null, tracker = null, channel=null;
        ConfigLog.setStyle("-fx-text-fill:red");
        try {
            if (!"".equals(triggerS)) {
                trigger = Integer.valueOf(triggerS);
            } else {
                ConfigLog.appendText("\n触发序号必须填写");
                return;
            }

            if (!("".equals(planeS)&&"".equals(trackerS)) ) {
                if (!"".equals(planeS)) {
                    plane = Integer.valueOf(planeS);
                } else {
                    plane = null;
                }

                if (!"".equals(trackerS)) {
                    tracker = Integer.valueOf(trackerS);
                } else {
                    tracker = null;
                }
            } else {
                ConfigLog.appendText("\n平面序号或者径迹序号必须填写一个");
                return;
            }

            if (!"".equals(channelS)) {
                channel = Integer.valueOf(channelS);
            } else {
                channel = null;
            }

        } catch (NumberFormatException e) {
            ConfigLog.appendText(e.getMessage());
        }
        if (dataSource==null){
            ConfigLog.appendText("\n没有数据源");
            return;
        }
        DetectorPaintController.setTriggerNum(trigger);
        DetectorPaintController.setDataSource(dataSource);
        List<SimplifyData> sdList =dataSource.getSdList();
        Integer finalTrigger = trigger;
        Integer finalPlane = plane;
        Stream<SimplifyData> simplifyDataStream = sdList.stream()
                .filter(s -> s.getTriggerNum() == finalTrigger).filter(s -> s.getTriggerNum() == finalTrigger);
        if (tracker!=null){
            Integer finalTracker = tracker;
            Stream<SimplifyData> streamTracker = simplifyDataStream.filter(s -> s.getTrackerNum() == finalTracker);
            if (channel!=null){
                Integer finalChannel = channel;
                ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) streamTracker.filter(s -> s.getTrackerNum() == finalChannel).collect(Collectors.toList());
                DetectorPaintController.fillRect(collect);
                ConfigLog.appendText("\n填充 触发号:"+planeS+" 径迹"+trackerS+" 通道"+channelS+" 成功！");
            }
            else {
                ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) streamTracker.collect(Collectors.toList());
                DetectorPaintController.fillRect(collect);
                ConfigLog.appendText("\n填充 触发号:"+planeS+" 径迹"+trackerS+" 成功！");
            }
        }
        else {
            Stream<SimplifyData> streamPlane=simplifyDataStream.filter(s -> s.getPlaneNum() == finalPlane);
            ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) streamPlane.collect(Collectors.toList());
            DetectorPaintController.fillRect(collect);
            ConfigLog.appendText("\n填充 触发号:"+triggerS+"平面:"+planeS+"成功！");
        }
    }

    public static LtpcDetector getLtpcDetector() {
        return ltpcDetector;
    }

    public static void setLtpcDetector(LtpcDetector ltpcDetector) {
        ConfigController.ltpcDetector = ltpcDetector;
    }
}
