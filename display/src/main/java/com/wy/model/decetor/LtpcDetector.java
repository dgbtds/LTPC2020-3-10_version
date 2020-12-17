package com.wy.model.decetor;

import scala.Serializable;
import scala.Tuple2;

import java.util.HashMap;
import java.util.List;

public class LtpcDetector implements Serializable {
	public static HashMap<Integer,LtpcSourceBoard> SourceBoardChannelsMap=new HashMap<>(20);
	public static HashMap<String,LtpcChannel>sourceBoardChannelIdChannelMap=new HashMap<>(1500);
	public static List<LtpcChannel> channels;

	public LtpcDetector() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("-------Detector Info---------\n");

		stringBuilder.append("    ######### Board Info #########\n");
		stringBuilder.append("        Board Count: "+SourceBoardChannelsMap.size()+"\n");
		stringBuilder.append("        channel Count: "+sourceBoardChannelIdChannelMap.size()+"\n");
		stringBuilder.append("\n\n    #########ChannelId (Plane,Laser,Cluster)#########\n");
		return stringBuilder.toString();
	}

}
