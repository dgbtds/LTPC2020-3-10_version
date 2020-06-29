package com.wy.model.decetor;

import scala.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LtpcDetector implements Serializable {
	public static HashMap<Integer,LtpcSourceBoard> SourceBoardMap=new HashMap<>(20);
	private List<LtpcArea>areas;
	private List<LtpcBoard>boards;
	private List<LtpcChannel>channels;
	private static HashMap<Integer,LtpcChannel>pidChannelMap=new HashMap<>(1500);

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
		channels.forEach(channel->{
			pidChannelMap.put(channel.getPid(),channel);
		});
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("-------Detector Info---------\n");

		stringBuilder.append("    ######### Board Info #########\n");
		stringBuilder.append("        Board Count: "+SourceBoardMap.size()+"\n");
		for(Map.Entry<Integer,LtpcSourceBoard> entry:SourceBoardMap.entrySet()){
			stringBuilder.append("        BoardNum: "+entry.getKey()+" ,Channel Count: "+entry.getValue().getLtpcChannels().size()+"\n");
		}

		stringBuilder.append("\n\n    #########ChannelId (Plane,Laser,Cluster)#########\n");
		channels.forEach(c->{
			stringBuilder.append("\n            ChannelId:"+c.getPid()+" --(Plane,Laser,cluster)--");
			PlaneWithTrack[] planeWithTracks = c.getPlaneWithTracks();
			for(PlaneWithTrack pwt:planeWithTracks){
				stringBuilder.append("("+pwt.getPlane().planeNum+","+pwt.getTracker().trackerNum+","+pwt.getTracker().cluster+"); ");
			}
		});
		return stringBuilder.toString();
	}

	public static HashMap<Integer, LtpcChannel> getPidChannelMap() {
		return pidChannelMap;
	}

	public static void setPidChannelMap(HashMap<Integer, LtpcChannel> pidChannelMap) {
		LtpcDetector.pidChannelMap = pidChannelMap;
	}
}
