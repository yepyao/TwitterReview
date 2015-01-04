package models;

import helper.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

import sun.swing.text.CountingPrintable;

public class NB {
	HashMap<String, Word> dict;
	double pos, neg;

	public NB() {
		dict = new HashMap<String, Word>();
		getModel();
	}

	public void getModel() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"dict.txt"));
			PrintStream outp = new PrintStream("nb.txt");
			String temp = reader.readLine();
			double pos_num = Integer.parseInt(temp);
			while ((temp = reader.readLine()) != null) {
				String[] arr = temp.split(",");
				int id = Integer.parseInt(arr[0]);
				String word = arr[1];
				int times = Integer.parseInt(arr[2]);
				Word w = new Word(id, word);
				w.times = times;
				dict.put(word, w);
			}
			reader.close();
			reader = new BufferedReader(new FileReader("dict_timeline.txt"));
			temp = reader.readLine();
			double neg_num = Integer.parseInt(temp);
			while ((temp = reader.readLine()) != null) {
				String[] arr = temp.split(",");
				int id = Integer.parseInt(arr[0]);
				String word = arr[1];
				int times_timeline = Integer.parseInt(arr[2]);
				dict.get(word).times_timeline = times_timeline;
			}
			reader.close();

			pos = pos_num / (pos_num + neg_num);
			neg = 1 - pos;

			for (Word w : dict.values()) {
				w.pos_NB = (w.times + 100) / (pos_num + 200);
				w.neg_NB = (w.times_timeline + 100) / (neg_num + 200);
				outp.println(w.word + "\t" + w.pos_NB + "\t" + w.neg_NB);
			}
			System.out.println("NB model ok!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public double getScore(LinkedList<String> text) {
		double pos_logsum, neg_logsum;
		pos_logsum = Math.log(pos);
		neg_logsum = Math.log(neg);
		// System.out.println(pos+" "+neg);
		for (String word : text) {
			if (!dict.containsKey(word)) {
				continue;
			}
			Word w = dict.get(word);
			pos_logsum += Math.log(w.pos_NB);
			neg_logsum += Math.log(w.neg_NB);
		}
		// System.out.println(pos_logsum+" "+neg_logsum);
		double p = Math.exp(pos_logsum)
				/ (Math.exp(pos_logsum) + Math.exp(neg_logsum));
		// System.out.println(p);
		// double p = pos_logsum/(pos_logsum+neg_logsum+1);
		return p;
	}

}
