package com.wy.display;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/7/17 18:25
 */

import com.wy.Main;
import com.wy.display.config.ConfigController;
import com.wy.display.daq.DaqController;
import com.wy.display.statistics.StatisticsController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-07-17 18:25
 **/
public class LtpcController {
    @FXML
    private  TabPane tabPane;
    @FXML
    private Tab daqTab;
    @FXML
    private Tab analyseTab;
    @FXML
    public DaqController daqController;
    @FXML
    public ConfigController configController;
    public StatisticsController statisticController;
    @FXML
    private void initialize() throws IOException {


        daqController.daqBox.prefWidthProperty().bind(tabPane.widthProperty());
        daqController.daqBox.prefHeightProperty().bind(tabPane.heightProperty());
        statisticController.vBox.prefWidthProperty().bind(tabPane.widthProperty());
        statisticController.vBox.prefHeightProperty().bind(tabPane.heightProperty());
        configController.vbox.prefWidthProperty().bind(tabPane.widthProperty());
        configController.vbox.prefHeightProperty().bind(tabPane.heightProperty());



    }
}
