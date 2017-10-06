import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.conf.Configuration;

import java.util.logging.Logger;

/**
 *KNeighborScores: Include main function. Calculate k neighborhood score from corpus, and output results. 
 *@author jiangtao
 *
 */
public class KNeighborScores {
	private static final Logger logger = Logger.getLogger("io.saagie.example.hdfs.Main");//to print in hadoop
	private static String INPUT_PATH;
	private static String OUTPUT_PATH;
	private static String K_NEIGHBORS;

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			INPUT_PATH = "/kneighbor/input";
			OUTPUT_PATH = "/kneighbor/output";
			K_NEIGHBORS = "2";
		}else {
			INPUT_PATH = args[0];
			OUTPUT_PATH = args[1];
			K_NEIGHBORS = args[2];
		}

			runApplication();//run program

	}



	public static void runApplication() throws Exception{
		logger.info("1st round mapreduce to get number of letters");
		long d1 = System.nanoTime();
		@SuppressWarnings("deprecation")
		Job job1 = new Job(); 
		job1.setJarByClass(KNeighborScores.class); 
		job1.setJobName("letter count");
		FileInputFormat.setInputDirRecursive(job1, true);
		FileInputFormat.addInputPath(job1, new Path(INPUT_PATH));
		Configuration conf = new Configuration();
		FileSystem f = FileSystem.get(conf);
        if(f.exists(new Path(OUTPUT_PATH)))  f.delete(new Path(OUTPUT_PATH),true);//delete path if exist before
		FileOutputFormat.setOutputPath(job1, new Path(OUTPUT_PATH + "/letterfrequency"));
		job1.setMapperClass(LetterCountMapper.class);
		job1.setReducerClass(LetterCountReducer.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);

		// *********************************************************************************************************//		
		//read first round reduce output from HDFS, and calculate the letter score，put the letter score into
		//running configuration

		if (job1.waitForCompletion(true)) {//if 1st round mapreduce job finished
			logger.info("get total number of letters from 1st round mapreduce result");
			conf.set("kneighbors", K_NEIGHBORS);// add K neighbors to configuration
			FileSystem fs = FileSystem.get(conf);
			int totalLetters = 0;
			for (FileStatus fileStat : fs.globStatus(new Path(OUTPUT_PATH + "/letterfrequency/" + "part-r-*"))) {
				//calculate total number of letters

				BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(fileStat.getPath())));
				String nextLine;
				while ((nextLine = reader.readLine()) != null) {
					String tokens[] = nextLine.split("\t");
					totalLetters += Integer.parseInt(tokens[1]);
				}
				reader.close();
			}


			for (FileStatus fileStat : fs.globStatus(new Path(OUTPUT_PATH + "/letterfrequency/" + "part-r-*"))) {
				//calculate letter score, and put score into running configuration

				BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(fileStat.getPath())));
				String nextLine;
				while ((nextLine = reader.readLine()) != null) {
					String tokens[] = nextLine.split("\t");
					double chCount = Double.parseDouble(tokens[1]);

					if (chCount / totalLetters >= 0.1) conf.set(tokens[0], "0");
					else if (chCount / totalLetters >= 0.08)conf.set(tokens[0], "1");
					else if (chCount / totalLetters >= 0.06)conf.set(tokens[0], "2");
					else if (chCount / totalLetters >= 0.04)conf.set(tokens[0], "4");
					else if (chCount / totalLetters >= 0.02)conf.set(tokens[0], "8");
					else if (chCount / totalLetters >= 0.01)conf.set(tokens[0], "16");
					else conf.set(tokens[0], "32");
				}
				reader.close();
			}
				

				// *********************************************************************************************************//		
				//start 2nd round mapreduce job2 using configuration containing letter scores
				
				@SuppressWarnings("deprecation")
				Job job2 = new Job(conf);
				job2.setJarByClass(KNeighborScores.class); 
				job2.setJobName("get neighbor score");
				FileInputFormat.setInputDirRecursive(job2, true);
				FileInputFormat.addInputPath(job2, new Path(INPUT_PATH));
				FileOutputFormat.setOutputPath(job2, new Path(OUTPUT_PATH + "/kneighbor_score_result"));
				job2.setMapperClass(KNeighborCalculateMapper.class);
				job2.setReducerClass(KNeighborCalculateReducer.class);
				job2.setOutputKeyClass(Text.class);
				job2.setOutputValueClass(IntWritable.class);
				if (job2.waitForCompletion(true)){
					long d2 = System.nanoTime();
					logger.info(String.valueOf((d2 - d1) / 1000000));
					System.exit(0);
				}
				

			}else {
			System.exit(1);
		}
	}

}
