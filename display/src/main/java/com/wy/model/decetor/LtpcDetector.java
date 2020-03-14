package com.wy.model.decetor;

import java.util.List;

public class LtpcDetector {
	private List<LtpcArea>areas;
	private List<LtpcBoard>boards;
	private List<LtpcChannel>channels;

	public List<LtpcArea> getAreas() {
		return areas;
	}
	public void setAreas(List<LtpcArea> areas) {
		this.areas = areas;
	}
	public List<LtpcBoard> getBoards() {
		return boards;
	}
	public void setBoards(List<LtpcBoard> boards) {
		this.boards = boards;
	}
	public List<LtpcChannel> getChannels() {
		return channels;
	}
	public void setChannels(List<LtpcChannel> channels) {
		this.channels = channels;
	}
	public LtpcDetector() {
		super();
	}
	public LtpcDetector(List<LtpcArea> areas, List<LtpcBoard> boards, List<LtpcChannel> channels) {
		super();
		this.areas = areas;
		this.boards = boards;
		this.channels = channels;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("-------Detector信息如下---------\n");
		System.out.println();
	   for (int i = 0; i < this.areas.size(); i++) {
		   stringBuilder.append("   区块"+i+",board数目："+this.areas.get(i).SizeList()+"\n");
			for (int j = 0; j < this.areas.get(i).getList().size(); j++) {
				stringBuilder.append("------>boardNum:"+j+"  ,含有通道："+this.areas.get(i).getList().get(j).getList().size()+"\n");
			}
		}
	   stringBuilder.append("\n\n-----通道和(平面,激光)序号对应关系-----\n");
	   channels.forEach(c->{
		   stringBuilder.append("\n通道序列号:"+c.getPid()+" --(平面,激光)--");
		   PlaneWithTrack[] planeWithTracks = c.getPlaneWithTracks();
		   for(PlaneWithTrack pwt:planeWithTracks){
		   	stringBuilder.append("("+pwt.getPlane().planeNum+","+pwt.getTracker().trackerNum+"); ");
		   }
	   });
	   return stringBuilder.toString();
	}

}
