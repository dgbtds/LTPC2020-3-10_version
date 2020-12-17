package sumit

import java.io.BufferedOutputStream
import java.util
import java.util.Random

import com.wy.model.decetor.LtpcChannel
import com.wy.output.MyByteOutputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{ByteWritable, NullWritable}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/15 8:54
 */
object BaseJob {
	var pckNumWrite = 10
	val External_trigger_timestamp = 0xff00ffff0000L

	case class datapck(pckType: String, trigger: Int, tracker: Int, time_left: Int, time_right: Int, board: Int, channelId: Int, splitstart: Int, min_point: Int, max_point: Int, points: String)

	def setTriggerData(trigger_bytes: ListBuffer[Byte], board_channelId_trackers: Map[(Int, Int), Array[Int]], External_trigger_timestamp: Long, triggerNum: Int): Unit = {
		val external_trigger_timestamp = External_trigger_timestamp + triggerNum * 100
		val bytes0 = Array(
			0x1e.toByte, 0xad.toByte, 0xc0.toByte, 0xde.toByte,
			0x0.toByte, 0x0.toByte, 0x0.toByte, 0x0.toByte,
			0x0.toByte, 0x0.toByte, 0x0.toByte, 0x0.toByte,
			0xc0.toByte, 0x00.toByte, (triggerNum >> 8).toByte, (triggerNum & 0xff).toByte,
			0x0.toByte, 0x0.toByte, 0x0.toByte, 0x0.toByte,
			0x0.toByte, 0x0.toByte, (external_trigger_timestamp >> 40).toByte, ((external_trigger_timestamp >> 32) & 0xff).toByte,
			((external_trigger_timestamp >> 24) & 0xff).toByte, ((external_trigger_timestamp >> 16) & 0xff).toByte, ((external_trigger_timestamp >> 8) & 0xff).toByte, (external_trigger_timestamp & 0xff).toByte,
			0x5a.toByte, 0x5a.toByte, 0x5a.toByte, 0x5a.toByte)
		trigger_bytes ++= bytes0
		val random = new Random
		board_channelId_trackers.foreach(kv => {
			val bytes1 = ListBuffer[Byte](
				0x1e.toByte, 0xad.toByte, 0xc0.toByte, 0xde.toByte,
				0x0.toByte, 0x0.toByte, 0x0.toByte, 0x0.toByte,
				0x0.toByte, kv._1._1.toByte, 0x0.toByte, 0x1.toByte,
				(2 << 6 | (kv._1._2 & 0x3f)).toByte, 0x12.toByte, 0x0.toByte, 0x0.toByte,
				0x0.toByte, 0x0.toByte, 0x0.toByte, 0x0.toByte,
				(triggerNum >> 8).toByte, (triggerNum & 0xff).toByte, (external_trigger_timestamp >> 40).toByte, ((external_trigger_timestamp >> 32) & 0xff).toByte,
				((external_trigger_timestamp >> 24) & 0xff).toByte, ((external_trigger_timestamp >> 16) & 0xff).toByte, ((external_trigger_timestamp >> 8) & 0xff).toByte, (external_trigger_timestamp & 0xff).toByte)
			for (_ <- 0 until 288) {
				val nextInt = random.nextInt(1000)
				bytes1.append((nextInt >> 8).toByte)
				bytes1.append((nextInt & 0xff).toByte)
			}
			bytes1.append(0x5a.toByte)
			bytes1.append(0x5a.toByte)
			bytes1.append(0x5a.toByte)
			bytes1.append(0x5a.toByte)
			trigger_bytes ++= bytes1
		})
	}

	def createMap(sc: SparkContext, spark: SparkSession): Map[(Int, Int), Array[Int]] = {
		def resolveString(s: String): ((Int, Int), Array[Int]) = {
			val strings = s.split(" ")
			val key = (strings(0).trim().toInt, strings(1).trim().toInt)
			val trackers = new Array[Int](strings.length - 2)
			for (i <- 2 until strings.length) {
				val int = strings(i).toInt
				trackers(i - 2) = int
			}
			(key, trackers)
		}

		val map_path = "file:///" + "C:\\javaProject\\idealProject\\LTPC2020-3-10_version\\display\\src\\main\\resources\\trackmap.txt"
		sc.hadoopConfiguration.set("textinputformat.record.delimiter", "\r\n")
		sc.textFile(map_path, 1).map(s => {
			resolveString(s)
		}).collect().toMap
	}

	def creatDataBySpark(sc: SparkContext, trigger: Int, numPartttion: Int, spark: SparkSession, pathString: String): Unit = {
		sc.hadoopConfiguration.set("textinputformat.record.delimiter", "")
		val board_channelId_trackers = createMap(sc, spark)
		val path = new Path(pathString)
		val conf = new Configuration(sc.hadoopConfiguration)
		val fs = path.getFileSystem(conf)
		if (fs.exists(path)) {
			fs.delete(path, true)
		}
		val rddData = sc.makeRDD(0 until (trigger), numPartttion).mapPartitions(
			iter => {
				val partitionData = ListBuffer[Array[Byte]]()
				while (iter.hasNext) {
					val i = iter.next()
					val trigger_bytes = ListBuffer[Byte]()
					setTriggerData(trigger_bytes, board_channelId_trackers, External_trigger_timestamp, i)
					partitionData += trigger_bytes.toArray
				}
				partitionData.toArray.iterator
			}
		)
		rddData.map((NullWritable.get(), _))
			.saveAsNewAPIHadoopFile(pathString, classOf[NullWritable], classOf[ByteWritable],
				classOf[MyByteOutputFormat])

	}

	def creatData(sc: SparkContext, trigger: Int, spark: SparkSession, pathString: String): Unit = {
		val board_channelId_trackers = createMap(sc, spark)
		val path = new Path(pathString)
		val conf = new Configuration(sc.hadoopConfiguration)
		val fs = path.getFileSystem(conf)
		if (fs.exists(path)) {
			fs.delete(path, false)
		}
		val out = new BufferedOutputStream(fs.create(path))
		var trigger_bytes = ListBuffer[Byte]()
		for (i <- 0 until trigger) {
			setTriggerData(trigger_bytes, board_channelId_trackers, External_trigger_timestamp, i)
			if (pckNumWrite == 0) {
				out.write(trigger_bytes.toArray)
				out.flush()
				trigger_bytes = ListBuffer[Byte]()
			}
			else if ((i + 1) % pckNumWrite == 0) {
				out.write(trigger_bytes.toArray)
				out.flush()
				trigger_bytes = ListBuffer[Byte]()
			}
		}
		out.close()
		fs.close()
	}

	def intToByteArray(a: Int): Array[Byte] = {
		Array(((a >> 24) & 0xFF).toByte, ((a >> 16) & 0xFF).toByte, ((a >> 8) & 0xFF).toByte, (a & 0xFF).toByte)
	}

	def byteArrayToInt(b: Array[Byte], start: Int) = b(3 + start) & 0xFF | (b(2 + start) & 0xFF) << 8 | (b(1 + start) & 0xFF) << 16 | (b(start) & 0xFF) << 24

	def splitData(arr: Array[Byte], BC_trackers: Map[(Int, Int), Array[Int]]) = {
		val trigger = (arr(16) & 0xff) << 8 | (arr(17) & 0xff)
		val channelId = arr(8) & 0x3f
		val board = arr(5) & 0xff
		val time_left = (arr(18) & 0xff) << 8 | (arr(19) & 0xff)
		val time_right = byteArrayToInt(arr, 20)
		if (BC_trackers.contains((board, channelId))) {
			val trackers = BC_trackers.get((board, channelId))
			val size = trackers.get.length
			val simpleLength = arr(9).toInt * 16

			val outs = new Array[Array[Int]](size)
			var length = (simpleLength / size)
			var pointIndex = 24

			for (i <- 0 until size) {
				if (i == size - 1) {
					length = simpleLength % size + length
				}
				val splitArr = new Array[Int](length + 9)
				splitArr(0) = trigger
				splitArr(1) = trackers.get(i)
				splitArr(2) = time_left
				splitArr(3) = time_right
				splitArr(4) = board
				splitArr(5) = channelId
				val start = i * length
				//splitstart
				splitArr(6) = 1 + start
				var min_point = Int.MaxValue
				var max_point = Int.MinValue
				for (j <- 0 until length) {
					splitArr(9 + j) = (arr(pointIndex) & 0x0f) << 8 | (arr(pointIndex + 1) & 0xff)
					pointIndex += 2
					min_point = if (min_point > splitArr(9 + j)) {
						splitArr(9 + j)
					} else {
						min_point
					}
					max_point = if (max_point < splitArr(9 + j)) {
						splitArr(9 + j)
					} else {
						max_point
					}
				}
				splitArr(7) = min_point
				splitArr(8) = max_point
				outs(i) = splitArr
			}
			if (pointIndex + 4 != arr.length) {
				print("SamplePoint error")
			}
			if (byteArrayToInt(arr, pointIndex) != 0x5a5a5a5a) {
				print("Trailer error")
			}
			outs
		}
		else {
			Array[Array[Int]]()
		}
	}

	def splitDataSingle(arr: Array[Byte], BC_trackers: util.HashMap[String, LtpcChannel]):Array[datapck] = {
		val trigger = (arr(16) & 0xff) << 8 | (arr(17) & 0xff)
		val channelId = arr(8) & 0x3f
		val board = arr(5) & 0xff
		val time_left = (arr(18) & 0xff) << 8 | (arr(19) & 0xff)
		val time_right = byteArrayToInt(arr, 20)
		if (BC_trackers.containsKey(board + "," + channelId)) {
			val ltpcChannel = BC_trackers.get(board + "," + channelId)
			val trackers = ltpcChannel.getTrackNums
			val size = trackers.length
			val simpleLength = arr(9).toInt * 16

			val outs = new Array[datapck](size)
			var length = (simpleLength / size)
			var pointIndex = 24
			val trailer = byteArrayToInt(arr, arr.length - 4)

			for (i <- 0 until size) {
				if (trailer != 0x5a5a5a5a) {
					outs(i) = BaseJob.datapck("error", trigger, trackers(i), time_left, time_right
						, board, channelId, 1 + i * length, 0, 0, "trailer is error")
				}
				else {
					if (i == size - 1) {
						length = simpleLength % size + length
					}
					val builder = new StringBuilder
					var min_point = Int.MaxValue
					var max_point = Int.MinValue
					for (_ <- 0 until length) {
						val charge = (arr(pointIndex) & 0x0f) << 8 | (arr(pointIndex + 1) & 0xff)
						builder.append(String.valueOf(charge) + ";")
						pointIndex += 2
						min_point = if (min_point > charge) {
							charge
						} else {
							min_point
						}
						max_point = if (max_point < charge) {
							charge
						} else {
							max_point
						}
					}
					outs(i) = BaseJob.datapck("sample", trigger, trackers(i), time_left, time_right
						, board, channelId, 1 + i * length, min_point, max_point, builder.toString())
				}
			}
			outs
		}
		else {
			val outs = new Array[datapck](1)
			outs(0)= datapck("error", -1, 0, 0, 0, 0, 0, 0, 0, 0, "board: " + board + " ,channelID: " + channelId + " not Exist")
			outs
		}
	}

}
