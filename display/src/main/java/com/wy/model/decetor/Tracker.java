package com.wy.model.decetor;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/11 17:51
 */

import scala.Serializable;

import java.util.HashMap;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:激光径迹
 *
 * @author: WuYe
 *
 * @create: 2020-03-11 17:51
 **/
public class Tracker implements Serializable {
    public static final int allTrackerNum=42;
    public  int trackerNum;
    public  int cluster;
    public static HashMap<Integer,Tracker> trackerHashMap=new HashMap<>(allTrackerNum);
    public static Tracker[] trackers=new Tracker[allTrackerNum+1];
    static {
       trackers[1] = new  Tracker(1, 5);
       trackers[2] = new  Tracker(2, 5);
       trackers[3] = new  Tracker(3, 18);
       trackers[4] = new  Tracker(4, 18);
       trackers[5] = new  Tracker(5, 21);
       trackers[6] = new  Tracker(6, 21);
       trackers[7] = new  Tracker(7, 5);
       trackers[8] = new  Tracker(8, 5);
       trackers[9] = new  Tracker(9, 18);
       trackers[10] = new Tracker(10, 18);
       trackers[11] = new Tracker(11, 21);
       trackers[12] = new Tracker(12, 21);

       trackers[13] = new Tracker(13, 5);
       trackers[14] = new Tracker(14, 5);
       trackers[15] = new Tracker(15, 18);
       trackers[16] = new Tracker(16, 18);
       trackers[17] = new Tracker(17, 21);
       trackers[18] = new Tracker(18, 21);
       trackers[19] = new Tracker(19, 5);
       trackers[20] = new Tracker(20, 5);
       trackers[21] = new Tracker(21, 18);
       trackers[22] = new Tracker(22, 18);
       trackers[23] = new Tracker(23, 21);
       trackers[24] = new Tracker(24, 21);

       trackers[25] = new Tracker(25, 5);
       trackers[26] = new Tracker(26, 5);
       trackers[27] = new Tracker(27, 18);
       trackers[28] = new Tracker(28, 18);
       trackers[29] = new Tracker(29, 21);
       trackers[30] = new Tracker(30, 21);
       trackers[31] = new Tracker(31, 5);
       trackers[32] = new Tracker(32, 5);
       trackers[33] = new Tracker(33, 18);
       trackers[34] = new Tracker(34, 18);
       trackers[35] = new Tracker(35, 21);
       trackers[36] = new Tracker(36, 21);

       trackers[37] = new Tracker(37, 37);
       trackers[38] = new Tracker(38, 37);
       trackers[39] = new Tracker(39, 37);
       trackers[40] = new Tracker(40, 37);
       trackers[41] = new Tracker(41, 37);
       trackers[42] = new Tracker(42, 37);
        for(int i=1;i<=allTrackerNum;i++){
            trackerHashMap.put(i,trackers[i]);
        }
    }

    public static final Tracker[] plane1Trackers= {
       trackers[1],trackers[2],trackers[3],trackers[4],trackers[5],trackers[6],trackers[7],trackers[8],trackers[9],trackers[10],trackers[11],trackers[12]
    };
    public static final Tracker[] plane2Trackers={
       trackers[12],trackers[13],trackers[14],trackers[15],trackers[16],trackers[17],trackers[18],trackers[19],trackers[20],trackers[21],trackers[22],trackers[23],trackers[24]
    };
    public static final Tracker[] plane3Trackers= {
       trackers[25],trackers[26],trackers[27],trackers[28],trackers[29],trackers[30],trackers[31],trackers[32],trackers[33],trackers[34],trackers[35],trackers[36]
    };
    public static final Tracker[] plane4Trackers= {trackers[37]};
    public static final Tracker[] plane5Trackers= {trackers[38]};
    public static final Tracker[] plane6Trackers= {trackers[39] };
    public static final Tracker[] plane7Trackers= {trackers[40] };
    public static final Tracker[] plane8Trackers= {trackers[41] };
    public static final Tracker[] plane9Trackers= {trackers[42] };

    public Tracker(int trackerNum, int cluster) {
        this.trackerNum= trackerNum;
        this.cluster= cluster;
    }
}
