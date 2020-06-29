package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 16:51
 */

import scala.Int;
import scala.Serializable;
import scala.collection.immutable.Map;
import scala.sumit.AnalyseData;


/**
 * @program: LTPC2020-3-4-version2
 * @description:数据源模型
 * @author: WuYe
 * @create: 2020-03-07 16:51
 **/
public class DataSource implements Serializable {
    private String filePath;
    private Long allPackageCount;
    private int rawDataPackageCount;
    private int triggerCount;//触发数量
    private String trigger_pckCount;

    public String getTrigger_pckCount() {
        return trigger_pckCount;
    }

    public void setTrigger_pckCount(String trigger_pckCount) {
        this.trigger_pckCount = trigger_pckCount;
    }

    public int getRawDataPackageCount() {
        return rawDataPackageCount;
    }

    public void setRawDataPackageCount(int rawDataPackageCount) {
        this.rawDataPackageCount = rawDataPackageCount;
    }

    public Long getAllPackageCount() {
        return allPackageCount;
    }

    public void setAllPackageCount(Long allPackageCount) {
        this.allPackageCount = allPackageCount;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\ndataFile path：" + filePath);
        stringBuilder.append("\nrawDataPackageCount Count：" + rawDataPackageCount);
        stringBuilder.append("\nAnalyseDataPackage Count：" + allPackageCount);
        stringBuilder.append("\nTrigger Count：" + triggerCount);
        stringBuilder.append("\nData rate" + (double) allPackageCount / (double) (triggerCount * 20) + "\n");
        stringBuilder.append("\nEveryPackage Count： \n");
        return stringBuilder.toString()+trigger_pckCount;
    }
}
