package com.wy.Utils;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/12/15 17:58
 */

import javafx.scene.control.TextArea;

import java.sql.*;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-12-15 17:58
 **/
public class HiveUtil {
    private static Connection con=null;
    private static String connectionURL = "jdbc:hive2://hd01:10000/ltpc";
    private static String drivername = "org.apache.hive.jdbc.HiveDriver";
    private static String username = "";
    private static String password = "";
    private TextArea ConfigLog;
    static {
        try {
            Class.forName(drivername);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        try {
             con = DriverManager.getConnection(connectionURL, username, password);
        }
        catch(SQLException se)
        {
            se.printStackTrace();
        }
    }

    public HiveUtil(TextArea configLog) {
        ConfigLog = configLog;
    }

    public ResultSet SQLResult(String sql) throws SQLException {
        if (con != null) {
//            String finalSql = sql;
//            javafx.application.Platform.runLater(() ->
//            {
//                ConfigLog.appendText("\nHive jdbc Connected!\n");
//                ConfigLog.appendText("Running: " + finalSql);
//            });
        } else {
            ConfigLog.appendText("\nHive jdbc Not Connected!\n");
            return null;
        }
        Statement stmt = con.createStatement();
        return stmt.executeQuery(sql);
    }
    public static void main(String[] args) throws SQLException {
        ResultSet resultSet = new HiveUtil(null).SQLResult("select board,channelid,max_point from ltpctable100 where `trigger`=1 and tracker=37");
            System.out.println("board,channelid,max_point");
        while (resultSet.next()){
            System.out.println(resultSet.getInt(1)+" , "+resultSet.getInt(2)+" , "+resultSet.getInt(3));
        }

    }
}
