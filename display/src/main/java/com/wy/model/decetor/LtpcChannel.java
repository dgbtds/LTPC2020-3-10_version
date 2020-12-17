package com.wy.model.decetor;

import com.wy.display.config.ConfigController;
import com.wy.model.data.Rectangle;
import javafx.scene.paint.Color;
import scala.Serializable;

import java.beans.Transient;


public class LtpcChannel implements Serializable {
    private int pid;//通道序列号
    private int area;//探测器区号	)
    private int board;//探测器分区分块号
    private int col;//探测器分区分块列号
    private int row;//探测器分区分块行号
    private double x_center;
    private double y_center;
    private int interfaceId;//接口槽id
    private int channelId;
    private int heigh;
    private int wdith;
    private int slope;//倾斜角度
    private int sourceBoardNum;//来源板号
    private Rectangle rectangle;
    private int[] trackNums;
    private int ClickCount = 0;
    private int color = -1;

    public int[] getTrackNums() {
        return trackNums;
    }

    public void setTrackNums(int[] trackNums) {
        this.trackNums = trackNums;
    }

    public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getClickCount() {
        return ClickCount;
    }

    public void setClickCount(int clickCount) {
        ClickCount = clickCount;
    }


    public int getSourceBoardNum() {
        return sourceBoardNum;
    }

    public void setSourceBoardNum(int sourceBoardNum) {
        this.sourceBoardNum = sourceBoardNum;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public int getWdith() {
        return wdith;
    }

    public void setWdith(int wdith) {
        this.wdith = wdith;
    }

    public int getHeigh() {
        return heigh;
    }

    public void setHeigh(int heigh) {
        this.heigh = heigh;
    }


    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getBoard() {
        return board;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public double getX_center() {
        return x_center;
    }

    public void setX_center(double x_center) {
        this.x_center = x_center;
    }

    public double getY_center() {
        return y_center;
    }

    public void setY_center(double y_center) {
        this.y_center = y_center;
    }

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getSlope() {
        return slope;
    }

    public void setSlope(int slope) {
        this.slope = slope;
    }

    public LtpcChannel() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LtpcChannel(int pid, int area, int board, int col, int row, double x_center, double y_center,
                       int interfaceId, int channelId, int slope) {
        super();
        this.pid = pid;
        this.area = area;
        this.board = board;
        this.col = col;
        this.row = row;
        this.x_center = x_center;
        this.y_center = y_center;
        this.interfaceId = interfaceId;
        this.channelId = channelId;
        this.slope = slope;
    }

    @Override
    public String toString() {
        return "LtpcChannel{" +
                "pid=" + pid +
                ", area=" + area +
                ", board=" + board +
                ", col=" + col +
                ", row=" + row +
                ", x_center=" + x_center +
                ", y_center=" + y_center +
                ", interfaceId=" + interfaceId +
                ", channelId=" + channelId +
                ", heigh=" + heigh +
                ", wdith=" + wdith +
                ", slope=" + slope +
                ", sourceBoardNum=" + sourceBoardNum +
                '}';
    }
    public void computeTracks() {
        switch (area) {
            case 0:
                trackNums = new int[]{37, 38, 39, 40, 41, 42};
                break;
            case 1:
                trackNums = new int[]{board + 1, board + 13, board + 25};
                break;
            case 2:
                trackNums = new int[]{board + 7, board + 19, board + 31};
                break;
            case 3:
                switch (board) {
                    case (0):
                        trackNums = new int[]{2,  19,  31};
                        break;
                    case (1):
                        trackNums = new int[]{1,13, 25};
                        break;
                    case (2):
                        trackNums = new int[]{4, 16, 28};
                        break;
                    case (3):
                        trackNums = new int[]{3, 15, 27};
                        break;
                    case (4):
                        trackNums = new int[]{6, 18, 30};
                        break;
                    case (5):
                        trackNums = new int[]{5, 17, 29};
                        break;
                    default:
                        throw new RuntimeException("area:"+area+" , board error: " +board);
                }
                break;
            case 4:
                switch (board) {
                    case (0):
                        trackNums = new int[]{8, 20, 32};
                        break;
                    case (1):
                        trackNums = new int[]{7, 19, 31};
                        break;
                    case (2):
                        trackNums = new int[]{10, 22, 34};
                        break;
                    case (3):
                        trackNums = new int[]{9, 21, 33};
                        break;
                    case (4):
                        trackNums = new int[]{12, 24, 36};
                        break;
                    case (5):
                        trackNums = new int[]{11, 23, 35};
                        break;
                    default:
                        throw new RuntimeException("area:"+area+" , board error: " + board);
                }
                break;
            default:
                throw new RuntimeException("case error: " + area);
        }
    }
}
