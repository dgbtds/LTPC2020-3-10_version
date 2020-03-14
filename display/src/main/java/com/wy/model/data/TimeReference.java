package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 14:54
 */

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:每次trigger第一个时间标定包
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 14:54
 **/
public class TimeReference extends GeneralData{
    private short triggerNumber;//外触发计数
    private int reservedArea3;
    private short reservedArea4;//
    private short extTimestamp16;//外触发时间前16位
    private int extTimestamp32;//外触发时间后32位
    private  int tailler;

    public short getTriggerNumber() {
        return triggerNumber;
    }

    public void setTriggerNumber(short triggerNumber) {
        this.triggerNumber = triggerNumber;
    }

    public int getReservedArea3() {
        return reservedArea3;
    }

    public void setReservedArea3(int reservedArea3) {
        this.reservedArea3 = reservedArea3;
    }

    public long getReservedArea4() {
        return reservedArea4;
    }

    public void setReservedArea4(short reservedArea4) {
        this.reservedArea4 = reservedArea4;
    }

    public long getExtTimestamp16() {
        return extTimestamp16;
    }

    public void setExtTimestamp16(short extTimestamp16) {
        this.extTimestamp16 = extTimestamp16;
    }

    public long getExtTimestamp32() {
        return extTimestamp32;
    }

    public void setExtTimestamp32(int extTimestamp32) {
        this.extTimestamp32 = extTimestamp32;
    }

    public int getTailler() {
        return tailler;
    }

    public void setTailler(int tailler) {
        this.tailler = tailler;
    }
}
