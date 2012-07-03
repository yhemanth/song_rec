package com.songrec.mappers;

import com.songrec.utils.HashingX;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class UserIdHashMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String userId = value.toString().split("\t")[0];
        context.write(new IntWritable(HashingX.hash(userId)), new Text(userId));
    }
}
