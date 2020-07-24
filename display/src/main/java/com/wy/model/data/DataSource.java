package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 16:51
 */

import com.wy.model.decetor.LtpcDetector;
import scala.Serializable;

import java.util.*;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:数据源模型
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 16:51
 **/
public class DataSource implements Serializable {
    private String filePath;
    private int allPackageCount;
    private int rawDataPackageCount;
    private int triggerCount;//触发数量
    private Map<Integer,Integer> everyTriggerPckCount;//每个触发包的数量
    private Map<Integer,Long> everyTriggerTime;//每个触发包的时间包
    private List<SimplifyData> sdList;
    private int chargeMax;
    private int chargeMin;

    public int getRawDataPackageCount() {
        return rawDataPackageCount;
    }

    public void setRawDataPackageCount(int rawDataPackageCount) {
        this.rawDataPackageCount = rawDataPackageCount;
    }

    public Map<Integer, Long> getEveryTriggerTime() {
        return everyTriggerTime;
    }

    public void setEveryTriggerTime(Map<Integer, Long> everyTriggerTime) {
        this.everyTriggerTime = everyTriggerTime;
    }

    public int getAllPackageCount() {
        return allPackageCount;
    }

    public void setAllPackageCount(int allPackageCount) {
        this.allPackageCount = allPackageCount;
    }

    public Map<Integer, Integer> getEveryTriggerPckCount() {
        return everyTriggerPckCount;
    }

    public void setEveryTriggerPckCount(Map<Integer, Integer> everyTriggerPckCount) {
        this.everyTriggerPckCount = everyTriggerPckCount;
    }

    public int getChargeMax() {
        return chargeMax;
    }

    public void setChargeMax(int chargeMax) {
        this.chargeMax = chargeMax;
    }

    public int getChargeMin() {
        return chargeMin;
    }

    public void setChargeMin(int chargeMin) {
        this.chargeMin = chargeMin;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }

    public List<SimplifyData> getSdList() {
        return sdList;
    }

    public void setSdList(List<SimplifyData> sdList) {
        this.sdList = sdList;
    }
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\ndataFile path："+filePath);
        stringBuilder.append("\nrawDataPackageCount Count："+rawDataPackageCount);
        stringBuilder.append("\nAnalyseDataPackage Count："+allPackageCount);
        stringBuilder.append("\nTrigger Count："+triggerCount);
        stringBuilder.append("\nMax pointEnergy ："+chargeMax);
        stringBuilder.append("\nMin pointEnergy："+chargeMin+"\n");
        stringBuilder.append("\nData rate"+(double)allPackageCount/(double)(triggerCount*20)+"\n");
        stringBuilder.append("\nEveryPackage Count： \n");
        LtpcDetector.SourceBoardMap.forEach((k,v)->{
            stringBuilder.append("SourceBoardId: "+k+" , ClickCount: "+v.getClickCount()+"\n");
        });
        everyTriggerPckCount.forEach( (k,v)->{
            stringBuilder.append("      Trigger: "+k+" , packageCount: "+v+"\n");
        } );
        return stringBuilder.toString();
    }
}
