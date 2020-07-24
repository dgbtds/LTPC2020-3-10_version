package scala.sumit

import java.io.{BufferedOutputStream, File, FileOutputStream, RandomAccessFile}
import java.nio.channels.FileChannel.MapMode
import java.util
import java.util.Properties

import com.wy.Utils.KryoUtil
import com.wy.display.config.readXML.ReadConfig
import com.wy.model.data.{DataSource, SimplifyData}
import com.wy.model.decetor.{LtpcChannel, LtpcDetector}
import javafx.scene.control.{ProgressBar, TextArea}
import listerner.SparkJobListener
import org.apache.commons.codec.binary.Base64
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SparkSession}

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
			.set("spark.sql.shuffle.partitions", "10")
			.setMaster("local[*]")

		ReadConfig.setDetectorByXlxs(new File("C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\detector.xlsx"))

		SparkSession.clearDefaultSession()
		val spark = SparkSession.builder().config(conf).getOrCreate()


		val dataSource = AnalyseData.MainJob(spark, "C:\\Users\\dgbtds\\Desktop\\LtpcExe\\MyApp\\CreatData\\1000", 1)
		SparkSession.clearDefaultSession()
		spark.sparkContext.stop()
	}

	def analyseData(localFile: String, out_file: String, fileProgressBar: ProgressBar, ConfigLog: TextArea): DataSource = {
		ConfigLog.clear()
		ConfigLog.setStyle("-fx-text-fill:blue")
		ConfigLog.appendText("\n*********Search From History Record*********")
		AnalyseData.read4fileIfexist(localFile) match {
			case Some(dataSource) => dataSource
			case None =>
				ConfigLog.setStyle("-fx-text-fill:red")
				ConfigLog.appendText("\n*********History Record Not Exists*********")
				ConfigLog.appendText("\n*********Start Spark Analyse*********")
				ConfigLog.appendText("\n*********Linked to Spark*********")
				val AppName = "analyseData"
				val master = "local[*]"
				//sparkContext
				val spark = SparkSession.builder().master(master).appName(AppName).getOrCreate()
				spark.sparkContext.addSparkListener(new SparkJobListener(6, fileProgressBar, ConfigLog))

				val dataSource = AnalyseData.MainJob(spark, localFile, 1)
				spark.sparkContext.stop()
				dataSource
		}

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


	def MainJob(spark: SparkSession, datafilePath: String, minPartitions: Int): DataSource = {
		setChannelMap()
		rddAnalyse(spark, datafilePath, minPartitions)
	}

	def rddAnalyse(spark: SparkSession, datafilePath: String, minPartitions: Int): DataSource = {
		val datafilePath0 = "file:///" + datafilePath
		val sc = spark.sparkContext
		val bytes1 = StringUtil.intToByteArray(trailer)
		val str = new String(bytes1, "ISO-8859-1")
		sc.hadoopConfiguration.set("textinputformat.record.delimiter",str)
		val rdd = sc.newAPIHadoopFile(datafilePath0, classOf[TextInputFormat], classOf[LongWritable], classOf[Text])
		val rawDataPckcount = rdd.count()
		val rdd2 = rdd.map(
			pair =>
				new String(pair._2.getBytes, 0, pair._2.getLength, "ISO-8859-1").getBytes("ISO-8859-1"))
		val timeMap = rdd2.filter(
			arr =>
				(arr(12) & 0xff) >> 6 == 3).
			map(arr => ((
				arr(14) & 0xff) << 8 | (arr(15) & 0xff)
				, (arr(22) & 0xff) << 40 | (arr(23) & 0xff) << 32 | (arr(24) & 0xff) << 24 | (arr(25) & 0xff) << 16 | (arr(26) & 0xff) << 8 | (arr(27) & 0xff)))
			.sortByKey(true).collect().toMap
		timeMap.foreach(k => {
			timemap.put(k._1, k._2)
		})
		val rdd3 = rdd2.filter(arr => (arr(12) & 0xff) >> 6 == 2)
		val countMap = rdd3.map(arr => ((arr(20) & 0xff) << 8 | (arr(21) & 0xff), 1)).reduceByKey(_ + _)
			.sortByKey(true).collect().toMap
		countMap.foreach(k => {
			hashmap.put(k._1, k._2)
		})
		val rdd4 = rdd3
			.map(arr => {
				processSimpleData(arr)
			})
			.filter {
				case Some(_) => true
				case None => false
			}
			.flatMap(opt => opt.get)

		val sfdList = rdd4.collect().toList
		val dataSource = new DataSource
		setChargeLimit(sfdList)
		dataSource.setFilePath(datafilePath)
		dataSource.setRawDataPackageCount(rawDataPckcount.toInt)
		dataSource.setTriggerCount(timemap.size())
		dataSource.setEveryTriggerPckCount(hashmap)

		dataSource.setSdList(sfdList.asJava)
		dataSource.setChargeMax(chargeMax)
		dataSource.setChargeMin(chargeMin)
		dataSource.setEveryTriggerTime(timemap)
		dataSource.setAllPackageCount(sfdList.size)
		setBoardClick()
		if (isSerializable) {
			save2file(dataSource, datafilePath)
		}
		dataSource
	}


	def save2file(dataSource: DataSource, datafilePath: String): Unit = {
		val bytes = KryoUtil.serializationObject(dataSource)
		val savefilePath = getSaveFilePath(datafilePath)
		val bos = new BufferedOutputStream(
			new FileOutputStream(savefilePath));
		bos.write(bytes)
		bos.close()
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

	def read4fileIfexist(datafilePath: String): Option[DataSource] = {
		if (!isSerializable) {
			return None
		}
		val savefilePath = getSaveFilePath(datafilePath)
		val file = new File(savefilePath)
		if (file.exists() && isSerializable) {
			//Kryo
			val fc = new RandomAccessFile(file, "r").getChannel
			val byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size).load
			System.out.println(byteBuffer.isLoaded)
			val bytes = new Array[Byte](fc.size().toInt)
			if (byteBuffer.remaining > 0) {
				byteBuffer.get(bytes, 0, byteBuffer.remaining)
			}
			val datasource = KryoUtil.deserializationObject(bytes, classOf[DataSource])
			Some(datasource)
		}
		else {
			None
		}
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

	private def setBoardClick(): Unit = {
		val map = LtpcDetector.SourceBoardMap.asScala
		map.foreach(k => {
			val sum = k._2.getLtpcChannels.asScala.map(l => l.getClickCount).sum
			k._2.setClickCount(sum)
		})
	}

	def setChannelMap(): Unit = {
		val channels = ReadConfig.getLtpcDetector.getChannels.asScala
		channels.foreach(c => {
			ChannelMap.put((c.getSourceBoardNum, c.getChannelId), c)
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
		StringUtil.byteArrayToInt(arr, start)==num
	}

	//处理单个数据包
	private def processSimpleData(arr: Array[Byte]): Option[Array[SimplifyData]] = {
		if (arr.length<22){
			println(" package short than 22")
			return None
		}
		if ( !compareInt(arr, 0, header)) {
			println("Trigger " + ((arr(20) & 0xff) << 8 | (arr(21) & 0xff)) + " package header is not complete")
			return None
		}
		val channelId = arr(12) & 0x3f
		ChannelMap.get((arr(9) & 0xff, channelId)) match {
			case Some(ltpcChannel) => {
				ltpcChannel.setClickCount(ltpcChannel.getClickCount + 1)

				val simpleLength = arr(13).toInt * 16
				val dataPoints = new Array[Short](simpleLength)
				try {
					for (i <- 0 to dataPoints.length - 1) {
						dataPoints(i) =
							((arr(20 + 2 * i) & 0x0f) << 8 |
								(arr(21 + 2 * i) & 0xff)).toShort
					}
				} catch {
					case e: ArrayIndexOutOfBoundsException => println("ArrayIndexOutOfBoundsException")
				}

				val planeWithTracks = ltpcChannel.getPlaneWithTracks
				val piece = planeWithTracks.length
				var Size = simpleLength / piece + 1
				var start = 0
				var end = 0
				val sfdArr = new Array[SimplifyData](piece)
				for (i <- 0 until piece) {
					val sd = new SimplifyData
					end = start + Size * (i + 1)
					if (end > dataPoints.length)
						end = dataPoints.length - 1
					val max = findMax(dataPoints, start, end)
					sd.setLtpcChannel(ltpcChannel)
					sd.setTriggerNum((arr(20) & 0xff) << 8 | (arr(21) & 0xff))
					sd.setTrackerNum(planeWithTracks(i).getTracker.trackerNum)
					sd.setCharge(dataPoints(max).toInt)
					sd.setPID(ltpcChannel.getPid)
					sd.setPlaneNum(planeWithTracks(i).getPlane.planeNum)
					if (sd.getTriggerNum <= cacheTriggerNum)
						sd.setShorts(dataPoints)
					start += Size
					sfdArr(i) = sd
				}
				Some(sfdArr)
			}
			case None => {
				println(s"(${arr(9).toInt}.toInt,${channelId}) not match channel")
				None
			}
		}
	}
}

