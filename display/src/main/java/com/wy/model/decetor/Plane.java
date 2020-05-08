package com.wy.model.decetor;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/11 17:49
 */

import scala.Serializable;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:9个平面
 *
 * @author: WuYe
 *
 * @create: 2020-03-11 17:49
 **/
public class Plane implements Serializable {
    public  int planeNum;
    public  Tracker[] planeTrackers;

    public static final Plane PLANE1=new Plane(1,Tracker.plane1Trackers);
    public static final Plane PLANE2=new Plane(2,Tracker.plane2Trackers);
    public static final Plane PLANE3=new Plane(3,Tracker.plane3Trackers);
    public static final Plane PLANE4=new Plane(4,Tracker.plane4Trackers);
    public static final Plane PLANE5=new Plane(5,Tracker.plane5Trackers);
    public static final Plane PLANE6=new Plane(6,Tracker.plane6Trackers);
    public static final Plane PLANE7=new Plane(7,Tracker.plane7Trackers);
    public static final Plane PLANE8=new Plane(8,Tracker.plane8Trackers);
    public static final Plane PLANE9=new Plane(9,Tracker.plane9Trackers);

    public Plane(int planeNum,Tracker[] planeTrackers) {
        this.planeNum = planeNum;
        this.planeTrackers = planeTrackers;
    }
}
