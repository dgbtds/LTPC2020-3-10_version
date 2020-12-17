package com.wy.Utils;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/12/15 16:42
 */

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-12-15 16:42
 **/
public class SSHTool {

    private Connection conn;
    private String ipAddr;
    private Charset charset = StandardCharsets.UTF_8;
    private String userName;
    private String password;

    public SSHTool(String ipAddr, String userName, String password, Charset charset) {
        this.ipAddr = ipAddr;
        this.userName = userName;
        this.password = password;
        if (charset != null) {
            this.charset = charset;
        }
    }

    /**
     * 登录远程Linux主机
     *
     * @return 是否登录成功
     */
    private boolean login() {
        conn = new Connection(ipAddr);

        try {
            // 连接
            conn.connect();
            // 认证
            return conn.authenticateWithPassword(userName, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 执行Shell脚本或命令
     *
     * @param cmds 命令行序列
     * @return 脚本输出结果
     */
    public StringBuilder exec(String cmds) throws IOException {
        InputStream in = null;
        StringBuilder result = new StringBuilder();
        try {
            if (this.login()) {
                // 打开一个会话
                Session session = conn.openSession();
                session.execCommand(cmds);
                in = session.getStdout();
                result = this.processStdout(in, this.charset);
                conn.close();
            }
        } finally {
            if (null != in) {
                in.close();
            }
        }
        return result;
    }

    /**
     * 解析流获取字符串信息
     *
     * @param in      输入流对象
     * @param charset 字符集
     * @return 脚本输出结果
     */
    public StringBuilder processStdout(InputStream in, Charset charset) throws FileNotFoundException {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
//        OutputStream os = new FileOutputStream("./data.txt");
        try {
            int length;
            while ((length = in.read(buf)) != -1) {
//                os.write(buf, 0, c);
                sb.append(new String(buf, 0, length));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    public static void main(String[] args) throws IOException {
        SSHTool tool = new SSHTool("hd01", "root", "online123", StandardCharsets.UTF_8);
        //StringBuilder exec = tool.exec("spark-submit --class submit_Spark_CDH.LtpcDataAnalyse --master yarn --deploy-mode cluster --num-executors 10 --executor-cores 3 --executor-memory 6G ./spark-ltpcData-2.1-release.jar hdfs://hd01:8020/user/wy/creat_data/LaserH_45_1.bin 20 realdata");
        StringBuilder exec = tool.exec("pwd");
        System.out.println(exec);
    }
}
