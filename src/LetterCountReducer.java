import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
/**
 *LetterCountReducer: calculate the frequency of each key
 *@author jiangtao
 *
 */
public class LetterCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
	@Override
	public void reduce(Text key, Iterable<IntWritable> values, Context context) 
			throws IOException, InterruptedException {
		int frequency = 0; 
		for (IntWritable value : values) {
			frequency = frequency + value.get();
		}
		context.write(key, new IntWritable(frequency)); 
	}
}
