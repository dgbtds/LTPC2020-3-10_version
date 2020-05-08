package listerner

import javafx.scene.control.{ProgressBar, TextArea}
import org.apache.spark.scheduler._

import scala.collection.mutable

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/30 13:29
 */
class SparkJobListener(jobNum: Int, fileProgressBar: ProgressBar,ConfigLog: TextArea)  extends SparkListener{
	//Job和Job信息（包括总task数，当前完成task数，当前Job百分比）的映射
	private val jobToJobInfo = new mutable.HashMap[Int, (Int, Int, Int)]
	//stageId和Job的映射，用户获取task对应的job
	private val stageToJob = new mutable.HashMap[Int, Int]
	//完成的job数量
	private var finishJobNum = 0
	private var hasException: Boolean = false

	override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = synchronized {
		val appId = applicationStart.appId
		val name = applicationStart.appName
//		ConfigLog.appendText("\n link to spark")
//		ConfigLog.appendText("\n start run application : "+name)
		//记录app的Id，用于后续处理：
		//如：yarn application  -kill  appId
		//handleAppId(appId)
	}

	//获取job的task数量，初始化job信息
	override def onJobStart(jobStart: SparkListenerJobStart) = synchronized {
		val jobId = jobStart.jobId
		val tasks = jobStart.stageInfos.map(stageInfo => stageInfo.numTasks).sum
		ConfigLog.appendText("\n start run job : "+jobId)
		jobToJobInfo += (jobId ->(tasks, 0, 0))
		jobStart.stageIds.map(stageId => stageToJob(stageId) = jobId)
		var progess = jobId.toDouble / jobNum.toDouble
		if (progess>1) {
			progess=1
		}
			fileProgressBar.setProgress(progess)
	}

	//task结束时，粗略估计当前app执行进度。
	//估算方法：当前完成task数量/总task数量。总完成task数量按（job总数*当前job的task数。）
	override def onTaskEnd(taskEnd: SparkListenerTaskEnd) = synchronized {
		val stageId = taskEnd.stageId
		val jobId = stageToJob.get(stageId).get
		val (totalTaskNum: Int, finishTaskNum: Int, percent: Int) = jobToJobInfo.get(jobId).get
		val currentFinishTaskNum = finishTaskNum + 1
		val newPercent = currentFinishTaskNum * 100 / (totalTaskNum * jobNum)
		jobToJobInfo(jobId) = (totalTaskNum, currentFinishTaskNum, newPercent)

	}

	//job 结束，获取job结束的状态，异常结束可以将异常的类型返回处理。
	// handle处理自定义，比如返回给web端，显示异常log。
	override def onJobEnd(jobEnd: SparkListenerJobEnd) = synchronized {
		val jobId = jobEnd.jobId
		jobEnd.jobResult match {
			case JobSucceeded => finishJobNum += 1
			case _ =>
		}
	}

	//app结束时，将程序执行进度标记为 100%。
	//缺陷：SparkListenerApplicationEnd没有提供app的Exception的获取。这样，当程序在driver端出错时，
	//获取不到出错的具体原因返回给前端，自定义提示。比如（driver对app中的sql解析异常，还没有开始job的运行）

	/*** driver 端异常可通过主程序代码里 try catch获取到 ***/

	override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd) = synchronized {
		ConfigLog.appendText("\nApplication end Succeeded!")
		fileProgressBar.setProgress(1)
		val totalJobNum = jobToJobInfo.keySet.size
		val totalPercent = jobToJobInfo.values.map(_._3).sum
		//handle precision lose
		if (!hasException && totalPercent == 99) {
			//      handleAppProgress(100)
		}
		val msg = "执行失败"
	}
}
