package com.wy.display.config.readXML;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/4 11:26
 */

import com.wy.Main;
import com.wy.Utils.HDFSUtil;
import com.wy.model.decetor.*;
import org.apache.poi.ss.usermodel.*;
import scala.collection.mutable.StringBuilder;
import scala.math.Ordering;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @program: LTPC 2020-3-4
 * @description:读取探测器模型数据
 * @author: WuYe
 * @create: 2020-03-04 11:26
 **/
public class ReadConfig {
    private static LtpcDetector ltpcDetector;

    public static LtpcDetector getLtpcDetector() {
        return ltpcDetector;
    }

    public <T> List<T> parseFromExcel(File file, Class<T> aimClass) {
        return parseFromExcel(file, 0, aimClass);
    }

    public static void main(String[] args) throws Exception {
        ReadConfig.setDetectorByXlxs(new File("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\detector.xlsx"));
    }

    @SuppressWarnings("deprecation")
    public static <T> List<T> parseFromExcel(File file, int firstIndex, Class<T> aimClass) {
        List<T> result = new ArrayList<T>();
        try {
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fis);
            //对excel文档的第一页,即sheet1进行操作
            Sheet sheet = workbook.getSheetAt(0);
            int lastRaw = sheet.getLastRowNum();
            for (int i = firstIndex; i <= lastRaw; i++) {
                //第i行
                Row row = sheet.getRow(i);
                T parseObject = aimClass.newInstance();
                Field[] fields = aimClass.getDeclaredFields();
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    //第j列
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    //很重要的一行代码,如果不加,像12345这样的数字是不会给你转成String的,只会给你转成double,而且会导致cell.getStringCellValue()报错
                    cell.setCellType(CellType.STRING);
                    String cellContent = cell.getStringCellValue();
                    cellContent = "".equals(cellContent) ? "0" : cellContent;
                    if (type.equals(String.class)) {
                        field.set(parseObject, cellContent);
                    } else if (type.equals(char.class) || type.equals(Character.class)) {
                        field.set(parseObject, cellContent.charAt(0));
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {
                        field.set(parseObject, Integer.parseInt(cellContent));
                    } else if (type.equals(long.class) || type.equals(Long.class)) {
                        field.set(parseObject, Long.parseLong(cellContent));
                    } else if (type.equals(float.class) || type.equals(Float.class)) {
                        field.set(parseObject, Float.parseFloat(cellContent));
                    } else if (type.equals(double.class) || type.equals(Double.class)) {
                        field.set(parseObject, Double.parseDouble(cellContent));
                    } else if (type.equals(short.class) || type.equals(Short.class)) {
                        field.set(parseObject, Short.parseShort(cellContent));
                    } else if (type.equals(byte.class) || type.equals(Byte.class)) {
                        field.set(parseObject, Byte.parseByte(cellContent));
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        field.set(parseObject, Boolean.parseBoolean(cellContent));
                    }
                }
                result.add(parseObject);
            }
            fis.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occured when parsing object from Excel");
        }
        return result;
    }

    public static void setDetectorByXlxs(File file) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        ltpcDetector = new LtpcDetector();
        List<LtpcChannel> listChannels = parseFromExcel(file, 1, LtpcChannel.class);
        LtpcDetector.channels = listChannels;
        listChannels.forEach(c -> {
            c.computeTracks();
            if (LtpcDetector.SourceBoardChannelsMap.containsKey(c.getSourceBoardNum())) {
                LtpcDetector.SourceBoardChannelsMap.get(c.getSourceBoardNum()).addList(c);
            } else {
                LtpcSourceBoard ltpcSourceBoard = new LtpcSourceBoard(c.getArea(), c.getSourceBoardNum());
                ltpcSourceBoard.addList(c);
                LtpcDetector.SourceBoardChannelsMap.put(c.getSourceBoardNum(), ltpcSourceBoard);
            }
            if (c.getSourceBoardNum() != 0) {
                if (LtpcDetector.sourceBoardChannelIdChannelMap.containsKey(c.getSourceBoardNum() + "," + c.getChannelId())) {
                    throw new RuntimeException(c.getSourceBoardNum() + "," + c.getChannelId() + " 重复");
                } else {
                    LtpcDetector.sourceBoardChannelIdChannelMap.put(c.getSourceBoardNum() + "," + c.getChannelId(), c);
                }
            }
            stringBuilder.append(c.getSourceBoardNum() +" "+c.getChannelId()+" ");
            for (int i = 0; i < c.getTrackNums().length - 1; i++) {
                stringBuilder.append(c.getTrackNums()[i] +" ");
            }
            stringBuilder.append(c.getTrackNums()[c.getTrackNums().length - 1] + "\r\n");
        });
        HDFSUtil.writeFile("hdfs://hd01:8020/user/wy/trackmap.txt", stringBuilder.toString());
    }
}
