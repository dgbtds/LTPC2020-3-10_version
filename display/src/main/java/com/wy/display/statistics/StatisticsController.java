package com.wy.display.statistics;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/14 12:14
 */

import com.wy.model.data.DataSource;
import com.wy.model.decetor.LtpcDetector;
import com.wy.model.decetor.LtpcSourceBoard;
import com.wy.model.decetor.PlaneWithTrack;
import com.wy.model.decetor.Tracker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.HashMap;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:数据统计界面
 *
 * @author: WuYe
 *
 * @create: 2020-03-14 12:14
 **/
public class StatisticsController {

    private static DataSource dataSource;
    private static final double beanNum=10;
    @FXML
    private AnchorPane root;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem textMenuItem;
    @FXML
    private CheckMenuItem boardHitMap;
    @FXML
    private CheckMenuItem channelHitMap;
    @FXML
    private CheckMenuItem frequency;
    @FXML
    private CheckMenuItem energyLoss;
    @FXML
    private HBox hBox;
    boolean showText=false;
    boolean showHitmap=false;
    boolean showEnergy=false;
    private PieChart pieChart=null;
    private BarChart<String,Number> barChart=null;
    private LineChart<String,Number> channelHitChart=null;
    private BarChart<String,Number> frequencyChart=null;
    private BarChart<String,Number> energyLossChart=null;
    @FXML
    private void initialize(){
        menuBar.prefWidthProperty().bind(root.widthProperty());
        hBox.prefWidthProperty().bind(root.widthProperty().subtract(50));
        hBox.prefHeightProperty().bind(root.heightProperty().subtract(100));

        //文字统计
        textMenuItem.setOnAction(event -> {
            if (showHitmap||showEnergy) {
                hBox.getChildren().clear();
                showHitmap=false;
                showEnergy=false;
                channelHitMap.setSelected(false);
                boardHitMap.setSelected(false);
            }
            TextArea textArea = new TextArea();
            textArea.prefWidthProperty().bind(hBox.widthProperty());
            textArea.prefHeightProperty().bind(hBox.heightProperty());
            textArea.setFont(Font.font(20));
            textArea.setEditable(false);
            textArea.setText(dataSource.toString());
            hBox.getChildren().add(textArea);
            showText=true;
        });

        //板块击中统计
        boardHitMap.setOnAction(event -> {
            if (showText||showEnergy) {
                hBox.getChildren().clear();
                showEnergy=false;
                showText=false;
                frequency.setSelected(false);
                energyLoss.setSelected(false);
            }
            if (boardHitMap.isSelected()){
                 pieChart = new PieChart();
                HashMap<Integer, LtpcSourceBoard> map = LtpcDetector.SourceBoardMap;
                PieChart finalPieChart = pieChart;
                map.forEach((k, v)->{
                    PieChart.Data data = new PieChart.Data(k+"",v.getClickCount());
                    finalPieChart.getData().add(data);
                });
                pieChart.setTitle("Board Clicked pieChart");
                pieChart.setPrefWidth(hBox.getPrefWidth()/4);
                pieChart.getData().forEach(data -> {
                    Node node = data.getNode();
                    String format = String.format("board: %s , Proportion: %.2f%%", data.getName(), (double) data.getPieValue() *100/ (double) dataSource.getAllPackageCount());
                    Tooltip tooltip = new Tooltip(format);
                    tooltip.setFont(new Font(20));
                    Tooltip.install(node,tooltip);
                    node.setOnMouseClicked(event1 -> {
                        CategoryAxis X = new CategoryAxis();
                        X.setLabel(data.getName()+"ChannelNum");
                        NumberAxis Y = new NumberAxis();
                        LineChart<String, Number> Chart = new LineChart<>(X,Y);
                        LtpcSourceBoard ltpcSourceBoard = map.get(Integer.parseInt(data.getName()));
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName("board-"+data.getName());
                        ltpcSourceBoard.getLtpcChannels().forEach(c->{
                            XYChart.Data<String, Number> data1 = new XYChart.Data<>(c.getChannelId()+"",c.getClickCount());
                            series.getData().add(data1);
                        });
                        Chart.setTitle(data.getName()+"Board Clicked lineChart");
                        Chart.getData().add(series);

                        Stage stage = new Stage();
                        stage.setScene(new Scene(Chart));
                        stage.show();
                    });
                });
                hBox.getChildren().add(pieChart);

                CategoryAxis barChartX = new CategoryAxis();
                barChartX.setLabel("Board");
                NumberAxis barChartY = new NumberAxis();
                barChartY.setLabel("Clicked Count");

                barChart=new BarChart<>(barChartX,barChartY);
                     barChart.setLegendVisible(false);
                     barChart.setPrefWidth(hBox.getPrefWidth()/4);
                     XYChart.Series<String, Number> series = new XYChart.Series<>();
                map.forEach((k, v)->{
                    series.getData().add(new XYChart.Data<>(k+"",v.getClickCount()));
                });
                    barChart.getData().add(series);
                barChart.setBarGap(3);
                barChart.setCategoryGap(0);
                barChart.setTitle("Board Clicked barChart");
                hBox.getChildren().add(barChart);

                showHitmap=true;
            }
            else {
                hBox.getChildren().remove(pieChart);
                hBox.getChildren().remove(barChart);
                pieChart=null;
                barChart=null;
            }
        });

        //通道击中统计
        channelHitMap.setOnAction(event -> {
            if (showText||showEnergy) {
                hBox.getChildren().clear();
                showEnergy=false;
                showText=false;
                frequency.setSelected(false);
                energyLoss.setSelected(false);
            }
            if (channelHitMap.isSelected()) {
                CategoryAxis X = new CategoryAxis();
                X.setLabel("Channel Id");
                NumberAxis Y = new NumberAxis();
                Y.setTickUnit(1);
                Y.setLabel("Clicked Count");
                channelHitChart = new LineChart<>(X,Y);
                HashMap<Integer, LtpcSourceBoard> map = LtpcDetector.SourceBoardMap;
                channelHitChart.setPrefWidth(hBox.getPrefWidth()/2);
                map.forEach((k,v)->{
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(""+k);
                    v.getLtpcChannels().forEach(c->{
                        XYChart.Data<String, Number> data = new XYChart.Data<>(c.getPid()+"",c.getClickCount());
                        series.getData().add(data);
                    });

                    channelHitChart.setTitle("Channel Clicked lineChart");
                    channelHitChart.getData().add(series);
                });

                hBox.getChildren().add(channelHitChart);
                showHitmap=true;
            }
            else {
                hBox.getChildren().remove(channelHitChart);
                channelHitChart=null;
            }
        });
        frequency.setOnAction(event -> {
            if (showText||showHitmap) {
                hBox.getChildren().clear();
                showHitmap=false;
                showText=false;
                channelHitMap.setSelected(false);
                boardHitMap.setSelected(false);
            }
//            if (frequency.isSelected()){
//                int chargeMin = dataSource.getChargeMin();
//                double bean = (dataSource.getChargeMax() - chargeMin) / beanNum;
//                 int [] energyDistribution=new int[(int) beanNum];
//                dataSource.getSdList().forEach(s->{
//                    int i = (int) ((s.getCharge() - dataSource.getChargeMin()) / bean);
//                    if (i>=beanNum){
//                        i= (int) (beanNum-1);
//                    }
//                    energyDistribution[i]+=1;
//                });
//                XYChart.Series<String, Number> series = new XYChart.Series<>();
//                for(int i=0;i<energyDistribution.length;i++){
//                    int s = (int)(chargeMin + i  * bean);
//                    int e = (int)(chargeMin + (i + 1) * bean);
//                    series.getData().add(new XYChart.Data<>(s+"-"+e,energyDistribution[i]));
//                }
//
//                CategoryAxis barChartX = new CategoryAxis();
//                barChartX.setLabel("Energy Bean");
//                NumberAxis barChartY = new NumberAxis();
//                barChartY.setLabel("Package Count");
//
//                frequencyChart=new BarChart<>(barChartX,barChartY);
//                frequencyChart.setTitle("Data Energy Distribution");
//                frequencyChart.setBarGap(3);
//                frequencyChart.setCategoryGap(0);
//                frequencyChart.setPrefWidth(hBox.getPrefWidth()/2);
//                frequencyChart.getData().add(series);
//                hBox.getChildren().add(frequencyChart);
//                showEnergy=true;
//            }
            else {
                hBox.getChildren().remove(frequencyChart);
                frequencyChart=null;
            }
        });
//        energyLoss.setOnAction(event -> {
//            if (showText||showHitmap) {
//                hBox.getChildren().clear();
//                showHitmap=false;
//                showText=false;
//                channelHitMap.setSelected(false);
//                boardHitMap.setSelected(false);
//            }
//            if (energyLoss.isSelected()){
//                int[] EnergyLossArr = new int[Tracker.allTrackerNum];
//                dataSource.getSdList().forEach(s->{
//                    int trackerNum = s.getTrackerNum();
//                    EnergyLossArr[trackerNum-1]+=s.getCharge();
//                });
//                XYChart.Series<String, Number> series = new XYChart.Series<>();
//                for(int i=0;i<EnergyLossArr.length;i++){
//                    int j = i + 1;
//                    String s = String.valueOf(j);
//                    Tracker tracker = Tracker.trackerHashMap.get(j);
//                    if (tracker==null){
//                        Alert error = new Alert(Alert.AlertType.ERROR,"ERROR:  Tracker"+j+" Not Existed Exception");
//                        error.showAndWait();
//                    }
//                    double e = (double)EnergyLossArr[i]/(double)tracker.cluster*6 ;
//                    series.getData().add(new XYChart.Data<>(s,e));
//                    System.out.println("num-- " + j+" : loss-- "+e);
//                }
//
//                CategoryAxis barChartX = new CategoryAxis();
//                barChartX.setLabel("Tracker Number");
//                NumberAxis barChartY = new NumberAxis();
//                barChartY.setLabel("TrackerEnergy/TrackerLength C/mm");
//
//
//                energyLossChart=new BarChart<>(barChartX,barChartY);
//                energyLossChart.setBarGap(3);
//                energyLossChart.setCategoryGap(0);
//                energyLossChart.setPrefWidth(hBox.getPrefWidth()/2);
//                energyLossChart.setTitle("Average Energy Loss");
//                energyLossChart.getData().add(series);
//                hBox.getChildren().add(energyLossChart);
//                showEnergy=true;
//            }
//            else {
//                hBox.getChildren().remove(energyLossChart);
//                energyLossChart=null;
//            }
//        });
    }




    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        StatisticsController.dataSource = dataSource;
    }
}
