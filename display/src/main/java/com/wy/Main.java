package com.wy; /**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/10 13:01
 */

import com.wy.display.LtpcController;
import com.wy.display.daq.DaqController;
import com.wy.display.statistics.StatisticsController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private LtpcController controller=null;
    @Override
    public void stop() {
        System.out.println("stop soft");
    }
    @Override
    public void start(Stage primaryStage) throws Exception{

        URL resource =Main.class.getResource("/ltpc.fxml");

        FXMLLoader loader = new FXMLLoader(resource);

        Parent root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root);

//        scene.getStylesheets().add(Main.class.getResource("/sample.css")
//                .toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Ltpc Data Online Soft");
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setResizable(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            if ("Running".equals(controller.daqController.Status)) {
                DaqController.executor.shutdown();
                try {
                    DaqController.executor.awaitTermination(3, TimeUnit.SECONDS);
                    if (!DaqController.executor.isShutdown()){
                        DaqController.executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.WARNING, "daq process is not closed! please use red X button!!!");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait().ifPresent(response -> Platform.exit());
            }
            controller.configController.executorService.shutdown();
            try {
                controller.configController.executorService.awaitTermination(10,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.exit();
        });
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
    public static void main(String[] args) {
        launch(args);
    }

}
