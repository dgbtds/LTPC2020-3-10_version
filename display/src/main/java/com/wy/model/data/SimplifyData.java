package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/8 21:45
 */

import com.wy.model.decetor.LtpcChannel;
import javafx.scene.shape.Rectangle;
import scala.Serializable;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:每一个数据包拆分出，属于哪一个触发号，平面，路径，和电荷强度
 *
 * @author: WuYe
 *
 * @create: 2020-03-08 21:45
 **/
public class SimplifyData implements Serializable {
    private LtpcChannel ltpcChannel;
    private int triggerNum;
    private int planeNum;
    private int trackerNum;
    private int channelNum;
    private int charge;
    private short[] shorts;

    public short[] getShorts() {
        return shorts;
    }

    public void setShorts(short[] shorts) {
        this.shorts = shorts;
    }

    public LtpcChannel getLtpcChannel() {
        return ltpcChannel;
    }

    public void setLtpcChannel(LtpcChannel ltpcChannel) {
        this.ltpcChannel = ltpcChannel;
    }

    public int getPlaneNum() {
        return planeNum;
    }

    public void setPlaneNum(int planeNum) {
        this.planeNum = planeNum;
    }

    public int getTriggerNum() {
        return triggerNum;
    }

    public void setTriggerNum(int triggerNum) {
        this.triggerNum = triggerNum;
    }

    public int getTrackerNum() {
        return trackerNum;
    }

    public void setTrackerNum(int trackerNum) {
        this.trackerNum = trackerNum;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }
    @Override
    public String toString(){
        return " 触发号： "+triggerNum+" 平面序号 : "+planeNum+" 激光路径 ： "+trackerNum+" ,通道号 ： "+""+channelNum+" ,电荷强度 ： "+charge+"\n";
    }
}
