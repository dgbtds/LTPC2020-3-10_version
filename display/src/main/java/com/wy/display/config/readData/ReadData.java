package com.wy.display.config.readData;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 15:17
 */

import com.wy.Main;
import com.wy.Time.Runtime;
import com.wy.display.config.readXML.ReadConfig;
import com.wy.model.data.SimpleData;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcChannel;
import com.wy.model.decetor.LtpcDetector;
import com.wy.model.decetor.LtpcSourceBoard;
import com.wy.model.decetor.PlaneWithTrack;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import com.wy.model.data.DataSource;
import com.wy.model.data.TimeReference;
import javafx.scene.media.VideoTrack;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:读取数据到对象
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 15:17
 **/
public class ReadData extends Service<DataSource> implements Runtime {
    private static List<LtpcChannel> ltpcChannels;
    private final int rightHeader = 0x1EADC0DE;
    public final int rightTailler = 0x1a2b3c4d;
    private int chargeMax=0;
    private int chargeMin=Integer.MAX_VALUE;
    private double progess;
    private static DataInputStream dis;
    private static String filePath;
    private  static File file;


    public ReadData() {
    }

    public ReadData(DataInputStream dis, String filePath) {
        ReadData.dis = dis;
        ReadData.filePath = filePath;
    }

    public static void main(String[] args) throws Exception {
         file = new File("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\10trigger.bin");
         dis = new DataInputStream(new FileInputStream(file));
        System.out.println("文件大小 ："+file.length());

        //配置
        ReadConfig.setDetectorByXlxs(new File(Main.class.getResource("/detector.xlsx").getFile()));
        LtpcDetector ltpcDetector = ReadConfig.getLtpcDetector();
        ltpcChannels=ltpcDetector.getChannels();
        filePath=file.getAbsolutePath();
        new ReadData().RunT();

    }
    @Override
    public void run() throws Exception {
        ReadData readData = new ReadData();
        DataSource dataSource = readData.readDataSource0(dis);
        System.out.println(dataSource);
    }
    public DataSource readDataSource(DataInputStream dis) throws IOException {
        int triggerCount=1;
        int packageCount=0;
        int everyPackageCount=0;
        HashMap<Integer, Integer> map = new HashMap<>();
        DataSource dataSource = new DataSource();
        ArrayList<SimplifyData> sdList = new ArrayList<>();
        dataSource.setFilePath(filePath);

        TimeReference timeReference = readTimeReferencePck(dis);
        if (timeReference==null){return null;}
        short timeTriggerNum = timeReference.getTriggerNumber();
        //触发数据
        do {
            SimpleData simpleData = readSimpleDataPck(dis);
            if (simpleData==null){break;}
            int flag = simpleData.getFlag();
            if ((flag >> 14)==3){
                TimeReference tr = new TimeReference();
                tr.setHeader(simpleData.getHeader());
                tr.setReservedArea1(simpleData.getReservedArea1());
                tr.setTargetBoardAddress(simpleData.getTargetBoardAddress());
                tr.setPacklength(simpleData.getPacklength());
                tr.setReservedArea2(simpleData.getReservedArea2());
                tr.setSourceBoardAddress(simpleData.getSourceBoardAddress());
                tr.setPackageNunmber(simpleData.getPackageNunmber());
                tr.setType(simpleData.getType());
                tr.setFlag(simpleData.getFlag());

                tr.setTriggerNumber(dis.readShort());
                tr.setReservedArea3(dis.readInt());
                tr.setReservedArea4(dis.readShort());
                tr.setExtTimestamp16(dis.readShort());
                tr.setExtTimestamp32(dis.readInt());

                tr.setTailler(dis.readInt());
                if (timeReference.getTailler()!=rightTailler){
                    System.out.println("triggerNum"+tr.getTriggerNumber()+" : Tailler error");
                    break;
                }
                if (tr.getTriggerNumber()==timeTriggerNum){
                    System.out.println("TriggerNumber not change");
                    break;
                }
                else {
                    triggerCount++;
                    map.put((int)timeTriggerNum,everyPackageCount);
                    everyPackageCount=0;
                    timeTriggerNum=tr.getTriggerNumber();
                }
            }
            else {
                everyPackageCount++;
                processSimpleData(simpleData,sdList);
                packageCount++;
            }

        } while (dis.available()>0);
        map.put((int)timeTriggerNum,everyPackageCount);
        dataSource.setTriggerCount(triggerCount);
        dataSource.setEveryTriggerPckCount(map);
        dataSource.setSdList(sdList);
        dataSource.setChargeMax(chargeMax);
        dataSource.setChargeMin(chargeMin);
        dataSource.setAllPackageCount(packageCount);
        setBoardClick();

        return dataSource;
    }
    public DataSource readDataSource0(DataInputStream dis) throws IOException {
        int triggerCount=1;
        int packageCount=0;
        int everyPackageCount=0;
        HashMap<Integer, Integer> map = new HashMap<>(42);
        DataSource dataSource = new DataSource();
        ArrayList<SimplifyData> sdList = new ArrayList<>();
        dataSource.setFilePath(filePath);

        TimeReference timeReference = readTimeReferencePck0(dis);
        if (timeReference==null){return null;}
        short timeTriggerNum = timeReference.getTriggerNumber();
        //触发数据
        do {
            SimpleData simpleData = readSimpleDataPck0(dis);
            if (simpleData==null){break;}
            int flag = simpleData.getFlag();
            if ((flag >> 14)==3){
                TimeReference tr = new TimeReference();
                tr.setHeader(simpleData.getHeader());
                tr.setSourceBoardAddress(simpleData.getSourceBoardAddress());
                tr.setFlag(simpleData.getFlag());

                tr.setTriggerNumber(dis.readShort());
                dis.skipBytes(6);
                tr.setExtTimestamp16(dis.readShort());
                tr.setExtTimestamp32(dis.readInt());

                tr.setTailler(dis.readInt());
                if (timeReference.getTailler()!=rightTailler){
                    System.out.println("triggerNum"+tr.getTriggerNumber()+" : Tailler error");
                    break;
                }
                if (tr.getTriggerNumber()==timeTriggerNum){
                    System.out.println("TriggerNumber not change");
                    break;
                }
                else {
                    triggerCount++;
                    map.put((int)timeTriggerNum,everyPackageCount);
                    everyPackageCount=0;
                    timeTriggerNum=tr.getTriggerNumber();
                }
            }
            else {
                everyPackageCount++;
                processSimpleData(simpleData,sdList);
                packageCount++;
            }

        } while (dis.available()>0);
        map.put((int)timeTriggerNum,everyPackageCount);
        dataSource.setTriggerCount(triggerCount);
        dataSource.setEveryTriggerPckCount(map);
        dataSource.setSdList(sdList);
        dataSource.setChargeMax(chargeMax);
        dataSource.setChargeMin(chargeMin);
        dataSource.setAllPackageCount(packageCount);
        setBoardClick();

        return dataSource;
    }
    //处理单个数据包
    private void processSimpleData(SimpleData simpleData, ArrayList<SimplifyData> sdList){
        int flag = simpleData.getFlag();
        int channelId = (flag>>8) &0x3f;
        int channelNum = getChannelNum( channelId, simpleData.getSourceBoardAddress());
        LtpcChannel ltpcChannel = ltpcChannels.get(channelNum);
        ltpcChannel.setClickCount(ltpcChannel.getClickCount()+1);
        PlaneWithTrack[] planeWithTracks = ltpcChannel.getPlaneWithTracks();
        short[] shorts = simpleData.getSampleData();
        int piece=planeWithTracks.length;
        int Size=shorts.length/piece+1;
        int start=0;
        for (int i=0;i<piece;i++) {
            int max = findMax(shorts, start, start+Size*(i+1));
            SimplifyData sd = new SimplifyData();
            sd.setLtpcChannel(ltpcChannel);
            sd.setChannelNum(channelNum);
            sd.setTriggerNum(simpleData.getTriggerNumber());
            sd.setTrackerNum(planeWithTracks[i].getTracker().trackerNum);
            sd.setCharge((int) shorts[max]);
            sd.setPlaneNum(planeWithTracks[i].getPlane().planeNum);
            if (simpleData.getTriggerNumber()==0){
                sd.setShorts(simpleData.getSampleData());
            }
            sdList.add(sd);
            start+=Size;
        }
    }
    private int  getChannelNum( int channelId, int sourceBoard){
        if (ltpcChannels!=null){
            List<LtpcChannel> collect = ltpcChannels.stream().filter(s -> s.getChannelId() == channelId && s.getSourceBoardNum() == sourceBoard).collect(Collectors.toList());
            if (collect.size()>1){
                throw new RuntimeException("多个符合");
            }
            else if (collect.size()==1){
                return collect.get(0).getPid();
            }
            else {
                    throw new RuntimeException("未找到");
            }
        }
        throw new RuntimeException("ltpcChannels==null");
    }
    private int findMax(short[] shorts, int start, int end) {
        int MIndex=start;
        if (end>shorts.length){
            end=shorts.length;
        }
        for(int i=start+1;i<end;i++){
            if(shorts[i]>shorts[MIndex]){
                MIndex=i;
            }
        }
        if (shorts[MIndex]>chargeMax){
            chargeMax=shorts[MIndex];
        }
        if (shorts[MIndex]<chargeMin){
            chargeMin=shorts[MIndex];
        }
        return MIndex;
    }

    private TimeReference readTimeReferencePck(DataInputStream dis) throws IOException {

        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("TimeReference header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("TimeReference no header error");
                return null;
            }
        }
        TimeReference timeReference = new TimeReference();
        timeReference.setHeader(readInt);
        timeReference.setReservedArea1(dis.readByte());
        timeReference.setTargetBoardAddress(dis.readByte());
        timeReference.setPacklength(dis.readShort());
        timeReference.setReservedArea2(dis.readByte());
        timeReference.setSourceBoardAddress(dis.readUnsignedByte());
        timeReference.setPackageNunmber(dis.readByte());
        timeReference.setType(dis.readByte());

        int flag = dis.readUnsignedShort();
        timeReference.setFlag(flag);
        if ( (flag >> 14)!=3)
        { System.out.println("TimeReference flag error");return null; }

        timeReference.setTriggerNumber(dis.readShort());
        timeReference.setReservedArea3(dis.readInt());
        timeReference.setReservedArea4(dis.readShort());
        timeReference.setExtTimestamp16(dis.readShort());
        timeReference.setExtTimestamp32(dis.readInt());

        timeReference.setTailler(dis.readInt());
        if (timeReference.getTailler()!=rightTailler) { System.out.println("TimeReference Tailler error");return null; }
        return timeReference;
    }
    private TimeReference readTimeReferencePck0(DataInputStream dis) throws IOException {

        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("TimeReference header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("TimeReference no header error");
                return null;
            }
        }
        TimeReference timeReference = new TimeReference();
        timeReference.setHeader(readInt);
        dis.skipBytes(5);
        timeReference.setSourceBoardAddress(dis.readUnsignedByte());
       dis.skipBytes(2);

        int flag = dis.readUnsignedShort();
        timeReference.setFlag(flag);
        if ( (flag >> 14)!=3)
        { System.out.println("TimeReference flag error");return null; }

        timeReference.setTriggerNumber(dis.readShort());
        dis.skipBytes(6);
        timeReference.setExtTimestamp16(dis.readShort());
        timeReference.setExtTimestamp32(dis.readInt());

        timeReference.setTailler(dis.readInt());
        if (timeReference.getTailler()!=rightTailler) { System.out.println("TimeReference Tailler error");return null; }
        return timeReference;
    }
    private SimpleData readSimpleDataPck(DataInputStream dis) throws IOException {
        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("SimpleData header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("SimpleData no header error");
                return null;
            }
        }
        SimpleData simpleData = new SimpleData();
        simpleData.setHeader(readInt);
        simpleData.setReservedArea1(dis.readByte());
        simpleData.setTargetBoardAddress(dis.readByte());
        simpleData.setPacklength(dis.readShort());
        simpleData.setReservedArea2(dis.readByte());
        simpleData.setSourceBoardAddress( dis.readUnsignedByte());
        simpleData.setPackageNunmber(dis.readByte());
        simpleData.setType(dis.readByte());

        int flag = dis.readUnsignedShort();
        simpleData.setFlag(flag);
        if ((flag >> 14)==3) { return simpleData; }
        if ((flag >> 14)!=2) { System.out.println("SimpleData flag error");return null; }

        simpleData.setPackageNumber(dis.readShort());
        simpleData.setTriggerSource(dis.readInt());
        simpleData.setTriggerNumber(dis.readShort());
        simpleData.setExtTimestamp16(dis.readShort());
        simpleData.setExtTimestamp32(dis.readInt());
        int simpleLength = (flag &0xff) * (16);
        short[] simpleDatas = new short[simpleLength];
        for(int i=0;i<simpleDatas.length;i++){
            simpleDatas[i]=(short) dis.readUnsignedShort();
        }
        simpleData.setSampleData(simpleDatas);

        simpleData.setTailler(dis.readInt());
        if (simpleData.getTailler()!=rightTailler) { System.out.println("SimpleData Tailler error");return null; }
        return simpleData;

    }
    private SimpleData readSimpleDataPck0(DataInputStream dis) throws IOException {
        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("SimpleData header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("SimpleData no header error");
                return null;
            }
        }
        SimpleData simpleData = new SimpleData();
        simpleData.setHeader(readInt);
        dis.skipBytes(5);
        simpleData.setSourceBoardAddress( dis.readUnsignedByte());
        dis.skipBytes(2);

        int flag = dis.readUnsignedShort();
        simpleData.setFlag(flag);
        if ((flag >> 14)==3) { return simpleData; }
        if ((flag >> 14)!=2) { System.out.println("SimpleData flag error");return null; }

        dis.skipBytes(6);
        simpleData.setTriggerNumber(dis.readShort());
        simpleData.setExtTimestamp16(dis.readShort());
        simpleData.setExtTimestamp32(dis.readInt());
//        if (simpleData.getTriggerNumber()==0) {
            int simpleLength = (flag &0xff) * (16);
            short[] simpleDatas = new short[simpleLength];
            for(int i=0;i<simpleDatas.length;i++){
                simpleDatas[i]=(short) dis.readUnsignedShort();
            }
            simpleData.setSampleData(simpleDatas);
//        }

        simpleData.setTailler(dis.readInt());
        if (simpleData.getTailler()!=rightTailler) { System.out.println("SimpleData Tailler error");return null; }
        return simpleData;

    }
    private Boolean findRightHeader (DataInputStream dis,int readInt) throws IOException {
        while (dis.available() > 0) {
            byte b = dis.readByte();
            int read = (readInt << 8) | b;
            if (read == rightHeader) {
                return true;
            }
        }
        return false;
    }

    public double getProgess() {
        return progess;
    }

    public void setProgess(double progess) {
        this.progess = progess;
    }


    @Override
    protected Task<DataSource> createTask() {
        return new Task<DataSource>() {
            @Override
            protected DataSource call() throws Exception {
                long start = System.currentTimeMillis();
                int triggerCount=1;
                int packageCount=0;
                int everyPackageCount=0;
                int length=dis.available();
                HashMap<Integer, Integer> map = new HashMap<>();
                DataSource dataSource = new DataSource();
                ArrayList<SimplifyData> sdList = new ArrayList<>();
                dataSource.setFilePath(filePath);

                TimeReference timeReference = readTimeReferencePck(dis);
                if (timeReference==null){return null;}
                short timeTriggerNum = timeReference.getTriggerNumber();
                //触发数据
                do {
                    SimpleData simpleData = readSimpleDataPck(dis);
                    if (simpleData==null){break;}
                    int flag = simpleData.getFlag();
                    if ((flag >> 14)==3){
                        TimeReference tr = new TimeReference();
                        tr.setHeader(simpleData.getHeader());
                        tr.setReservedArea1(simpleData.getReservedArea1());
                        tr.setTargetBoardAddress(simpleData.getTargetBoardAddress());
                        tr.setPacklength(simpleData.getPacklength());
                        tr.setReservedArea2(simpleData.getReservedArea2());
                        tr.setSourceBoardAddress(simpleData.getSourceBoardAddress());
                        tr.setPackageNunmber(simpleData.getPackageNunmber());
                        tr.setType(simpleData.getType());
                        tr.setFlag(simpleData.getFlag());

                        tr.setTriggerNumber(dis.readShort());
                        tr.setReservedArea3(dis.readInt());
                        tr.setReservedArea4(dis.readShort());
                        tr.setExtTimestamp16(dis.readShort());
                        tr.setExtTimestamp32(dis.readInt());

                        tr.setTailler(dis.readInt());
                        if (timeReference.getTailler()!=rightTailler){
                            System.out.println("triggerNum"+tr.getTriggerNumber()+" : Tailler error");
                            break;
                        }
                        if (tr.getTriggerNumber()==timeTriggerNum){
                            System.out.println("TriggerNumber not change");
                            break;
                        }
                        else {
                            triggerCount++;
                            map.put((int)timeTriggerNum,everyPackageCount);
                            everyPackageCount=0;
                            timeTriggerNum=tr.getTriggerNumber();
                        }
                    }
                    else {
                        everyPackageCount++;
                        processSimpleData(simpleData,sdList);
                        packageCount++;
                        progess=length-dis.available();
                        updateProgress(progess,length);
                    }

                } while (dis.available()>0);
                map.put((int)timeTriggerNum,everyPackageCount);
                dataSource.setTriggerCount(triggerCount);
                dataSource.setEveryTriggerPckCount(map);
                dataSource.setSdList(sdList);
                dataSource.setChargeMax(chargeMax);
                dataSource.setChargeMin(chargeMin);
                dataSource.setAllPackageCount(packageCount);
                setBoardClick();
                long end = System.currentTimeMillis();
                System.out.println("Time used :"+(end-start));

                return dataSource;
            }
        };
    }
    private void  setBoardClick(){
        HashMap<Integer, LtpcSourceBoard> map = LtpcDetector.SourceBoardMap;
        map.forEach((n,b)->{
            int clickCount= b.getLtpcChannels().stream().mapToInt(LtpcChannel::getClickCount).sum();
            b.setClickCount(clickCount);
        });
    }

    public static void setLtpcChannels(List<LtpcChannel> ltpcChannels) {
        ReadData.ltpcChannels = ltpcChannels;
    }

}
