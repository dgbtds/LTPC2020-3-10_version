package scala.TestScala

import org.apache.spark.Partitioner
	/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/3 14:08
 */
class MyPartitioner (nums:Int)extends Partitioner{
		override def numPartitions: Int = nums

		override def getPartition(key: Any): Int = key.toString.toInt%nums;
	}
