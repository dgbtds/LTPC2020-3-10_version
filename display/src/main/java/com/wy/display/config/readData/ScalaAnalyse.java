package com.wy.display.config.readData;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/29 15:35
 */

import com.wy.Time.Runtime;
import com.wy.model.data.DataSource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-04-29 15:35
 **/
public class ScalaAnalyse extends Service<DataSource> implements Runtime {
    private String localfile;
    private String outfile;
    private ProgressBar fileProgressBar;
    private TextArea ConfigLog;

    public ScalaAnalyse(String localfile, String outfile, ProgressBar fileProgressBar, TextArea ConfigLog) {
        this.localfile = localfile;
        this.outfile = outfile;
        this.fileProgressBar=fileProgressBar;
        this.ConfigLog=ConfigLog;
    }

    @Override
    public void run() throws Exception {

    }

    @Override
    protected Task<DataSource> createTask() {
        return new Task<DataSource>() {
            @Override
            protected DataSource call() throws Exception {
                return  scala.sumit.LocalSubmitApp.analyseData(localfile,outfile,fileProgressBar,ConfigLog);
            }
        };
    }
}
