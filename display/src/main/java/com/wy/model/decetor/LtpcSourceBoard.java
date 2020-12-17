package com.wy.model.decetor;


import scala.Serializable;

import java.util.ArrayList;
import java.util.List;

//之前使用的板号
public class LtpcSourceBoard implements Serializable {
	private int board;
	private int area;
	public int clickCount;

public List<LtpcChannel> list=null;

public int getBoard() {
	return board;
}

public void setBoard(int board) {
	this.board = board;
}

public int getArea() {
	return area;
}

public void setArea(int area) {
	this.area = area;
}
public void addList(LtpcChannel channel) {
	this.list.add(channel);
	}
@Override
public String toString() {
	return "LtpcBoard [board=" + board + ", area=" + area + ", list=" + list + "]";
}

public LtpcSourceBoard() {
	super();
	// TODO Auto-generated constructor stub
}

public LtpcSourceBoard(int area, int board) {
	super();
	this.board = board;
	this.area = area;
	list= new ArrayList<>();
} 

}
