package com.wy.display.daq;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/7/17 19:22
 */

import com.wy.model.data.Configuration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @program: LTPC2020-3-10_version
 * @description:
 * @author: WuYe
 * @create: 2020-07-17 19:22
 **/
public class DaqController {
    @FXML
    public VBox daqBox;
    @FXML
    private TableView tabview;
    @FXML
    private TableColumn<String, Configuration> key;
    @FXML
    private TableColumn<String, Configuration> value;
    @FXML
    private Menu command;
    @FXML
    private TextField changeValue;
    @FXML
    private Button saveChange;
    @FXML
    private Button runDaq;
    @FXML
    private Button ConfigBoard;
    @FXML
    private Button close;
    @FXML
    private Button autoReadOut;
    @FXML
    private ComboBox<Integer> cb;
    @FXML
    private TextArea daqlog;
    @FXML
    private HBox rb1;
    @FXML
    private HBox rb2;
    private Configuration selectedItem = null;
    public String Status = "Waitting";
    private String[] boards = {"254", "12", "14", "13", "18", "28",
            "31", "15", "16", "17", "19", "5",
            "2", "32", "27", "11", "26", "30", "33", "6"};
    private String[] cmds = new String[25];

    private TreeMap<String, String> treemap = new TreeMap<>();
    private ObservableList<Configuration> entrys = FXCollections.observableArrayList();
    private String[] keyArr = null;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private Process process = null;
    private HashMap<String, File> filemap = new HashMap();
    private int Configboardnum = 0;
    private Configuration ConfigConf;
    private Configuration EnableConf;
    private Properties prop = new Properties();
    //enableboards String
    private LinkedList<String> enboardStr = new LinkedList<String>();

    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 11, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10), r -> {
        Thread thread = new Thread(r);
        thread.setName("DAQ Thread");
        return thread;
    });


    @FXML
    private void initialize() {
        //get all radioButtons
        ObservableList<Node> children1 = rb1.getChildren();
        ObservableList<Node> children2 = rb2.getChildren();
        ArrayList<Node> radioButtons = new ArrayList<>(children1.size() + children2.size());
        radioButtons.addAll(children1);
        radioButtons.addAll(children2);

        for (int i = 0; i < 20; i++) {
            filemap.put(String.format("Config_board_%d.txt", i), null);
        }
        filemap.put("Config_common.txt", null);
        filemap.put("Config_multiboard.txt", null);
        filemap.put("DefaultConfig.prop", null);


        daqlog.setFont(Font.font(18));
        command.getItems().forEach(menuItem -> {
            if (!(menuItem instanceof SeparatorMenuItem)) {
                menuItem.setOnAction(event -> {
                    String newValue = menuItem.getId();
                    if ("Running".equals(Status) && process != null && process.isAlive()) {
                        try {
                            if ("Q".equals(newValue)) {
                                if (process != null && process.isAlive()) {
                                    bufferedWriter.write(newValue + "\n");
                                    Status = "Waitting";
                                    bufferedWriter.close();
                                    bufferedWriter.close();
                                }
                            } else {
                                bufferedWriter.write(newValue + "\n");
                                bufferedWriter.flush();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        daqlog.appendText("DAQ not Running!!!\n");
                    }
                });
            }
        });
        ObservableList<Integer> iarr = FXCollections.observableArrayList();
        for (int i = 0; i < boards.length; i++) {
            iarr.add(i);
        }
        cb.setItems(iarr);
        cb.getSelectionModel().selectedItemProperty().addListener((observer, oldValue, newValue) -> {
            ConfigBoard.setDisable(true);
            Configboardnum = newValue;
            ConfigConf.setValue(newValue+"");

            StringJoiner stringJoiner = new StringJoiner(" , ");
            for (int i = 0; i <= Configboardnum; i++) {
                stringJoiner.add(boards[i]);
            }
            //使能选择框
            radioButtons.forEach(c -> {
                RadioButton c1 = (RadioButton) c;
                c1.setSelected(false);
                int id = Integer.parseInt(c.getId());
                if (id <= Configboardnum) {
                    c1.setDisable(false);
                }
                else {
                    c1.setDisable(true);
                }
            });
            daqlog.appendText("you choose config boards: " + stringJoiner.toString() + "\n");
            daqlog.appendText("Please enable Boards \n");
        });

        //disable button
        cb.setDisable(true);
        runDaq.setDisable(true);
        ConfigBoard.setDisable(true);
        saveChange.setDisable(true);
        close.setDisable(true);
        command.setDisable(true);


        radioButtons.forEach(c -> {
            RadioButton rb=(RadioButton) c;
            rb.selectedProperty().addListener( (observable, oldValue, newValue) -> {
                String id = rb.getId();
                ConfigBoard.setDisable(false);
                if (rb.isSelected()) {
                    if (!enboardStr.contains(boards[Integer.parseInt(id)])) {
                        enboardStr.add(boards[Integer.parseInt(id)]);
                        EnableConf.setValue(String.join(",", enboardStr));
                    }
                    daqlog.appendText("you enable board" + id + "!!!\n");
                } else {
                    if (enboardStr.contains(boards[Integer.parseInt(id)])) {
                        enboardStr.removeIf(s -> s.equals(boards[Integer.parseInt(id)]));
                        EnableConf.setValue(String.join(",", enboardStr));
                    }
                    daqlog.appendText("you disable board" + id + "!!!\n");
                }
            });

        });
    }

    @FXML
    private void saveChangeAction() {
        String value = changeValue.getText();
        if (value.matches(" *")) {
            daqlog.appendText("Configuration cant set null \n");
            return;
        }
        if ("EnableBoards".equals(selectedItem.getKey())) {
            daqlog.appendText(" cant change EnableBoards!!!\n");
            return;
        }
        if ("ConfigBoardNum".equals(selectedItem.getKey())) {
            daqlog.appendText("cant change ConfigBoardNum!!!\n");
            return;
        }
        if (selectedItem != null) {
            if (!value.equals(selectedItem.getValue())) {
                daqlog.appendText("change --" + selectedItem.getKey() + "--from " + selectedItem.getValue() + "--to--" + value + "\n");
                selectedItem.setValue(value);
                prop.setProperty(selectedItem.getKey(), value);
            }
        } else {
            daqlog.appendText("not choose configuration!!!\n");
        }
    }

    @FXML
    public void closeAction() throws IOException {
        File file = filemap.get("DefaultConfig.prop");
        if (file == null) {
            daqlog.appendText("DefaultConfig.prop not exist\n");
            return;
        }
        saveDefaultConfig(file);
        daqlog.appendText("RUN Configuration Save At :"+file.getName()+"\n");
        if (process != null && process.isAlive()) {
            process.destroy();
            daqlog.appendText(" Killing DAQ Processed!!!"+"\n");
        }
        if (process==null || !process.isAlive()){
            Status="Waitting";
            daqlog.appendText("DAQ  Stop success!!!"+"\n");
        }

    }

    @FXML
    private void setdirAction() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择配置文件目录");
        File file = directoryChooser.showDialog(new Stage());
        if (file != null) {
            File[] files = file.listFiles((dir, name) -> filemap.containsKey(name));
            if (files == null || files.length != filemap.size()) {
                daqlog.appendText("设置配置文件夹目录失败\n");
            } else {
                for (File f : files) {
                    filemap.put(f.getName(), f);
                }
                daqlog.appendText("设置配置文件夹目录成功\n");
                loadDefaultConfig();
                cb.setDisable(false);
                runDaq.setDisable(false);
                saveChange.setDisable(false);
                close.setDisable(false);
            }
        }
    }

    @FXML
    private void ConfigBoardAction() throws IOException {
        if (Configboardnum < boards.length) {
            daqlog.appendText("------------->start config boards\n");
            ConfigConf.setValue(Configboardnum + "");
            prop.setProperty("ConfigBoardNum", "" + Configboardnum);

            //修改board文件
            setBoardFile();
            //修改Config_multiboard.txt
            setMulboardFile();
        }
    }

    private void setMulboardFile() throws IOException {
        String filname = "Config_multiboard.txt";
        File file = filemap.get(filname);
        if (file != null) {
            String line;
            CharArrayWriter tempStream = new CharArrayWriter();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while ((line = bufferedReader.readLine()) != null) {
                if ("".equals(line)) {
                    continue;
                }
                // 替换每行中, 符合条件的字符串
                if (line.startsWith("//")) {
                    tempStream.write(line + "\n");
                    continue;
                }
                if (line.contains("board_n")) {
                    String[] s = line.split(" +");
                    List<String> stringList = Arrays.stream(s).filter(c -> !"".equals(c)).collect(Collectors.toList());
                    if (stringList.size() >= 2) {
                        stringList.set(1, "[" + (Configboardnum + 1) + "]");
                        // 将该行写入内存
                        tempStream.write(String.join(" ", stringList));
                    } else {
                        tempStream.write(line);
                    }
                } else {
                    // 将该行写入内存
                    tempStream.write(line);
                }
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));

            }
            // 关闭 输入流
            bufferedReader.close();
            // 将内存中的流 写入 文件
            FileWriter out = new FileWriter(file);
            tempStream.writeTo(out);
            out.close();
            javafx.application.Platform.runLater(() -> {
                daqlog.appendText("\n==============================\n");
                daqlog.appendText("\n" + filname + " config successed\n");
                daqlog.appendText("\n==============================\n");
            });
        } else {
            javafx.application.Platform.runLater(() -> {
                daqlog.appendText("\n==============================\n");
                daqlog.appendText(filname + " not exist\n");
                daqlog.appendText("\n==============================\n");
            });

        }
    }

    private void setBoardFile() throws IOException {
        javafx.application.Platform.runLater(() ->
                daqlog.appendText("\n==============================\n"));
        for (int i = 0; i < boards.length; i++) {
            String filname = String.format("Config_board_%d.txt", i);
            File file = filemap.get(filname);
            if (file != null) {
                String line;
                CharArrayWriter tempStream = new CharArrayWriter();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                while ((line = bufferedReader.readLine()) != null) {
                    if ("".equals(line)) {
                        continue;
                    }
                    // 替换每行中, 符合条件的字符串
                    if (line.startsWith("//")) {
                        tempStream.write(line + "\n");
                        continue;
                    }
                    String[] s = line.split(" ");
                    List<String> stringList = Arrays.stream(s).filter(c -> !"".equals(c)).collect(Collectors.toList());
                    if (stringList.size() != 4) {
                        tempStream.write(line + "\n");
                        continue;
                    }
                    if (enboardStr.contains(boards[i])) {
                        stringList.set(3, "[1]");
                    } else {
                        stringList.set(3, "[0]");
                    }
                    // 将该行写入内存
                    tempStream.write(String.join(" ", stringList));
                    // 添加换行符
                    tempStream.append(System.getProperty("line.separator"));
                }
                // 关闭 输入流
                bufferedReader.close();
                // 将内存中的流 写入 文件
                FileWriter out = new FileWriter(file);
                tempStream.writeTo(out);
                out.close();
                javafx.application.Platform.runLater(() ->
                        daqlog.appendText(filname + " config successed\n"));
            } else {
                javafx.application.Platform.runLater(() -> {
                            daqlog.appendText("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
                            daqlog.appendText(filname + " not exist\n");
                            daqlog.appendText("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
                        }
                );
                break;
            }
        }
        javafx.application.Platform.runLater(() -> daqlog.appendText("\n==============================\n"));
    }

    private void loadDefaultConfig() throws IOException {
        prop.load(new FileInputStream(filemap.get("DefaultConfig.prop")));
        treemap.put("daqExe", prop.getProperty("daqExe"));
        treemap.put("singleFileTime", prop.getProperty("singleFileTime"));
        treemap.put("singleFileNum", prop.getProperty("singleFileNum"));
        treemap.put("MoldingTime", prop.getProperty("MoldingTime"));
        treemap.put("Gain", prop.getProperty("Gain"));
        treemap.put("ConfigBoardNum", prop.getProperty("ConfigBoardNum"));
        treemap.put("EnableBoards", prop.getProperty("EnableBoards"));
        String enableBoards = prop.getProperty("EnableBoards");
        String ConfigBoards = prop.getProperty("ConfigBoardNum");
        if (enableBoards!=null && ConfigBoards!=null ) {
            String[] EnableBoards =enableBoards.split(",",-1);
            for (String b:EnableBoards){
                b=b.trim();
                if (Arrays.asList(boards).contains(b)){
                    enboardStr.add(b);
                }
            }
            rb1.getChildren().forEach(c->{
                if (Integer.parseInt(c.getId())<=Integer.parseInt(ConfigBoards)){
                    c.setDisable(false);
                }
                if (enboardStr.contains(boards[Integer.parseInt(c.getId())])){
                    RadioButton c1 = (RadioButton) c;
                    c1.setSelected(true);
                }
            });
            rb2.getChildren().forEach(c->{
                if (Integer.parseInt(c.getId())<=Integer.parseInt(prop.getProperty("ConfigBoardNum"))){
                    c.setDisable(false);
                }
                if (enboardStr.contains(boards[Integer.parseInt(c.getId())])){
                    RadioButton c1 = (RadioButton) c;
                    c1.setSelected(true);
                }
            });
        }

        keyArr = new String[treemap.keySet().size()];
        treemap.keySet().toArray(keyArr);

        key.setCellValueFactory(new PropertyValueFactory<>("key"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        treemap.forEach((k, v) -> {
            if (v==null){
                v="Null";
            }
            if ("ConfigBoardNum".equals(k)) {
                ConfigConf = new Configuration(k, v);
                entrys.add(ConfigConf);
            }
            else if ("EnableBoards".equals(k)){
                EnableConf=new Configuration(k,v);
                entrys.add(EnableConf);
            }  else
             {
                entrys.add(new Configuration(k, v));
            }
        });

        tabview.setItems(entrys);
    }

    private void setCmds() {
        cmds[0] = prop.getProperty("daqExe");
        cmds[1] = prop.getProperty("singleFileTime");
        cmds[2] = prop.getProperty("singleFileNum");
        cmds[3] = prop.getProperty("MoldingTime");
        cmds[4] = prop.getProperty("Gain");

        prop.setProperty("EnableBoards",String.join(",",enboardStr));

        for (int i = 0; i < boards.length; i++) {
            if (enboardStr.contains(boards[i])) {
                cmds[5 + i] = "15";
            } else {
                cmds[5 + i] = "0";
            }
        }
    }

    private void saveDefaultConfig(File saveFile) throws IOException {
        FileWriter fileWriter = new FileWriter(saveFile);
        prop.store(fileWriter, "Save Config");
        fileWriter.close();
        daqlog.appendText("\n----------------Save  Config Success-----------------\n");
    }

    @FXML
    private void runDaqAction() {
        if ("Waitting".equals(Status)) {
            setCmds();
            try {
                for (String s : cmds) {
                    if (s == null) {
                        daqlog.appendText("configuration not complete!!!");
                        return;
                    }
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String date = df.format(new Date());
                File file = new File(cmds[0]);
                if (file == null) {
                    javafx.application.Platform.runLater(() ->
                            daqlog.appendText(" exe path is null\n"));
                    return;
                }
                daqlog.appendText("\n^^^^^^^^^^^^^^^^^^^ Saving RUN Config ^^^^^^^^^^^^^^^^^^^\n");
                String savePath = file.getParentFile().getParent() + "\\Data_bin_files\\ConfigInfo_RUN_" + date + ".txt";
                File file1 = new File(savePath);
                if (file1==null){
                    daqlog.appendText("----------------Save  Config Failed-----------------\n");
                    daqlog.appendText(file.getParentFile().getParent() + "\\Data_bin_files\\  Dir not Exist!!!");
                    return;
                }
                saveDefaultConfig(file1);

                daqlog.appendText("\n%%%%%%%%%%%%%%%%%%%%RUN DAQ Config Info%%%%%%%%%%%%%%%%%%%%\n" );
                daqlog.appendText("daqExe:  "+prop.getProperty("daqExe","Null")+"\n");
                daqlog.appendText("singleFileTime:  "+prop.getProperty("singleFileTime","Null")+"\n");
                daqlog.appendText("singleFileNum:   "+prop.getProperty("singleFileNum","Null")+"\n");
                daqlog.appendText("MoldingTime:   "+ prop.getProperty("MoldingTime","Null")+"\n");
                daqlog.appendText("Gain:    "+prop.getProperty("Gain","Null")+"\n");
                daqlog.appendText("ConfigBoardNum:    "+ prop.getProperty("ConfigBoardNum","Null")+"\n");
                daqlog.appendText("EnableBoards:    "+prop.getProperty("EnableBoards","Null")+"\n");
                daqlog.appendText("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" );


                process = new ProcessBuilder(cmds).start();
                if (process.isAlive()) {
                    //获取进程输入流
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    //获取进程输出流
                    bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    Status = "Running";
                    command.setDisable(false);
                    autoReadOut.setDisable(false);
                }
                else {
                    daqlog.appendText("\n############### LtpcDaq Start Failed #################\n");
                }
                Runnable logout = () -> {
                    String line;
                    try {
                        while ((line = bufferedReader.readLine()) != null) {
                            String finalLine = line;
                            javafx.application.Platform.runLater(() ->
                                    daqlog.appendText(finalLine + "\n"));
                        }
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() ->
                                daqlog.appendText(e.getMessage() + "\n"));
                    }
                };
                executor.execute(logout);

            } catch (Exception e) {

            }
        } else if ("Running".equals(Status)) {
            daqlog.appendText("DAQ is already RUNNING , Quit First!!!\n");
        }
    }




    @FXML
    private void clickitemAction(MouseEvent event) {
        if (event.getClickCount() == 1) {
            selectedItem = (Configuration) tabview.getSelectionModel().getSelectedItem();
            changeValue.setText(selectedItem.getValue());
        }
    }
    @FXML
    private void autoReadOutAction() throws IOException {
        if ("Running".equals(Status) && process.isAlive()) {
            bufferedWriter.write("Z" + "\n");
            bufferedWriter.flush();
            bufferedWriter.write("F" + "\n");
            bufferedWriter.flush();
            bufferedWriter.write("Z" + "\n");
            bufferedWriter.flush();
            bufferedWriter.write("H" + "\n");
            bufferedWriter.flush();
            bufferedWriter.write("G" + "\n");
            bufferedWriter.flush();
            bufferedWriter.write("B" + "\n");
            bufferedWriter.flush();
        }
        else {
            daqlog.appendText("DAQ Not Running!!!\n");
        }
    }


}
