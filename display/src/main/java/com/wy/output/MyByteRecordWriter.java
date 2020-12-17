package com.wy.output;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/15 21:13
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * @program: FlinkDemo
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-10-15 21:13
 **/
public class MyByteRecordWriter<K, V> extends RecordWriter<K, V> {
    private FileSystem fs;
    private Configuration conf;
    private BufferedOutputStream out ;
    private  byte[] newline ;

    MyByteRecordWriter(TaskAttemptContext job,Path file) {
        // 初始化一些属性
        try {
            conf = job.getConfiguration();
            fs = FileSystem.get(conf);
            out = new BufferedOutputStream(fs.create(file));
            String delimiter = job.getConfiguration().get(
                    "textinputformat.record.delimiter");
            newline=delimiter.getBytes(StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public synchronized void write(K key, V value)
            throws IOException {

        boolean nullKey = key == null || key instanceof NullWritable;
        boolean nullValue = value == null || value instanceof NullWritable;
        if (nullKey && nullValue) {
            return;
        }
        if (!nullKey) {
            writeObject(key);
        }
        byte[] keyValueSeparator=":".getBytes(StandardCharsets.ISO_8859_1);
        if (!(nullKey || nullValue)) {
            out.write(keyValueSeparator);
        }
        if (!nullValue) {
            writeObject(value);
        }
        out.write(newline);
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        out.close();
    }
    private void writeObject(Object o) throws IOException {
        if (o instanceof byte[]) {
            byte[] to = (byte[]) o;
            out.write(to);
        } else {
            out.write(o.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
