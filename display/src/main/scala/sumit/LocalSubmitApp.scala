package scala.sumit

import java.io.File
import java.util
import java.util.Properties

import com.google.common.base.Charsets
import com.wy.display.config.ConfigController
import com.wy.display.config.readXML.ReadConfig
import com.wy.input.MyTextInputFormat
import com.wy.model.data.SimplifyData
import com.wy.model.decetor.{LtpcChannel, LtpcDetector}
import javafx.scene.control.{ProgressBar, TextArea}
import listerner.SparkJobListener
import org.apache.commons.codec.binary.Base64
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import sumit.BaseJob
import sumit.BaseJob.{byteArrayToInt, datapck}

import scala.collection.mutable
import scala.util.StringUtil

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/3 13:04
 */
object LocalSubmitApp {
	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
			.setAppName("analyseData2")
			.set("spark.sql.shuffle.partitions", "10")
			.setMaster("local[*]")

		ReadConfig.setDetectorByXlxs(new File("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\detector.xlsx"))

		SparkSession.clearDefaultSession()
		val spark = SparkSession.builder().config(conf).getOrCreate()
		//		spark.sql("select count(*),trigger from test group by trigger order by trigger ").show(true)
		//		spark.sql("select count(*),pckType from test group by pckType").show(true)
		//		spark.sql("select pckType,points from test where pckType='error'").show(false)

		AnalyseData.MainJobOPT(spark, "C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\CreatData\\LaserH_45_1.bin", 1)
	}


	def analyseDataOPT(localFile: String, fileProgressBar: ProgressBar, ConfigLog: TextArea):Dataset[datapck]= {
		ConfigLog.clear()
		ConfigLog.setStyle("-fx-text-fill:red")
		ConfigLog.appendText("\n*********Start Spark Analyse*********")
		ConfigLog.appendText("\n*********Linked to Spark*********")
		val AppName = "analyseData"
		val master = "local[*]"
		//sparkContext
		val spark = SparkSession.builder().master(master).appName(AppName).getOrCreate()
		ConfigController.spark=spark
		spark.sparkContext.addSparkListener(new SparkJobListener(6, fileProgressBar, ConfigLog))
		AnalyseData.MainJobOPTSingle(spark, localFile)
	}

}

object AnalyseData {
	var isSerializable = false
	val cacheTriggerNum = 100
	val header = 0x1eadc0de
	val trailer = 0x5a5a5a5a

	case class mysqlData(var ltpcChannel: LtpcChannel, var triggerNum: Int, var planeNum: Int,
											 var trackerNum: Int, var channelNum: Int, var charge: Int, var shorts: Array[Short]) {
	}

	case class dataSource(var filePath: String, var allPackageCount: Int, var rawDataPackageCount: Int,
												var triggerCount: Int, var everyTriggerPckCount: Map[Integer, Integer], var everyTriggerTime: Map[Integer, Long],
												var sdList: List[SimplifyData], var chargeMax: Int, var chargeMin: Int)

	def cloneFromSimplifyData(s: SimplifyData): mysqlData = {
		val ltpcChannel = s.getLtpcChannel
		val triggerNum = s.getTriggerNum
		val planeNum = s.getPlaneNum
		val trackerNum = s.getTrackerNum
		val channelNum = s.getPID
		val charge = s.getCharge
		val shorts = s.getShorts
		mysqlData(ltpcChannel, triggerNum, planeNum, trackerNum, channelNum, charge, shorts)
	}

	val ChannelMap = mutable.HashMap[(Integer, Integer), LtpcChannel]()
	var chargeMax = 0
	var chargeMin = Integer.MAX_VALUE
	val hashmap = new util.HashMap[Integer, Integer]()
	val timemap = new util.HashMap[Integer, java.lang.Long]()



	def MainJobOPT(spark: SparkSession, datafilePath: String, minPartitions: Int) {
		rddAnalyseOpt(spark, datafilePath, minPartitions)
	}
	def MainJobOPTSingle(spark: SparkSession, datafilePath: String):Dataset[datapck]= {
		rddAnalyseOptSingle(spark, datafilePath)
	}
	def rddAnalyseOptSingle(spark: SparkSession, datafilePath: String) :Dataset[datapck]={
		val datafilePath0 = "file:///" + datafilePath
		val sc = spark.sparkContext
		import scala.collection.mutable.ListBuffer

		val board_channelId_trackers = LtpcDetector.sourceBoardChannelIdChannelMap
		//        ---------------    creat rdd by txt ---------------------
		val header = new String(BaseJob.intToByteArray(0x1eadc0de), Charsets.ISO_8859_1)

		sc.hadoopConfiguration.set("textinputformat.record.delimiter", header)
		val rdd0 = sc.newAPIHadoopFile(datafilePath0, classOf[MyTextInputFormat], classOf[LongWritable], classOf[Text], sc.hadoopConfiguration)
		import spark.implicits._
		val ds = rdd0.mapPartitions(iter => {
			val buffer = ListBuffer[datapck]()
			while (iter.hasNext) {
				val text = iter.next()._2
				val arr = text.getBytes
				if (arr.length > 8) {
					val flag = ((arr(8) & 0xff) >> 6)
					if (flag == 2) {
						val outs = BaseJob.splitDataSingle(arr, board_channelId_trackers)
						for(i<-outs.indices){
							buffer.append(outs(i))
						}
					}
					else if (flag == 3) {
						val trigger = (arr(10) & 0xff) << 8 | (arr(11) & 0xff)
						val time_left = (arr(18) & 0xff) << 8 | (arr(19) & 0xff)
						val time_right = byteArrayToInt(arr, 20)
						val timepck = datapck("time", trigger, 0, time_left, time_right, 0, 0, 0, 0, 0, "timePck,")
						buffer.append(timepck)
					}
					else {
						val errorpck = datapck("error", -1, flag, 0, 0, 0, 0, 0, 0, 0, "flag!=2,3 ,tracker is flag")
						buffer.append(errorpck)
					}
				}
				else {
					if (arr.nonEmpty) {
						val errorpck = datapck("error", -1, 0, 0, 0, 0, 0, 0, 0, 0, "arr.length <8")
						buffer.append(errorpck)
					}
				}
			}
			buffer.iterator
		}).toDS()

		ds.createTempView(ConfigController.tableName)
		ds
	}

	def rddAnalyseOpt(spark: SparkSession, datafilePath: String, minPartitions: Int) {
		val datafilePath0 = "file:///" + datafilePath
		val sc = spark.sparkContext
		import scala.collection.mutable.ListBuffer
		print("start spark job")

		val board_channelId_trackers = BaseJob.createMap(sc, spark)
		//        ---------------    creat rdd by txt ---------------------
		val header = new String(BaseJob.intToByteArray(0x1eadc0de), Charsets.ISO_8859_1)

		sc.hadoopConfiguration.set("textinputformat.record.delimiter", header)
		val rdd0 = sc.newAPIHadoopFile(datafilePath0, classOf[MyTextInputFormat], classOf[LongWritable], classOf[Text], sc.hadoopConfiguration)
		import spark.implicits._
		val ds = rdd0.mapPartitions(iter => {
			val buffer = ListBuffer[datapck]()
			while (iter.hasNext) {
				val text = iter.next()._2
				val arr = text.getBytes
				if (arr.length > 8) {
					val flag = ((arr(8) & 0xff) >> 6)
					if (flag == 2) {
						val array = BaseJob.splitData(arr, board_channelId_trackers)
						if (array.length == 0) {
							val channelId = arr(8) & 0x3f
							val board = arr(5) & 0xff
							val errorpck = datapck("error", -1, 0, 0, 0, 0, 0, 0, 0, 0, "board: " + board + " ,channelID: " + channelId + " not Exist")
							buffer.append(errorpck)
						}
						array.foreach(ints => {
							val ints1 = ints.slice(9, ints.length)
							val str = ints1.mkString(";")
							val samplepck = datapck("sample", ints(0), ints(1), ints(2), ints(3), ints(4), ints(5), ints(6), ints(7), ints(8), str)
							buffer.append(samplepck)
						})
					}
					else if (flag == 3) {
						val trigger = (arr(10) & 0xff) << 8 | (arr(11) & 0xff)
						val time_left = (arr(18) & 0xff) << 8 | (arr(19) & 0xff)
						val time_right = byteArrayToInt(arr, 20)
						val timepck = datapck("time", trigger, 0, time_left, time_right, 0, 0, 0, 0, 0, "timePck,")
						buffer.append(timepck)
					}
					else {
						val errorpck = datapck("error", -1, flag, 0, 0, 0, 0, 0, 0, 0, "flag!=2,3 ,tracker is flag")
						buffer.append(errorpck)
					}
				}
				else {
					if (arr.nonEmpty) {
						val errorpck = datapck("error", -1, 0, 0, 0, 0, 0, 0, 0, 0, "arr.length <8")
						buffer.append(errorpck)
					}
				}
			}
			buffer.iterator
		}).toDS()

		ds.createTempView("test")
		//spark.sql("select count(*),t.trigger from (SELECT a.trigger FROM test a GROUP BY a.trigger,a.board,a.channelid ) as t GROUP BY t.trigger ORDER BY t.trigger Desc").show(true)
	}


	private def getSaveFilePath(datafilePath: String): String = {
		val file = new File(datafilePath)
		val saveName = file.getName + "_" + Base64.encodeBase64String(datafilePath.getBytes())
		val dictory = new File(file.getParent + "\\kryoSerializable")
		if (!dictory.exists()) {
			dictory.mkdir()
		}
		val savefilePath = s"${dictory}\\${saveName}.dat"
		savefilePath
	}


	def saveRdd2Mysql(spark: SparkSession, rdd: RDD[SimplifyData], tableName: String): Unit = {
		val prop = new Properties()
		prop.load(getClass.getResourceAsStream("/mysql.properties"))
		import spark.implicits._
		val caseClassDF = rdd.map(simplifyData => cloneFromSimplifyData(simplifyData)).toDF()
		caseClassDF.write.mode(SaveMode.Append).jdbc(prop.getProperty("url"), tableName, prop)
	}

	private def setChargeLimit(sfdList: List[SimplifyData]): Unit = {
		sfdList.foreach(s => {
			val charge = s.getCharge
			if (charge > chargeMax) chargeMax = charge
			if (charge < chargeMin) chargeMin = charge
		})
	}


	def findMax(dataPoints: Array[Short], start: Int, end: Int) = {
		var MIndex = start
		for (i <- start + 1 until end) {
			if (dataPoints(i) > dataPoints(MIndex)) MIndex = i
		}
		MIndex
	}

	private def compareInt(arr: Array[Byte], start: Int, num: Int): Boolean = {
		if (start + 3 >= arr.length) {
			return false
		}
		StringUtil.byteArrayToInt(arr, start) == num
	}

}

