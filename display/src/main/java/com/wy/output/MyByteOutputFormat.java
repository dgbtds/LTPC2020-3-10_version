package com.wy.output;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/15 21:10
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @program: FlinkDemo
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-10-15 21:10
 **/
public class MyByteOutputFormat extends FileOutputFormat<NullWritable, ByteWritable> {
    @Override
    public RecordWriter<NullWritable, ByteWritable> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
        Path file = super.getDefaultWorkFile(job, "");
        return new MyByteRecordWriter(job,file);
    }
}
