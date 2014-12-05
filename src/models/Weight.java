package models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;

import database.Weibo;
import database.dbConnector;

public class Weight {
	HashMap<String, Integer> dict;

	public Weight() {
		try {
			dict = new HashMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new FileReader(
					"dict.txt"));
			String temp = reader.readLine();;
			while ((temp = reader.readLine()) != null) {
				String[] arr = temp.split(",");
				dict.put(arr[1], Integer.parseInt(arr[2]));
			}
			reader.close();
			reader = new BufferedReader(new FileReader("stopword.txt"));
			while ((temp = reader.readLine()) != null) {
				if (dict.containsKey(temp))
					dict.replace(temp, 0);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void calcScore() {

		try {
			dbConnector conn = new dbConnector("weibo");
			conn.clearScore("weight_log");
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
						if (dict.containsKey(word))
							score += Math.log(dict.get(word)+1);
					}
					if (count == 0)
						count++;
					conn.insertScore(weibo.weiboId, score / count,
							"weight_log");
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

}
