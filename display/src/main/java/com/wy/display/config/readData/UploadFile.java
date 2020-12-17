package com.wy.display.config.readData;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/12/14 21:20
 */

import com.wy.Time.Runtime;
import com.wy.Utils.HDFSUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.sql.CallableStatement;

/**
 * @program: LTPC2020-3-10_version
 * @description:
 * @author: WuYe
 * @create: 2020-12-14 21:20
 **/
public class UploadFile extends Service<Boolean> implements Runtime {
    private File file;

    public UploadFile(File file) {
        this.file = file;
    }

    @Override
    public void run() throws Exception {

    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return HDFSUtil.put(file.getAbsolutePath(), "hdfs://hd01:8020/user/wy/creat_data/" + file.getName());
            }
        };
    }
}
