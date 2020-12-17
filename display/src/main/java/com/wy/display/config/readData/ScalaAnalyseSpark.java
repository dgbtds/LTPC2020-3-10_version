package com.wy.display.config.readData;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/12/14 15:14
 */

import com.wy.Time.Runtime;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import sumit.BaseJob;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * @program: LTPC2020-3-10_version
 * @description:
 * @author: WuYe
 * @create: 2020-12-14 15:14
 **/
public class ScalaAnalyseSpark extends Service<Dataset<BaseJob.datapck>> implements Runtime {
    private File file;
    private ProgressBar fileProgressBar;
    private TextArea ConfigLog;

    public static void main(String[] args) {
    }

    public ScalaAnalyseSpark(File file, ProgressBar fileProgressBar, TextArea configLog) {
        this.file = file;
        this.fileProgressBar = fileProgressBar;
        ConfigLog = configLog;
    }

    @Override
    protected Task<Dataset<BaseJob.datapck>> createTask() {
        return new Task<Dataset<BaseJob.datapck>>() {
            @Override
            protected Dataset<BaseJob.datapck> call() {
                return scala.sumit.LocalSubmitApp.analyseDataOPT(file.getAbsolutePath(),fileProgressBar,ConfigLog);
            }
        };
    }

    @Override
    public void run() {

    }
}
