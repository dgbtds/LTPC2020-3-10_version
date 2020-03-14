package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 16:51
 */

import java.util.ArrayList;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:数据源模型
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 16:51
 **/
public class DataSource {
    private String filePath;
    private int triggerCount;//触发数量
    private ArrayList<SimplifyData> sdList;
    private int chargeMax;
    private int chargeMin;

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

    public ArrayList<SimplifyData> getSdList() {
        return sdList;
    }

    public void setSdList(ArrayList<SimplifyData> sdList) {
        this.sdList = sdList;
    }
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n数据源路径："+filePath);
        stringBuilder.append("\n触发数："+triggerCount+"\n数据包简化数据：\n");
        stringBuilder.append("\n电荷最大值："+chargeMax);
        stringBuilder.append("\n电荷最小值："+chargeMin+"\n");
        if (sdList!=null) {
            sdList.forEach(simplifyData -> {
                stringBuilder.append(simplifyData.toString());
            });
        }
        return stringBuilder.toString();
    }
}
