package code;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class IFIDF {

	// Total number of words in the document
	public static Map<String, Long> numWordsPerDoc = new ConcurrentHashMap<>();

	// Total number of documents containing the given word
	public static Map<String, Long> totalDocsForWord = new ConcurrentHashMap<>();

	public static class Mapper1 extends Mapper<Object, Text, Text, IntWritable> {

		private Text term = new Text();
		HashSet<String> set = new HashSet<String>();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {

				String source = itr.nextToken().toString();
				term.set(source);
				String fileName = ((FileSplit) context.getInputSplit()).getPath().getName().toString();

				String termKey = term.toString() + ":::" + fileName;
				context.write(new Text(termKey), new IntWritable(1));

			}
		}
	}

	public static class Reducer1 extends Reducer<Text, IntWritable, Text, Text> {
		private Text result = new Text();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int temp = 0;
			for (IntWritable val : values) {
				temp = temp + val.get();
			}

			Text t = new Text("+++" + String.valueOf(temp));
			result.set(t);
			StringTokenizer tk = new StringTokenizer(key.toString(), ":::");
			tk.nextToken();
			String name = tk.nextToken().toString();
			if (numWordsPerDoc.containsKey(name))
				numWordsPerDoc.put(name, numWordsPerDoc.get(name) + temp);
			else
				numWordsPerDoc.put(name, new Long(temp));
			context.write(key, t);
		}
	}

	public static class Mapper2 extends Mapper<Object, Text, Text, Text> {

		private Text term = new Text();
		HashSet<String> set = new HashSet<String>();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String word = value.toString().split(":::")[0];
			if (totalDocsForWord.containsKey(word))
				totalDocsForWord.put(word, totalDocsForWord.get(word) + 1);
			else
				totalDocsForWord.put(word, new Long(1));

			context.write(new Text(value.toString().split("\t")[0]), value);
		}
	}

	public static class Reducer2 extends Reducer<Text, Text, Text, DoubleWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			double tf = 0.0d;
			String stars = "";
			String filename = "";
			for (Text val : values) {
				stars = val.toString().split("\\+\\+\\+")[1];
				filename = val.toString().split("\t")[0];
				filename = filename.split(":::")[1];
			}

			// tf = Double.valueOf(stars) / numWordsPerDoc.get(filename);
			
			//doubel temp = Double.valueOf(stars);

			//double nr = (stars == null || stars.trim() == "") ? 0.0d : Double.valueOf(stars);
			double nr = (stars == null || stars.trim().equals("")) ? 0.0d : Double.valueOf(stars);
			double dr = numWordsPerDoc.get(filename) == null ? 1.0 : numWordsPerDoc.get(filename);

			tf = (nr) / (dr);

			// tf = Double.valueOf(stars == null ? 0.0 : stars)
			// / (numWordsPerDoc.get(filename) == null ? 1.0 :
			// numWordsPerDoc.get(filename) == null ? 1.0 :
			// numWordsPerDoc.get(filename));
			String word = key.toString().split(":::")[0];
			Long count = totalDocsForWord.get(word);
			Double idf = Math.log10(556 / count);
			Double tf_idf = idf * tf;
			context.write(key, new DoubleWritable(tf_idf));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Word Count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(Mapper1.class);
		job.setReducerClass(Reducer1.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);

		Job job2 = Job.getInstance(conf, "Word Count 2");
		job2.setJarByClass(WordCount.class);
		job2.setMapperClass(Mapper2.class);//
		job2.setReducerClass(Reducer2.class);
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(DoubleWritable.class);
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		System.exit(job2.waitForCompletion(true) ? 0 : 1);

	}
}