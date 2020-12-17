package com.wy.display.config;

import com.wy.Main;
import com.wy.Utils.HiveUtil;
import com.wy.Utils.SSHTool;
import com.wy.display.config.creatData.CreateData;
import com.wy.display.config.readData.ScalaAnalyseSpark;
import com.wy.display.config.readData.UploadFile;
import com.wy.display.config.readXML.ReadConfig;
import com.wy.display.detector.DetectorPaintController;
import com.wy.model.data.DataSource;
import com.wy.model.decetor.LtpcDetector;
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
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import sumit.BaseJob;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dgbtds
 */
public class ConfigController {
    public static Color[] colors = {
            Color.BEIGE, Color.BISQUE, Color.PINK, Color.CYAN,
            Color.ORANGE, Color.BLUEVIOLET, Color.RED, Color.YELLOW,
            Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE
    };
    @FXML
    private TextField triggerCount;
    @FXML
    public Pane anaPane;
    @FXML
    public VBox vbox;
    @FXML
    private TextField channelId;
    @FXML
    private TextField trackerNum;
    @FXML
    private TextField boardId;
    @FXML
    private TextField triggerNum;
    @FXML
    private Button image;
    @FXML
    private ProgressBar fileProgressBar;
    @FXML
    private Button creatBut;
    @FXML
    private Button colorBut;
    @FXML
    private Button fillBut;
    @FXML
    private Button fileopenBut;
    @FXML
    private TextArea ConfigLog;
    public static int trigger;
    public static SparkSession spark;
    private static LtpcDetector ltpcDetector;
    public static String tableName = "realdata";
    public static Dataset<BaseJob.datapck> ds = null;
    private boolean isConfiged = false;
    private File dir = null;
    public static int chargMax = colors.length + 1;
    public static int chargMin = 0;
    public ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private void initialize() {

        ConfigLog.setFont(Font.font(20));
        fileProgressBar.setProgress(0);
        Tooltip triggerTip = new Tooltip("填写触发号,必填");
        Tooltip trackerTip = new Tooltip("填写路径号，必填");
        Tooltip boardTip = new Tooltip("填写源板号，选填");
        Tooltip channelTip = new Tooltip("填写通道号，选填");
        Tooltip imageTip = new Tooltip("打开参考图片");
        triggerNum.setTooltip(triggerTip);
        trackerNum.setTooltip(trackerTip);
        boardId.setTooltip(boardTip);
        channelId.setTooltip(channelTip);
        image.setTooltip(imageTip);

        setIntegerFormatter(triggerNum);
        setIntegerFormatter(trackerNum);
        setIntegerFormatter(boardId);
        setIntegerFormatter(channelId);

        Tooltip triggerC = new Tooltip("模拟数据触发数");
        triggerCount.setTooltip(triggerC);
        setIntegerFormatter(triggerCount);

        creatBut.setDisable(true);
        colorBut.setDisable(true);
//        fillBut.setDisable(true);
        fileopenBut.setDisable(true);

    }

    @FXML
    public void creatDataAction() {
        if (isConfiged) {
            String text = triggerCount.getText();
            if (!"".equals(text)) {
                FileChooser fileChooser = FileChooseBuild();
                fileChooser.setTitle("Save File");
                File file = fileChooser.showSaveDialog(new Stage());


                fileProgressBar.progressProperty().unbind();
                fileProgressBar.setProgress(0);

                if (file != null) {
                    CreateData createData = new CreateData(file, Integer.parseInt(text));

                    //bind bar
                    fileProgressBar.progressProperty().bind(createData.progressProperty());

                    ConfigLog.appendText("\nData Create Start：Path:" + file.getAbsolutePath());

                    createData.start();
                    createData.setOnSucceeded(event -> {
                        ConfigLog.appendText("\nData Create Succeed。Path:" + file.getAbsolutePath());
                    });
                } else {
                    ConfigLog.appendText("\nNot choose fileSavePath");
                }
            } else {
                ConfigLog.appendText("\nTrigger Cannot be null");
            }
        } else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.appendText("\nPlease Import ConfigFile");
        }
    }

    private void setIntegerFormatter(TextField t) {
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
        fileChooser.setTitle("Please import excel File");
        File file = fileChooser.showOpenDialog(new Stage());
//        File file =new File(Main.class.getResource("/detector.xlsx").getFile());
        if (file != null) {
            ConfigLog.setStyle("-fx-text-fill:green");
            ConfigLog.setText("---------Initial Detector Info--------- \n Path: " + file.getAbsolutePath() + "\n\n");
            ReadConfig.setDetectorByXlxs(file);
            ltpcDetector = ReadConfig.getLtpcDetector();
//            String s = ltpcDetector.toString();
//            ConfigLog.appendText(s);

            Main.showDetector();
            dir = file.getParentFile();
            isConfiged = true;
            creatBut.setDisable(false);
            fileopenBut.setDisable(false);

        } else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.setText("\nPlease Import ConfigFile");
        }
    }

    @FXML
    private void imageAction() {
        VBox vBox = new VBox();
        ImageView imageView = new ImageView(Main.class.getResource("/DetectorInfo.png").toExternalForm());
        String s = "平面: 1    ; 径迹编号: 1,2,3,4,5,6,7,8,9,10,11,12 \n" +
                "平面: 2    ; 径迹编号: 13,14,15,16,17,18,19,20,21,22,23,24 \n" +
                "平面: 3    ; 径迹编号: 25,26,27,28,29,30,31,32,33,34,35,36 \n" +
                "平面: 4# 5# 6# 7# 8# 9  ; 径迹编号: 37# 38# 39# 40# 41# 42 ";
        TextArea textArea = new TextArea(s);
        textArea.setFont(Font.font(20));
        textArea.setStyle("-fx-border-color: red");
        textArea.setStyle("-fx-text-fill: blueviolet");
        textArea.setPrefWidth(vBox.getWidth());
        textArea.setWrapText(true);
        vBox.getChildren().addAll(textArea, imageView);

        Stage stage = new Stage();
        stage.setTitle("Route Info");
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setScene(new Scene(vBox));
        stage.show();
    }

    @FXML
    private void FileOpenAction() throws Exception {
        if (isConfiged) {
            FileChooser fileChooser = FileChooseBuild();
            File file = fileChooser.showOpenDialog(new Stage());
            if (file == null) {
                ConfigLog.setStyle("-fx-text-fill:red");
                ConfigLog.appendText("\n\nNot choose file");
            } else {
                if (spark != null) {
                    spark.close();
                }
                ConfigLog.appendText("\n\n##################Start Analyse##################\n");
                ConfigLog.setStyle("-fx-text-fill:mediumvioletred");
                ConfigLog.appendText("Path:" + file.getAbsolutePath());
                ConfigLog.appendText("\n\n##################Upload File to Hdfs##################\n");
                long start = System.currentTimeMillis();
//                upLoadFileAndAnalyse(file,start);
                fileProgressBar.progressProperty().unbind();
                fileProgressBar.setProgress(0);
                ScalaAnalyseSpark scalaAnalyseSpark = new ScalaAnalyseSpark(file, fileProgressBar, ConfigLog);
                scalaAnalyseSpark.start();
                scalaAnalyseSpark.setOnSucceeded(event -> {
                    ds = scalaAnalyseSpark.getValue();
                    long end = System.currentTimeMillis();
                    long UseSeconds = (end - start) / 1000;
                    ConfigLog.appendText("\nData Analyse Over,Use Time: " + UseSeconds + " s");
                    ConfigLog.appendText("\nHive Data:http://hd01.ihep.com:8889/hue/editor/?type=hive");
                    ConfigLog.appendText("\nTable Name: ltpcTable" + file.getName());
                    colorBut.setDisable(false);
                    fillBut.setDisable(false);
                    if (ds == null) {
                        ConfigLog.appendText("\ndataset is null");
                    } else {
                        ConfigLog.appendText("\n" + ds.describe("pckType", "trigger").showString(20, 20, false));
                        List<Row> error = ds.select("points").where("pckType='error'").limit(100).collectAsList();
                        if (error.size() > 0) {
                            ConfigLog.appendText("\nfind " + error.size() + "error packages:");
                            ConfigLog.appendText("\n points ");
                            error.forEach(row -> {
                                ConfigLog.appendText("\n "+row.getString(0));
                            });
                        }
                    }
                    ConfigLog.appendText("\n##################End Analyse##################\n");
                });
            }
        } else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.appendText("\n请先配置参数文件");
        }
    }

    private void upLoadFileAndAnalyse(File file, long start) {
        UploadFile uploadFile = new UploadFile(file);
        uploadFile.start();
        uploadFile.setOnSucceeded(event -> {
            if (uploadFile.getValue()) {
                tableName = "ltpcTable" + file.getName();
                executorService.submit(new sshtask(file, ConfigLog, start, colorBut, fillBut));
            } else {
                ConfigLog.appendText("\n\nUpload File to Hdfs Failed!\n");
            }
        });
    }


    static class sshtask implements Runnable {
        private File file;
        private TextArea ConfigLog;
        private long start;
        private Button colorBut;
        private Button fillBut;
        ;

        public sshtask(File file, TextArea configLog, long start, Button colorBut, Button fillBut) {
            this.file = file;
            ConfigLog = configLog;
            this.start = start;
            this.colorBut = colorBut;
            this.fillBut = fillBut;
        }

        @Override
        public void run() {
            javafx.application.Platform.runLater(() ->
            {
                ConfigLog.appendText("\nUpload File to Hdfs Succeeded!\n");
                ConfigLog.appendText("Start Remote Spark Job\n");
                ConfigLog.appendText("\nSpark Job WebUi:http://hd01.ihep.com:8088/cluster/apps/RUNNING\n");
            });
            SSHTool tool = new SSHTool("hd01", "root", "online123", StandardCharsets.UTF_8);
            try {
                tool.exec("spark-submit --class submit_Spark_CDH.LtpcDataAnalyse --master yarn --deploy-mode cluster --num-executors 10 --executor-cores 3 --executor-memory 6G ./spark-ltpcData-2.1-release.jar " + "hdfs://hd01:8020/user/wy/creat_data/"
                        + file.getName() + " " + 20 + " "
                        + "ltpcTable" + file.getName());
            } catch (IOException e) {
                javafx.application.Platform.runLater(() ->
                        ConfigLog.appendText("\nSpark Job Failed :" + e.getMessage() + "\n"));
            }
            long end = System.currentTimeMillis();
            long UseSeconds = (end - start) / 1000;
            javafx.application.Platform.runLater(() ->
            {
                ConfigLog.appendText("\nData Analyse Over,Use Time: " + UseSeconds + " s");
                ConfigLog.appendText("\nHive Data:http://hd01.ihep.com:8889/hue/editor/?type=hive");
                ConfigLog.appendText("\nTable Name: ltpcTable" + file.getName());
                ConfigLog.appendText("\n##################End Analyse##################\n");
                colorBut.setDisable(false);
                fillBut.setDisable(false);
            });
        }
    }

    public FileChooser FileChooseBuild() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有类型", "*.*"),
                new FileChooser.ExtensionFilter("Excel类型", "*.xlsx"),
                new FileChooser.ExtensionFilter("数据类型", "*.bin")
        );
        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }
        return fileChooser;
    }

    @FXML
    private void ColorButtonAction() {
        VBox ColorBlock = new VBox();
        VBox labels = new VBox();
        ColorBlock.setPrefHeight(560);
        ColorBlock.setPrefWidth(120);
        ColorBlock.setBorder(new Border(
                new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)
        ));
        labels.setPrefHeight(560);
        labels.setPrefWidth(120);
        labels.setBorder(new Border(
                new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)
        ));

        double wdith = ColorBlock.getPrefWidth();
        double height = ColorBlock.getPrefHeight();
        int count = colors.length + 1;

        int bin = (chargMax - chargMin) / count;
        int lastbin = chargMax;

        for (int i = 0; i < colors.length; i++) {
            Rectangle rectangle = new Rectangle(0, height / (double) count, wdith, height / (double) count);
            rectangle.setFill(colors[colors.length - 1 - i]);
            rectangle.setStyle("-fx-stroke-type:outside");
            ColorBlock.getChildren().add(rectangle);

            Label lab = new Label((lastbin - bin) + "->" + lastbin);
            lastbin = lastbin - bin;
            lab.setPrefWidth(wdith);
            lab.setFont(Font.font(15));
            lab.setPrefHeight(height / (double) count);
            labels.getChildren().add(lab);
        }
        Label label = new Label("颜色标尺");
        label.setPrefWidth(wdith);
        label.setFont(Font.font(20));
        label.setPrefHeight(height / (double) count);
        ColorBlock.getChildren().add(label);

        Label label2 = new Label(" 能量区间 ");
        label2.setPrefWidth(wdith);
        label2.setFont(Font.font(20));
        label2.setPrefHeight(height / (double) count);
        labels.getChildren().add(label2);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("  颜色标尺 ");

        HBox hbox = new HBox();
        hbox.setPrefWidth(2 * wdith + 10);
        hbox.setPrefHeight(height);
        hbox.setSpacing(10);
        Scene scene = new Scene(hbox);
        hbox.getChildren().addAll(ColorBlock, labels);

        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void fillButtonAction() throws SQLException {
        if (tableName == null || "".equals(tableName)) {
            ConfigLog.appendText("\ntableName error");
            return;
        }

        String triggerS = triggerNum.getText();
        String trackerS = trackerNum.getText();
        String boardS = boardId.getText();
        String channelS = channelId.getText();
        StringBuilder sqlBuilder = new StringBuilder();
//        sqlBuilder.append("select board,channelid,max_point from " + tableName + " where");
        ConfigLog.setStyle("-fx-text-fill:red");
        try {
            if (!"".equals(triggerS)) {
                sqlBuilder.append(" trigger=" + triggerS);
                trigger = Integer.valueOf(triggerS);
            } else {
                ConfigLog.appendText("\n触发序号必须填写");
                return;
            }
            if (!"".equals(trackerS)) {
                sqlBuilder.append(" and tracker=" + trackerS);
            } else {
                ConfigLog.appendText("\n径迹号必须填写");
                return;
            }
            if (!"".equals(boardS)) {
                sqlBuilder.append(" and board=" + boardS);
            }
            if (!"".equals(channelS)) {
                sqlBuilder.append(" and channelId=" + channelS);
            }
        } catch (NumberFormatException e) {
            ConfigLog.appendText(e.getMessage());
        }
        ConfigLog.appendText("\nlinking to spark sql:" + sqlBuilder.toString());
        executorService.submit(() -> {
            Dataset<Row> where = ds.select("board", "channelId", "max_point").where(sqlBuilder.toString());
            ConfigLog.appendText("\n Spark sql compeleted！Result describe:\n" + where.describe().showString(20, 0, false));
            List<Row> rows = where.collectAsList();
            if (rows.size() > 0) {
                try {
                    DetectorPaintController.fillRectOPT(rows, ConfigLog);
                } catch (SQLException e) {
                    ConfigLog.appendText(e.getMessage());
                }
            }
        });


//          Hive job
//        executorService.submit(() -> {
//        ConfigLog.appendText("\nlinking to Hive sql:" + sqlBuilder.toString());
//            try {
//
//                ResultSet resultSet = new HiveUtil(ConfigLog).SQLResult(sqlBuilder.toString());
//                if (resultSet == null) {
//                    ConfigLog.appendText("\n Hive sql failed!");
//                    return;
//                }
//                javafx.application.Platform.runLater(() ->
//                {
//                    try {
//                        DetectorPaintController.fillRectOPT(resultSet,ConfigLog);
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                });
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        });
    }

    static class sparkTask implements Callable<String>, Serializable {
        private String sql;

        public sparkTask(String sql) {
            this.sql = sql;
        }

        @Override
        public String call() throws SQLException {
            return null;
        }
    }

    public static void main(String[] args) throws SQLException {
        ConfigController configController = new ConfigController();
        ConfigController.tableName = "ltpctable100";
        configController.fillButtonAction();
    }

    public static LtpcDetector getLtpcDetector() {
        return ltpcDetector;
    }

    public static void setLtpcDetector(LtpcDetector ltpcDetector) {
        ConfigController.ltpcDetector = ltpcDetector;
    }
}
