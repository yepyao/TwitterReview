package models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;

import database.Weibo;
import database.dbConnector;

public class MISum {
	HashMap<String, Double> miMap;

	public MISum() {
		try {
			miMap = new HashMap<String, Double>();
			BufferedReader reader = new BufferedReader(new FileReader(
					"dict_MI.txt"));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				String[] arr = temp.split(",");
				if (Double.parseDouble(arr[2]) > 0)
					miMap.put(arr[1], Double.parseDouble(arr[2]));
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void calcScore() {

		try {
			dbConnector conn = new dbConnector("weibo");
			
			conn.clearScore("MI_sum_avg");
			int id = 0;
			int batch = 10000;
			LinkedList<Weibo> weibos = null;
			while (true) {
				System.out.println("Begin to fetch...");
				weibos = conn.getWeibo(id, batch);
				id += weibos.size();

				for (Weibo weibo : weibos) {
					String[] arr = weibo.content_segs.split(" ");
					int count = 0;
					double score = 0;
					for (String word : arr) {
						count++;
						if (miMap.containsKey(word))
							score += miMap.get(word);
					}
					if (count == 0)
						count++;
					conn.insertScore(weibo.weiboId, score / count, "MI_sum_avg");
				}
				if (weibos.size() < batch)
					break;
				System.out.println("Seg OK! id: " + id);
				// break;
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public double getSumScore(LinkedList<String> text){
		double sum=0;
		for(String word: text){
			if (miMap.containsKey(word)) sum+=miMap.get(word);
		}
		return sum;
	}
	public double getAvgScore(LinkedList<String> text){
		double sum=0;
		for(String word: text){
			if (miMap.containsKey(word)) sum+=miMap.get(word);
		}
		return sum/text.size();
	}
}
