import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.util.ArrayList;
import java.util.Collections;


/**
 *KNeighborCalculateReducer calculate the mean score of neighborhood score
 *@author jiangtao
 *
 */
public class KNeighborCalculateReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
	@Override
	public void reduce(Text key, Iterable<IntWritable> values, Context context) 
			throws IOException, InterruptedException {
		int count = 0;
		ArrayList<Integer> scores = new ArrayList<Integer>();//put all scores into ArrayList, which is available to sort

		for (IntWritable value : values) {//get all scores
			scores.add(value.get());
			count ++;
		}
		
		Collections.sort(scores);//sort neighborhood scores
		
		if (count % 2 == 1) {//calculate median
			double median = (double) scores.get(count / 2);
			context.write(key, new DoubleWritable(median)); 
		}else {
			double median = (scores.get(count / 2) + scores.get((count - 1) / 2)) / 2.0 ;
			context.write(key, new DoubleWritable(median)); 
		}
	}
}
