package hbaseControl

import java.util

import com.wy.model.data.DataSource
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{ConnectionFactory, _}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/6/23 18:36
 */
object hbasetest {
	var connection: Option[Connection] = None
	var configuration: Option[Configuration] = None

	def get_configuration():Configuration= {
		configuration.getOrElse({
		val conf = HBaseConfiguration.create()
		//设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
		conf.set("hbase.zookeeper.quorum", "127.0.0.1")
		//设置zookeeper连接端口，默认2181
		conf.set("hbase.zookeeper.property.clientPort", "2181")
		conf
	})
	}

	def get_ConnectionPool(tablename: String): Connection = {
		connection.getOrElse(ConnectionFactory.createConnection(get_configuration()))
	}

	def notExist(tablename: String, filepath: String): Boolean = {
		val cfs = new Array[String](86)
		cfs(0) = "Time"
		cfs(1) = "Limit"
		//all samplePoint
		for (i <- 1 to 42) {
			cfs(i+1) = "Tracker_" + i
		}
		//只记录一个能量
		for (i <- 1 to 42) {
			cfs(i+43) = "chargeTracker_" + i
		}
		createTable(tablename, cfs)
		val cfs2 = new Array[String](1)
		cfs(0) = "info"
		cfs(1) = "ClickCount"
		createTable("DataSource", cfs2)

		val table = connection.getOrElse(get_ConnectionPool("DataSource")).getTable(TableName.valueOf("DataSource"))
		val get = new Get(Bytes.toBytes(filepath))
		val result = table.get(get)
		result.isEmpty
	}


	def HbaseReadCF(tablename: String, Trackerid: Int, row_prefix: String): util.Iterator[Result] = {

		val table = connection.getOrElse(get_ConnectionPool(tablename)).getTable(TableName.valueOf(tablename))

		val startRow: String = row_prefix + "-0"
		val endRow: String = row_prefix + "-0"
		//scan
		val scan = new Scan().withStartRow(Bytes.toBytes(startRow)).withStopRow(Bytes.toBytes(endRow), true)
		scan.addFamily(Bytes.toBytes("Tracker_" + Trackerid))
		val resultScanner = table.getScanner(scan)
		val results = resultScanner.iterator();
		resultScanner.close()

		if (table != null) table.close();
		results
	}

	def HbaseRead(tablename: String, sc: SparkContext): Unit = {
		//设置读取的表
		get_configuration().set(TableInputFormat.INPUT_TABLE, tablename)
		val resultRDD: RDD[(ImmutableBytesWritable, Result)] = sc.newAPIHadoopRDD(
			get_configuration(),
			classOf[TableInputFormat], //设置数据读入格式
			classOf[ImmutableBytesWritable], //设置输入数据key的类型
			classOf[Result] //设置输入数据value的类型
		)

		//取出数据
		resultRDD.foreach {
			case (key, result) =>
				val rowkey = Bytes.toString(key.get())
				//输出rowkey
				println(rowkey)
				//获取每一个字段的值
				for (cell <- result.rawCells) {
					val cf = Bytes.toString(CellUtil.cloneFamily(cell))
					val filed = Bytes.toString(CellUtil.cloneQualifier(cell))
					val value = Bytes.toString(CellUtil.cloneValue(cell))
					println(cf + ":" + filed + ":" + value)
				}
		}
	}


	def createTable( tablename: String, ColumnFamilys: Array[String]): Unit = {
		val hbaseConn = ConnectionFactory.createConnection(get_configuration())
		val admin = hbaseConn.getAdmin
		//如果不存在就创建表
		if (!admin.tableExists(TableName.valueOf(tablename))) {
			val desc = TableDescriptorBuilder.newBuilder(TableName.valueOf(tablename))
			//指定列簇 不需要创建列，列式存储不需要创建列
			ColumnFamilys.foreach(s => {
				val cf1 = ColumnFamilyDescriptorBuilder.newBuilder(s.getBytes()).build()
				desc.setColumnFamily(cf1)
			})
			admin.createTable(desc.build())
		}
	}
	def HbaseWrite_DataSource(tablename: String,datasource:DataSource,clickMap:mutable.Map[(Int,Int),Int]): Unit ={
		val table = connection.getOrElse(get_ConnectionPool(tablename)).getTable(TableName.valueOf(tablename))

		val put = new Put(Bytes.toBytes(datasource.getFilePath))
		put.addColumn(
			Bytes.toBytes("info"), //列簇信息
			Bytes.toBytes("RawDataPackageCount"), //字段
			Bytes.toBytes(datasource.getRawDataPackageCount+"")
		)
		put.addColumn(
			Bytes.toBytes("info"), //列簇信息
			Bytes.toBytes("Trigger_pckCount"), //字段
			Bytes.toBytes(datasource.getTrigger_pckCount)
		)
		put.addColumn(
			Bytes.toBytes("info"), //列簇信息
			Bytes.toBytes("TriggerCount"), //字段
			Bytes.toBytes(datasource.getTriggerCount)
		)
		put.addColumn(
			Bytes.toBytes("info"), //列簇信息
			Bytes.toBytes("AllPackageCount"), //字段
			Bytes.toBytes(datasource.getAllPackageCount)
		)
		clickMap.foreach(kv=>
			put.addColumn(
				Bytes.toBytes("ClickCount"), //列簇信息
				Bytes.toBytes(kv._1._1+","+kv._1._2), //字段
				Bytes.toBytes(kv._2+"")
			)
		)
		table.put(put)
		table.close()
	}
	def HbaseWriteLtpc( tablename: String, outRdd: RDD[Array[Int]], timemap: Map[Int, (Int,Int)], row_prefix: String
										,trigger_chargeMax:mutable.HashMap[Int, Int],trigger_chargeMin:mutable.HashMap[Int, Int]): Unit = {

		//初始化jobconf，TableOutputFormat必须是org.apache.hadoop.hbase.mapred包下的！
		val jobConf = new JobConf(get_configuration())
		jobConf.setOutputFormat(classOf[TableOutputFormat])
		jobConf.set(TableOutputFormat.OUTPUT_TABLE, tablename)


		val rdd = outRdd.map {
			arr => {
				//RowKey
				val rowkey = new ImmutableBytesWritable(Bytes.toBytes(row_prefix + "-" + arr(0)))
				//put对象
				val put = new Put(rowkey.get())
				if (!(timemap contains (arr(0)))) {
					throw new RuntimeException("trigger no start time Pck")
				}
				put.addColumn(
					Bytes.toBytes("Time"), //列簇信息
					Bytes.toBytes("Pck_start"), //字段
					Bytes.toBytes(timemap(arr(0))._1+"-"+timemap(arr(0))._2)
				)
				put.addColumn(
					Bytes.toBytes("Limit"), //列簇信息
					Bytes.toBytes("maxCharge"), //字段
					Bytes.toBytes(trigger_chargeMax(arr(0)))
				)
				put.addColumn(
					Bytes.toBytes("Limit"), //列簇信息
					Bytes.toBytes("minCharge"), //字段
					Bytes.toBytes(trigger_chargeMin(arr(0)))
				)
				val points = new StringBuilder
				points ++= ("time:" + arr(2) + arr(3) + ",")
				for (i <- 6 until arr.length)
					points ++= (arr(i) + ",")
				put.addColumn(
					Bytes.toBytes("Tracker_" + arr(1)), //列簇信息
					Bytes.toBytes("Position," + arr(4) + "," + arr(5)), //字段
					Bytes.toBytes(points.toString())
				)
				put.addColumn(
					Bytes.toBytes("chargeTracker_" + arr(1)), //列簇信息
					Bytes.toBytes("Position," + arr(4) + "," + arr(5)), //字段
					Bytes.toBytes(arr.drop(6).max+"")
				)
				//转化成RDD[(ImmutableBytesWritable,Put)]类型才能调用saveAsHadoopDataset
				(new ImmutableBytesWritable, put)
			}
		}

		rdd.saveAsHadoopDataset(jobConf)

	}
}
