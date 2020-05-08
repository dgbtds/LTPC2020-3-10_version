package com.wy.model.decetor;


import scala.Serializable;

import java.util.ArrayList;
import java.util.List;

//之前使用的板号
public class LtpcBoard implements Serializable {
	private int board;
	private int area;

private List<LtpcChannel> list=new ArrayList<LtpcChannel>();

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

public List<LtpcChannel> getList() {
	return list;
}

public void setList(List<LtpcChannel> list) {
	this.list = list;
}
public void addList(LtpcChannel channel) {
	this.list.add(channel);
	}
public int SizeList() {
	return this.list.size();
	}
@Override
public String toString() {
	return "LtpcBoard [board=" + board + ", area=" + area + ", list=" + list + "]";
}

public LtpcBoard() {
	super();
	// TODO Auto-generated constructor stub
}

public LtpcBoard(int board, int area) {
	super();
	this.board = board;
	this.area = area;
} 

}
