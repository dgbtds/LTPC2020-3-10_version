package com.wy.model.decetor;

import java.util.ArrayList;
import java.util.List;

public class LtpcArea {
	private List<LtpcBoard>list=new ArrayList<>();
	private int area;
	public List<LtpcBoard> getList() {
		return list;
	}
	public void setList(List<LtpcBoard> list) {
		this.list = list;
	}
	public void addList(LtpcBoard Board) {
		this.list.add(Board);
	}
	public int SizeList() {
		return this.list.size();
	}
	public int getArea() {
		return area;
	}
	public void setArea(int area) {
		this.area = area;
	}
	public LtpcArea(List<LtpcBoard> list, int area) {
		super();
		this.list = list;
		this.area = area;
	}
	public LtpcArea(int area) {
		super();
		this.area = area;
	}

}
