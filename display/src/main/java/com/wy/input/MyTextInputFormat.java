package com.wy.input; /**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/15 5:49
 */

import com.google.common.base.Charsets;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.Serializable;

/**
 * @program: FlinkDemo
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-10-15 05:49
 **/
public class MyTextInputFormat extends TextInputFormat implements Serializable {
    @Override
    public RecordReader<LongWritable, Text>
    createRecordReader(InputSplit split,
                       TaskAttemptContext context) {
        String delimiter = context.getConfiguration().get(
                "textinputformat.record.delimiter");
        byte[] recordDelimiterBytes = null;
        if (null != delimiter) {
            recordDelimiterBytes = delimiter.getBytes(Charsets.ISO_8859_1);
        }
        return new LineRecordReader(recordDelimiterBytes);
    }
}
