package scala.TestScala

import org.apache.spark.{SparkConf, SparkContext}

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/3 13:25
 */
object Test {


	//byte 数组与 int 的相互转换
	def byteArrayToInt(b: Array[Byte]): Int = b(3) & 0xFF | (b(2) & 0xFF) << 8 | (b(1) & 0xFF) << 16 | (b(0) & 0xFF) << 24
	def intToByteArray(a: Int): Array[Byte] = Array[Byte](((a >> 24) & 0xFF).toByte, ((a >> 16) & 0xFF).toByte, ((a >> 8) & 0xFF).toByte, (a & 0xFF).toByte)
	def SetSparkContext( Appname:String,master:String):SparkContext={
		// 设置提交任务的用户
		System.setProperty("HADOOP_USER_NAME", new String())
		val sparkConf = new SparkConf()
			.setAppName(Appname)
			// 设置本地模式提交
			.setMaster(master)
		val sc=new SparkContext(sparkConf)
		sc
	}
}
