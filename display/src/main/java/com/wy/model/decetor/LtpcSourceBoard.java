package com.wy.model.decetor;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/14 12:38
 */


import scala.Serializable;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:更新的来源板号
 *
 * @author: WuYe
 *
 * @create: 2020-03-14 12:38
 **/
public class LtpcSourceBoard implements Serializable {
    private int SourceBoardNum;
    private int Area;//区号
    private LinkedList<LtpcChannel> ltpcChannels;
    private int ClickCount;

    public int getArea() {
        return Area;
    }

    public void setArea(int area) {
        Area = area;
    }


    public int getSourceBoardNum() {
        return SourceBoardNum;
    }

    public void setSourceBoardNum(int sourceBoardNum) {
        SourceBoardNum = sourceBoardNum;
    }


    public LinkedList<LtpcChannel> getLtpcChannels() {
        return ltpcChannels;
    }

    public void setLtpcChannels(LinkedList<LtpcChannel> ltpcChannels) {
        this.ltpcChannels = ltpcChannels;
    }

    public int getClickCount() {
        return ClickCount;
    }

    public void setClickCount(int clickCount) {
        ClickCount = clickCount;
    }
}
