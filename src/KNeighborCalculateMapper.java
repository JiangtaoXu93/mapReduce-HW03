import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/**
 *KNeighborCalculateMapper: Mapper class to provide word  as key and each neighbor score as value
 *@author jiangtao
 *
 */
public class KNeighborCalculateMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		Configuration conf = (Configuration) context.getConfiguration();//get configuration
		int k_neighborhood = Integer.parseInt(conf.get("kneighbors"));// get K from configuration
		
		String line = value.toString();
		if (!line.trim().isEmpty()) {//remove empty line
			String[] words = line.toLowerCase().replaceAll("[^a-zA-Z ]", "").split(" ");
			 // remove punctuation, numbers, and split by space
			for (int i = 0; i < words.length; i++) {
				if (!words[i].equals("")) {
					List<String> neighborWords = new ArrayList<String>();
					int count = 1;
					while (i - count >= 0) {//add front neighbor to list
						if (count > k_neighborhood) break;
						neighborWords.add(words[i - count]);
						count++;
					}
					count = 1;
					while (i + count < words.length) {//add back neighbor to list
						if (count > k_neighborhood) break;
						neighborWords.add(words[i + count]);
						count++;
					}
					
					int score = countSingleWordNeighbor(neighborWords, conf);//calculate score from list of words
					context.write(new Text(words[i]), new IntWritable(score));//write to reduce
									
				}
				
			}


		}


	}
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//given the array of neighbor words, calculate the score		
		public static int countSingleWordNeighbor(List<String> neighbors, Configuration conf) {
			int score = 0;
			for (int i = 0; i < neighbors.size(); i++) {
				String w = neighbors.get(i);
				if (w != null) {
					for (int j = 0; j < w.length(); j++ ) {
						String ch = String.valueOf(w.charAt(j));//convert character to string 
						score = score + Integer.parseInt(conf.get(ch));// get score from configuration
					}
				}
			}
			
			return score;
		}

}
