package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/29 16:46
 */

import scala.Serializable;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-04-29 16:46
 **/
public class Rectangle extends  javafx.scene.shape.Rectangle implements Serializable {
    public Rectangle(double v, double v1, double wdith, double heigh) {
        super(v,v1,wdith,heigh);
    }
}
