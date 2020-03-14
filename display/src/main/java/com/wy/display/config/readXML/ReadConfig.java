package com.wy.display.config.readXML;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/4 11:26
 */

import com.wy.Main;
import com.wy.model.decetor.*;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: LTPC 2020-3-4
 *
 * @description:读取探测器模型数据
 *
 * @author: WuYe
 *
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
        ReadConfig.setDetectorByXlxs(new File(Main.class.getResource("/detector.xlsx").getFile()));
        List<LtpcChannel> channels = ltpcDetector.getChannels();
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
                Row row =  sheet.getRow(i);
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
        } catch ( Exception e) {
            e.printStackTrace();
            System.err.println("An error occured when parsing object from Excel" );
        }
        return result;
    }
    private static int  getSourecBoardNum(int area,int board){
        if (area==0){
            return board;
        }
        else {
            return  (area-1)*6+7+board;
        }
    }
    public static void setDetectorByXlxs(File file) throws Exception {
        //参数里的5表示有效行数从第5行开始
        List<LtpcArea> listAeras=new ArrayList<LtpcArea>();
        List<LtpcBoard> listBoards=new ArrayList<LtpcBoard>();
        List<LtpcBoard> sublistBoards=new ArrayList<LtpcBoard>();
        List<LtpcChannel> listChannels = parseFromExcel(file, 1, LtpcChannel.class);
        listChannels.forEach(c->{
            c.setPlaneWithTracks(PlaneWithTrack.getPlaneWithTrack(c));
        });
        int board=-1,area=-1;
        LtpcArea ltpcArea =null;
        LtpcBoard ltpcBoard =null;
        for (int i = 0; i < listChannels.size(); i++) {
            //由内向外生成
            if(listChannels.get(i).getBoard()!=board) {
                board=listChannels.get(i).getBoard();
                //System.err.println("－－－新的board数："+board);
                //老的放进去（每次ｂｏａｒｄ变化了才放进去）
                if(ltpcBoard!=null) {
                    ltpcArea.addList(ltpcBoard);
                }
                //生成新的
                ltpcBoard=new LtpcBoard(area,board);
                sublistBoards.add(ltpcBoard);
                listBoards.add(ltpcBoard);
            }

            if(listChannels.get(i).getArea()!=area	) {
                area=listChannels.get(i).getArea();
                //System.err.println("新的area数："+area);
                ltpcArea= new LtpcArea(area);
                listAeras.add(ltpcArea);
            }

            ltpcBoard.addList(listChannels.get(i));
        }
        ltpcArea.addList(ltpcBoard);
        ltpcDetector=new LtpcDetector(listAeras, listBoards, listChannels);
    }
}
