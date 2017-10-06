
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.IntWritable; 
import org.apache.hadoop.io.LongWritable; 
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/**
 *LetterCountMapper: read input file, and map to (key,value), key is low case letter from a-z, 
 * value is count of letters
 *@author jiangtao
 *
 */
public class LetterCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		String line = value.toString();
		Map<Character, Integer> letterFrequency = new HashMap<Character, Integer>();//store the letter frequency

		if (!line.trim().isEmpty()) {//remove empty line

			line = line.toLowerCase().replaceAll("[^a-zA-Z ]", "");// remove punctuation and numbers 

			for(int j = 0; j < line.length(); j++) {
				Character c = line.charAt(j);
				if(Character.isLetter(c)) {
					c = Character.toLowerCase(c);
					Integer count = letterFrequency.get(c);
					letterFrequency.put(c, (count == null) ? 1:count + 1);// add letter to map
				}
			}
		}

		for (Character c : letterFrequency.keySet()) {
			context.write(new Text(c.toString()), new IntWritable(letterFrequency.get(c)));
		}

	}



}
