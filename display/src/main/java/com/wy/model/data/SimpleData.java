package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 15:09
 */

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:采样数据包
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 15:09
 **/
public class SimpleData extends GeneralData{

    private int triggerSource;//最后三位是触发源
    private short triggerNumber;//外触发计数
    private long extTimestamp16;//外触发时间前16位
    private long extTimestamp32;//外触发时间后32位
    private short[] sampleData;
    private  int tailler;

    public int getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(int triggerSource) {
        this.triggerSource = triggerSource;
    }

    public short getTriggerNumber() {
        return triggerNumber;
    }

    public void setTriggerNumber(short triggerNumber) {
        this.triggerNumber = triggerNumber;
    }

    public long getExtTimestamp16() {
        return extTimestamp16;
    }

    public void setExtTimestamp16(long extTimestamp16) {
        this.extTimestamp16 = extTimestamp16;
    }

    public long getExtTimestamp32() {
        return extTimestamp32;
    }

    public void setExtTimestamp32(long extTimestamp32) {
        this.extTimestamp32 = extTimestamp32;
    }

    public short[] getSampleData() {
        return sampleData;
    }

    public void setSampleData(short[] sampleData) {
        this.sampleData = sampleData;
    }

    public int getTailler() {
        return tailler;
    }

    public void setTailler(int tailler) {
        this.tailler = tailler;
    }
}
