package hbaseControl

import com.wy.Main
import org.apache.spark.sql.SparkSession

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/6/24 1:28
 */
object TrackerMap {
		def trackers_From_board(spark:SparkSession): Map[(Int,Int), Array[Int]]={
			val path = classOf[Main].getResource("/trackmap.txt").toExternalForm
//			val path ="C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\trackmap.txt"
			val sc = spark.sparkContext
			val rdd = sc.textFile(path)
			val map = rdd.map(s => {
				resolveString(s)
			}).collect().toMap
			map
		}
	def resolveString(s:String):((Int,Int), Array[Int])={
		val strings = s.split(" ")
		val key = (strings(0).toInt ,strings(1).toInt)
		val trackers = new Array[Int](strings.length-2)
		for(i<-2 until strings.length){
			val int = strings(i).toInt
			trackers(i-2)=int
		}
		(key,trackers)
	}

}
