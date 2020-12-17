package com.wy.Utils;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/12/14 18:38
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @program: LTPC2020-3-10_version
 * @description:
 * @author: WuYe
 * @create: 2020-12-14 18:38
 **/
public class HDFSUtil {
    private static final String HDFS_URI = "hdfs://hd01:8020/";
    private static final String USER = "hdfs";
    private static FileSystem fileSystem = null;
    static {
        try {
            load();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) throws Exception {
        if (!load()){
            throw new RuntimeException("load fs failed");
        }
        boolean put = HDFSUtil.put("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\CreatData\\100"
                , "hdfs://hd01:8020/user/wy/creat_data/100");
        if (put){
            System.out.println("put trackmap.txt successed");
        }
    }
    /**
     * 构造FileSystem
     *
     * @return true, 构造成果
     * @throws Exception 构造异常
     */
    public static boolean load() throws Exception {
        Configuration conf = new Configuration();
        fileSystem = FileSystem.get(new URI(HDFS_URI), conf, USER);
        return fileSystem != null;
    }
    /**
     * 根据指定的hdfs路径创建hdfs目录
     *
     * @param hdfsDir 指定的hdfs目录
     * @return true, hdfs目录构造完成
     * @throws Exception 构造异常
     */
    public static boolean makeDir(Path hdfsDir) throws Exception {
        return fileSystem.exists(hdfsDir) || fileSystem.mkdirs(hdfsDir);
    }

    /**
     * 本地文件存储到hdfs目录
     *      示例:
     *          String localFile = "D:\\log\\text.demo";
     *          String hdfsDir = "/test/";
     *
     *          // 存储为hdfs://xxx/test/text.demo
     *          put(localFile, hdfsDir);
     *
     * @param localFile 本地文件路径
     * @param hdfsDir hdfs目录
     * @return true, 存储
     * @throws Exception 导入异常
     */
    public static boolean put(String localFile, String hdfsDir) throws Exception {
        Path path = new Path(hdfsDir);
        File local = new File(localFile);
        if (!local.isFile()) {
            return false;
        }
        fileSystem.copyFromLocalFile(new Path(localFile), path);
        return true;
    }
    public static boolean writeFile( String hdfsDir,String data) throws Exception {
        Path path = new Path(hdfsDir);
        FSDataOutputStream fsDataOutputStream = fileSystem.create(path, true);
        if (fsDataOutputStream!=null) {
            fsDataOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
            fsDataOutputStream.close();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * hdfs目录的文件存储到本地
     *      示例:
     *          String localDir = "D:\\log\\";
     *          String fileName = "text.demo";
     *          String hdfsDir = "/test/";
     *
     *          //将hdfs://xxx/test/text.demo存储到本地D:\\log\\
     *          get(hdfsDir, fileName, localDir);
     *
     * @param hdfsDir hdfs目录
     * @param fileName 指定文件
     * @param localDir 本地目录
     * @return true, 存储正常
     * @throws Exception 存储异常
     */
    public static boolean get(String hdfsDir, String fileName, String localDir) throws Exception {
        InputStream in = fileSystem.open(new Path(hdfsDir + "/" + fileName));
        FileOutputStream out = new FileOutputStream(new File(localDir + "/" + fileName));
        IOUtils.copyBytes(in, out, 2048, true);
        return true;
    }

}
