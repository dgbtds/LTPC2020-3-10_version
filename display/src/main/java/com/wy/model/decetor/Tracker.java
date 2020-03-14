package com.wy.model.decetor;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/11 17:51
 */

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:激光径迹
 *
 * @author: WuYe
 *
 * @create: 2020-03-11 17:51
 **/
public class Tracker {
    public  int trackerNum;
    public static final Tracker[] plane1Trackers= {
            new Tracker(1),new Tracker(2),new Tracker(3),
            new Tracker(4),new Tracker(5),new Tracker(6),
            new Tracker(7),new Tracker(8),new Tracker(9),
            new Tracker(10),new Tracker(11),new Tracker(12)
    };
    public static final Tracker[] plane2Trackers= {
            new Tracker(13),new Tracker(14),new Tracker(15),
            new Tracker(16),new Tracker(17),new Tracker(18),
            new Tracker(19),new Tracker(20),new Tracker(21),
            new Tracker(22),new Tracker(23),new Tracker(24)
    };
    public static final Tracker[] plane3Trackers= {
            new Tracker(25),new Tracker(26),new Tracker(27),
            new Tracker(28),new Tracker(29),new Tracker(30),
            new Tracker(31),new Tracker(32),new Tracker(33),
            new Tracker(34),new Tracker(35),new Tracker(36)
    };
    public static final Tracker[] plane4Trackers= {new Tracker(37) };
    public static final Tracker[] plane5Trackers= {new Tracker(38) };
    public static final Tracker[] plane6Trackers= {new Tracker(39) };
    public static final Tracker[] plane7Trackers= {new Tracker(40) };
    public static final Tracker[] plane8Trackers= {new Tracker(41) };
    public static final Tracker[] plane9Trackers= {new Tracker(42) };

    public Tracker(int trackerNum) {
        this.trackerNum = trackerNum;
    }
}
