package com.wy.model.decetor;/**

 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/11 17:08
 */
import scala.Serializable;

import static  com.wy.model.decetor.Plane.*;
import static  com.wy.model.decetor.Tracker.*;
/**
 * @program: LTPC2020-3-10_version
 *
 * @description:平面
 *
 * @author: WuYe
 *
 * @create: 2020-03-11 17:08
 **/
public class PlaneWithTrack implements Serializable {
    private Plane plane;
    private Tracker tracker;

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    private PlaneWithTrack(Plane plane, Tracker tracker) {
        this.plane = plane;
        this.tracker = tracker;
    }
    public static PlaneWithTrack[] getPlaneWithTrack(LtpcChannel ltpcChannel){
        int board=0;
        switch (ltpcChannel.getArea()){
            case 0:
                return new PlaneWithTrack[]{
                        new PlaneWithTrack(PLANE4,plane4Trackers[0]),
                        new PlaneWithTrack(PLANE5,plane5Trackers[0]),
                        new PlaneWithTrack(PLANE6,plane6Trackers[0]),
                        new PlaneWithTrack(PLANE7,plane7Trackers[0]),
                        new PlaneWithTrack(PLANE8,plane8Trackers[0]),
                        new PlaneWithTrack(PLANE9,plane9Trackers[0]),
                };
            case 1:
                board= ltpcChannel.getBoard();
                return new PlaneWithTrack[]{
                        new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                        new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                        new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                };

            case 2:
                board= ltpcChannel.getBoard()+6;
                return new PlaneWithTrack[]{
                        new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                        new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                        new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                };

            case 3:
                switch (ltpcChannel.getBoard()){
                    case (0):
                        board=1;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (1):
                        board=0;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (2):
                        board=3;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (3):
                        board=2;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (4):
                        board=5;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (5):
                        board=4;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };

                    default:
                        break;
                }
            case 4:
                switch (ltpcChannel.getBoard()){
                    case (0):
                        board=7;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (1):
                        board=6;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (2):
                        board=9;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (3):
                        board=8;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (4):
                        board=11;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };
                    case (5):
                        board=10;
                        return new PlaneWithTrack[]{
                                new PlaneWithTrack(PLANE1,plane1Trackers[board]),
                                new PlaneWithTrack(PLANE2,plane2Trackers[board]),
                                new PlaneWithTrack(PLANE3,plane3Trackers[board]),
                        };

                    default:
                        break;
                }
            default:
                throw new RuntimeException("case error: "+ltpcChannel.getArea());
        }
    }
}
