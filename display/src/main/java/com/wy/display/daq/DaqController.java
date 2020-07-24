package com.wy.display.daq;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/7/17 19:22
 */

import com.wy.model.data.Configuration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
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
    private Button enableBoard;
    @FXML
    private Button close;
    @FXML
    private ComboBox<Integer> cb;
    @FXML
    private TextArea daqlog;
    private Configuration selectedItem = null;
    public String Status = "Waitting";
    private String[] boards = {"254", "12", "14", "13", "18", "28",
            "31", "15", "16", "17", "19", "5",
            "2", "32", "27", "11", "26", "30", "33", "6"};
    private String[] cmds = new String[25];


    private String Adir = null;

    private TreeMap<String, String> treemap = new TreeMap<>();
    private ObservableList<Configuration> entrys = FXCollections.observableArrayList();
    private String[] keyArr = null;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private Process process = null;
    private HashMap<String, File> filemap = new HashMap();
    private int enableboardnum = 0;
    private Configuration enableConf;
    private Properties prop = new Properties();

    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10), r -> {
        Thread thread = new Thread(r);
        thread.setName("DAQ Thread");
        return thread;
    });


    @FXML
    private void initialize() {
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
                            daqlog.appendText("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
                            daqlog.appendText("you choose command------>" + newValue + "\n");
                            daqlog.appendText("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
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
            StringJoiner stringJoiner = new StringJoiner(" , ");
            for (int i = 0; i < newValue; i++) {
                stringJoiner.add(boards[i]);
            }
            enableboardnum = newValue;
            daqlog.appendText("you choose enable boards: " + stringJoiner.toString() + "\n");
            daqlog.appendText("Please Enable Board \n");
        });

        //disable button
        cb.setDisable(true);
        runDaq.setDisable(true);
        enableBoard.setDisable(true);
        saveChange.setDisable(true);
        close.setDisable(true);

    }

    @FXML
    private void saveChangeAction() {
        String value = changeValue.getText();
        if (value.matches(" *")) {
            daqlog.appendText("Configuration cant set null \n");
            return;
        }
        if (selectedItem != null) {
            if (!value.equals(selectedItem.getValue())) {
                if (!"EnableBoardNum".equals(selectedItem.getKey())) {
                    daqlog.appendText("change --" + selectedItem.getKey() + "--from " + selectedItem.getValue() + "--to--" + value + "\n");
                    selectedItem.setValue(value);
                    prop.setProperty(selectedItem.getKey(), value);
                    setCmds();
                } else {
                    daqlog.appendText("cant change EnableBoardNum!!!\n");
                }
            }
        } else {
            daqlog.appendText("not choose configuration!!!\n");
        }
    }

    @FXML
    public void closeAction() throws InterruptedException, IOException {
        File file = filemap.get("DefaultConfig.prop");
        if (file == null) {
            daqlog.appendText("DefaultConfig.prop not exist\n");
            return;
        }
        saveDefaultConfig(file);
        if (process != null && process.isAlive()) {
            process.destroy();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
        Platform.exit();
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
                enableBoard.setDisable(false);
                saveChange.setDisable(false);
                close.setDisable(false);
            }
        }
    }

    @FXML
    private void enableBoardAction() throws IOException {
        if (enableboardnum < boards.length) {
            daqlog.appendText("------------->start enable boards\n");
            enableConf.setValue(enableboardnum + "");
            prop.setProperty("EnableBoardNum", "" + enableboardnum);
            setCmds();

            //修改board文件
            setBoardFile();
            //修改Config_multiboard.txt
            setMulboardFile();
            //使能小板
            for (int i = 0; i < enableboardnum; i++) {
                cmds[5 + i] = "15";
            }
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
                    String[] s = line.split(" *");
                    List<String> stringList = Arrays.stream(s).filter(c -> !"".equals(c)).collect(Collectors.toList());
                    if (stringList.size() == 2) {
                        stringList.set(1, "[" + (enableboardnum + 1) + "]");
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
                    if (i <= enableboardnum) {
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
        String num = prop.getProperty("EnableBoardNum");

        treemap.put("daqExe", prop.getProperty("daqExe"));
        treemap.put("singleFileTime", prop.getProperty("singleFileTime"));
        treemap.put("singleFileNum", prop.getProperty("singleFileNum"));
        treemap.put("MoldingTime", prop.getProperty("MoldingTime"));
        treemap.put("Gain", prop.getProperty("Gain"));
        treemap.put("EnableBoardNum", num);

        setCmds();

        keyArr = new String[treemap.keySet().size()];
        treemap.keySet().toArray(keyArr);


        key.setCellValueFactory(new PropertyValueFactory<>("key"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        treemap.forEach((k, v) -> {
            if ("EnableBoardNum".equals(k)) {
                enableConf = new Configuration(k, v);
                entrys.add(enableConf);
            } else {
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


        for (int i = 0; i < boards.length; i++) {
            if (i <= Integer.parseInt(prop.getProperty("EnableBoardNum"))) {
                cmds[5 + i] = "15";
            } else {
                cmds[5 + i] = "0";
            }
        }
    }

    private void saveDefaultConfig(File saveFile) throws IOException {
        FileWriter fileWriter = new FileWriter(saveFile);
        prop.store(fileWriter, "save config");
        fileWriter.close();
    }

    @FXML
    private void runDaqAction() {
        if (Status.equals("Waitting")) {
            try {
                for (String s : cmds) {
                    if (s == null) {
                        daqlog.appendText("configuration not complete!!!");
                        return;
                    }
                }

                process = new ProcessBuilder(cmds).start();

                if (process.isAlive()) {
                    Status = "Running";
                    javafx.application.Platform.runLater(() ->
                            {
                                daqlog.appendText("RUN DAQ Config Info:\n" + String.join("-", cmds) + "\n");
                                daqlog.appendText("\n############### LtpcDaq is Start #################\n\n");
                            }
                    );
                }

                //获取进程输入流
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                //获取进程输出流
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Runnable logout = () -> {

                    String line;
                    try {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String date = df.format(new Date());
                        File file = filemap.get("DefaultConfig.prop");
                        if (file == null) {
                            daqlog.appendText("DefaultConfig.prop not exist\n");
                            return;
                        }
                        String savePath = file.getParentFile().getParent() + "\\Data_bin_files\\ConfigInfo_RUN_" + date + ".txt";
                        saveDefaultConfig(new File(savePath));
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
        } else if (Status.equals("Running")) {
            daqlog.appendText("DAQ is already RUNNING , Quit First!!!\n");
        }
    }


    public static File Fileopen(String dir) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有类型", "*.*"),
                new FileChooser.ExtensionFilter("configfile", "*.txt")
        );
        if (dir != null) {
            fileChooser.setInitialDirectory(new File(dir));
        }
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            Runtime runtime = Runtime.getRuntime();
            String cmd = String.format("cmd.exe /c start %s", file.getAbsolutePath());
            System.out.println(cmd);
            runtime.exec(cmd);
        }
        return file;
    }

    @FXML
    private void clickitemAction(MouseEvent event) {
        if (event.getClickCount() == 1) {
            selectedItem = (Configuration) tabview.getSelectionModel().getSelectedItem();
            changeValue.setText(selectedItem.getValue());
        }
    }


}
