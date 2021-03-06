package com.wy; /**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/10 13:01
 */

import com.wy.display.statistics.StatisticsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:程序主类
 *
 * @author: WuYe
 *
 * @create: 2020-03-10 13:01
 **/
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resource = getClass().getResource("/config.fxml");
        Parent root = FXMLLoader.load(resource);

        primaryStage.setTitle("Ltpc Data Online Soft");
        primaryStage.setScene(new Scene(root));
        primaryStage.setX(0);
        primaryStage.setY(0);
//        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }
    public static void showDetector() throws IOException {
        URL resource = Main.class.getResource("/DetectorPaint.fxml");
        Parent root = FXMLLoader.load(resource);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Ltpc Detector Display");
        primaryStage.setScene(new Scene(root));
        primaryStage.setX(1000);
        primaryStage.setY(0);
        primaryStage.show();
    }
        //打开统计窗口
    public static void showFileResult() throws IOException {
        URL resource = Main.class.getResource("/Statistics.fxml");
        Parent root = FXMLLoader.load(resource);
        Stage primaryStage = new Stage();
        primaryStage.setX(1000);
        primaryStage.setY(800);
        primaryStage.setTitle("File Analysed Result");
        primaryStage.setScene(new Scene(root,1200,800));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
