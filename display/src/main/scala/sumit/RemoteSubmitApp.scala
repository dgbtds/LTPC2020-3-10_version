package scala.sumit

import org.apache.spark.{SparkConf, SparkContext}

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/1/2 21:52
 */
object RemoteSubmitApp {


	def main(args: Array[String]):Unit={

		val AppName = "sc"
		val master = "yarn"
		val remote_file = "hdfs://localhost:9000/input/wordCount/1.txt"
		 val out_file = "hdfs://localhost:9000/output/"+AppName
		//sparkContext
			val sc = SetSparkContext(AppName, master)
			runSparkContextJob(sc ,remote_file,out_file)
	}

	def SetSparkContext( Appname:String,master:String):SparkContext={
		// 设置提交任务的用户
		System.setProperty("HADOOP_USER_NAME", "wy")
		val sparkConf = new SparkConf()
			.setAppName(Appname)
			// 设置yarn-client模式提交
			.setMaster(master)
			// 设置executor的个数
			.set("spark.executor.instance","2")
			// 设置executor的内存大小
			//.set("spark.executor.memory", "1024M")

		  //.set("yarn.resourcemanager.hostname", "192.168.124.4")
			.set("spark.yarn.jars","hdfs://master:9000/spark/jars/*.jar")

			// 设置driver的本地ip地址
			.setSparkHome("D:\\Spark3.0.0")
			.set("spark.driver.host","192.168.124.2")
			// 设置jar包的路径,如果有其他的依赖包,可以在这里添加,逗号隔开
			.setJars(List("C:\\javaProject\\idealProject\\Hadoop\\JavaScala\\build\\libs\\JavaScala-1.0.0.jar"))
		val sc=new SparkContext(sparkConf)
		sc
	}
	def runSparkContextJob(sc:SparkContext ,remote_file:String,out_file:String):Unit={
		val rowRdd = sc.textFile(remote_file,12)
		rowRdd.partitions.length
		val resultRdd = rowRdd.flatMap(_.split(" ")).map(s=>(s.toInt, 1)).reduceByKey(_ + _)
  			.sortByKey(true,1)

		//resultRdd.saveAsTextFile(out_file)

		var index =1
		resultRdd.collect().foreach(wordNum=>{
			println(index+" *** "+wordNum._1+":"+wordNum._2)
			index+=1
		})
		sc.stop()
	}

}
