package scala.sumit

import java.io.File

import com.wy.Main
import com.wy.display.config.readXML.ReadConfig
import com.wy.model.data.DataSource
import com.wy.model.decetor.{LtpcChannel, LtpcDetector}
import hbaseControl.{TrackerMap, hbasetest}
import javafx.scene.control.{ProgressBar, TextArea}
import listerner.SparkJobListener
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConverters._
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
			.setMaster("local[*]")

		SparkSession.clearDefaultSession()
		val spark = SparkSession.builder().config(conf).getOrCreate()
		ReadConfig.setDetectorByXlxs(new File(classOf[Main].getResource("/detector.xlsx").getPath))
		val dataSource = AnalyseData.MainJob(spark, "C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\", "3wy", 1)
		spark.sparkContext.stop()
	}

	def analyseData(Dictory: String, sourceDataName: String, fileProgressBar: ProgressBar, ConfigLog: TextArea): DataSource = {
		ConfigLog.setStyle("-fx-text-fill:blue")
		ConfigLog.setStyle("-fx-text-fill:red")
		ConfigLog.appendText("\n*********Start Spark Analyse*********")
		ConfigLog.appendText("\n*********Linked to Spark*********")
		val AppName = "analyseData"
		val master = "local[*]"
		//sparkContext
		val spark = SparkSession.builder().master(master).appName(AppName).getOrCreate()
		spark.sparkContext.addSparkListener(new SparkJobListener(6, fileProgressBar, ConfigLog))

		val dataSource = AnalyseData.MainJob(spark, Dictory, sourceDataName, 1)
		spark.sparkContext.stop()
		dataSource
	}


}

object AnalyseData {
	val tablename = "LtpcDataBase"
	val ChannelMap = mutable.HashMap[(Int, Int), LtpcChannel]()
	var trigger_chargeMax = mutable.HashMap[Int, Int]()
	var trigger_chargeMin = mutable.HashMap[Int, Int]()
	var countMap:Option[Map[Int,Int]] = None
	var cliclMap:Option[mutable.HashMap[(Int, Int), Int]] = None

	case class dataSource(var filePath: String, var allPackageCount: Int, var rawDataPackageCount: Int,
												var triggerCount: Int, var everyTriggerPckCount: Map[Int, Int])

	def MainJob(spark: SparkSession, Dictory: String, sourceDataName: String, minPartitions: Int): DataSource = {
		cliclMap = Some(setChannelMap())
		rddAnalyse(spark, Dictory, sourceDataName, minPartitions)
	}
	def countMap2String(): String ={
		val builder = new StringBuilder()
		countMap.get.foreach(
			kv=>{
				builder++=("    Trigger: " + kv._1 + " , packageCount: " + kv._2 + "\n")
			}
		)
		builder.toString()
	}

	def rddAnalyse(spark: SparkSession, Dictory: String, sourceDataName: String, minPartitions: Int): DataSource = {
		val dataSource = new DataSource
		if (hbasetest.notExist(tablename,Dictory +File.separator+ sourceDataName)) {
				val datafilePath = "file:///" + Dictory +File.separator+ sourceDataName
				val board_channelId_trackers = TrackerMap.trackers_From_board(spark)
				val sc = spark.sparkContext
				sc.hadoopConfiguration.set("textinputformat.record.delimiter", new String(StringUtil.intToByteArray(0x1a2b3c4d), "ISO-8859-1"))
				val rdd = sc.newAPIHadoopFile(datafilePath, classOf[TextInputFormat], classOf[LongWritable], classOf[Text])
				val rawDataPckcount = rdd.count()
				val rdd2 = rdd.map(
					pair => new String(pair._2.getBytes, 0, pair._2.getLength, "ISO-8859-1").getBytes("ISO-8859-1"))
				val timeMap = rdd2.filter(
					arr => (arr(12) & 0xff) >> 6 == 3).
					map(arr => ((
						arr(14) & 0xff) << 8 | (arr(15) & 0xff),
						(((arr(22) & 0xff) << 16 | (arr(23) & 0xff) << 8), (arr(24) & 0xff) << 24 | (arr(25) & 0xff) << 16 | (arr(26) & 0xff) << 8 | (arr(27) & 0xff))))
					.map(s => s).collect().toMap
				val rdd3 = rdd2.filter(arr => (arr(12) & 0xff) >> 6 == 2)

					//save 2 hbase
					val outRdd = rdd3.flatMap(arr => {
						splitData(arr, board_channelId_trackers)
					})
					hbasetest.HbaseWriteLtpc(tablename, outRdd, timeMap, sourceDataName,
						trigger_chargeMax, trigger_chargeMin)
				setBoardClick()

				countMap = Some(outRdd.map(arr => (arr(0), 1)).sortByKey().reduceByKey(_ + _).collect().toMap)

				dataSource.setFilePath(Dictory + sourceDataName)
				dataSource.setRawDataPackageCount(rawDataPckcount.toInt)
				dataSource.setTriggerCount(timeMap.size)
				dataSource.setAllPackageCount(outRdd.count())
				dataSource.setTrigger_pckCount(countMap2String())
		}
		dataSource
	}

	private def setBoardClick(): Unit = {
		val map = LtpcDetector.SourceBoardMap.asScala
		map.foreach(k => {
			val sum = k._2.getLtpcChannels.asScala.map(l => l.getClickCount).sum
			k._2.setClickCount(sum)
		})

	}

	def setChannelMap(): mutable.HashMap[(Int, Int), Int] = {
		val channels = ReadConfig.getLtpcDetector.getChannels.asScala
		val map = new mutable.HashMap[(Int, Int), Int]()
		channels.foreach(c => {
			ChannelMap.put((c.getSourceBoardNum, c.getChannelId), c)
			map.put((c.getSourceBoardNum, c.getChannelId), 0)
		})
		map
	}

	def splitData(arr: Array[Byte], BC_trackers: Map[(Int, Int), Array[Int]]): Array[Array[Int]] = {
		val trigger = (arr(20) & 0xff) << 8 | (arr(21) & 0xff)
		val channelId = arr(12) & 0x3f
		val SourceBoardNum = arr(9) & 0xff
		val time_left = (arr(22) & 0xff) << 8 | (arr(23) & 0xff)
		val time_right = (arr(24) & 0xff) << 24 | (arr(25) & 0xff) << 16 | (arr(26) & 0xff) << 8 | (arr(27) & 0xff)
		if (ChannelMap.contains((SourceBoardNum, channelId))) {
			ChannelMap((SourceBoardNum, channelId)).setClickCount(ChannelMap((SourceBoardNum, channelId)).getClickCount + 1)
			cliclMap.get((SourceBoardNum, channelId))+=1
		}
		else {
			throw new RuntimeException(s"sourceboard_channelId不匹配通道: ${SourceBoardNum},${channelId}")
		}

		if (BC_trackers.contains((SourceBoardNum, channelId))) {
			val trackers = BC_trackers.get((SourceBoardNum, channelId))
			val size = trackers.get.length
			val simpleLength = arr(13).toInt * 16
			val dataPoints = new Array[Int](simpleLength)
			var minCharge = Int.MaxValue
			var maxCharge = Int.MinValue

			try {
				for (i <- dataPoints.indices) {
					dataPoints(i) =
						(arr(28 + 2 * i) & 0x0f) << 8 |
							(arr(29 + 2 * i) & 0xff)
					minCharge = math.min(minCharge, dataPoints(i))
					maxCharge = math.max(maxCharge, dataPoints(i))
				}
				if (trigger_chargeMin.contains(trigger)) {
					trigger_chargeMin(trigger) = math.min(trigger_chargeMin(trigger), minCharge)
				}
				else {
					trigger_chargeMin.put(trigger, minCharge)
				}
				if (trigger_chargeMax.contains(trigger)) {
					trigger_chargeMax(trigger) = math.max(trigger_chargeMax(trigger), maxCharge)
				}
				else {
					trigger_chargeMax.put(trigger, maxCharge)
				}
			}
			catch {
				case e: ArrayIndexOutOfBoundsException => println("ArrayIndexOutOfBoundsException")
			}
			val outs = new Array[Array[Int]](size)
			val length = (simpleLength / size)

			for (i <- 0 until size) {
				val splitArr = new Array[Int](length + 6)
				splitArr(0) = trigger
				splitArr(2) = time_left
				splitArr(3) = time_right
				splitArr(4) = SourceBoardNum
				splitArr(5) = channelId
				splitArr(1) = trackers.get(i)
				val start = i * length
				for (j <- 0 until length) {
					splitArr(6 + j) = dataPoints(start + j)
				}
				outs(i) = splitArr
			}
			outs
		}
		else {
			throw new RuntimeException(s"board_channelId不匹配的数据包: ${SourceBoardNum},${channelId}")
		}
	}
}

