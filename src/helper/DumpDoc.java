package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text;

import models.Jahmm;
import models.MISum;
import models.NB;
import database.Dianping;
import database.NewWeibo;
import database.Textpart;
import database.Weibo;
import database.dbConnector;

public class DumpDoc {
	int doc_size;
	int wid;
	HashMap<String, Word> dict;

	public DumpDoc() {
		dict = new HashMap<String, Word>();
		wid = 1;
	}

	public void removeTimelineDup() {
		try {
			HashSet<String> ids = new HashSet<String>();
			dbConnector conn = new dbConnector("weibo");

			int id = 0;
			int batch = 10000;
			int count = 0;
			LinkedList<Weibo> weibos = null;
			while (true) {
				System.out.println("Begin to fetch...");
				weibos = conn.getWeiboTimeline(id, batch);
				id += weibos.size();

				for (Weibo weibo : weibos) {
					if (ids.contains(weibo.weiboId)) {
						conn.deleteTimeline(weibo.id);
						count++;
						System.out.println("totol delete: " + count
								+ " delete weiboId: " + weibo.weiboId);
					}
					ids.add(weibo.weiboId);
				}
				if (weibos.size() < batch / 10)
					break;
				System.out.println("Seg OK! id: " + id);
				// break;
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void calcMI() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"dict.txt"));
			String temp = reader.readLine();
			int doc_num = Integer.parseInt(temp);
			while ((temp = reader.readLine()) != null) {
				// System.out.println(temp);
				String[] arr = temp.split(",");
				int id = Integer.parseInt(arr[0]);
				String word = arr[1];
				dict.put(word, new Word(id, word));
				dict.get(word).times = Integer.parseInt(arr[2]);
			}
			int count = 0;

			try {
				dbConnector conn = new dbConnector("weibo");

				int id = 0;
				int batch = 10000;
				LinkedList<Weibo> weibos = null;
				while (true) {
					System.out.println("Begin to fetch...");
					weibos = conn.getWeiboTimeline(id, batch);

					for (Weibo weibo : weibos) {
						count++;
						if (weibo.id > id)
							id = weibo.id;
						String[] words = weibo.content_segs.split(" ");
						HashSet<String> hashword = new HashSet<String>();
						for (String word : words) {
							if (!isMeaningfulWord(word))
								continue;
							if (!dict.containsKey(word))
								continue;
							if (!hashword.contains(word)) {
								dict.get(word).times_timeline += 1;
								hashword.add(word);
							}
						}
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

			LinkedList<Word> list = new LinkedList<Word>(dict.values());

			Collections.sort(list, new Comparator<Word>() {
				@Override
				public int compare(Word w1, Word w2) {
					return -(w1.times_timeline - w2.times_timeline);
				}

			});

			PrintStream outp = new PrintStream("dict_timeline.txt");
			outp.println(count);
			for (Word word : list) {
				outp.println(word.wid + "," + word.word + ","
						+ word.times_timeline);
				double p_wr = ((double) word.times + doc_num / 1000) / doc_num;
				double p_w = ((double) word.times_timeline + count / 1000)
						/ count;
				word.MI = Math.log(p_wr / p_w);
			}
			outp.close();

			Collections.sort(list, new Comparator<Word>() {
				@Override
				public int compare(Word w1, Word w2) {
					if (w1.MI > w2.MI)
						return -1;
					if (w1.MI < w2.MI)
						return 1;
					return 0;
				}

			});
			outp = new PrintStream("dict_MI.txt");
			for (Word word : list) {
				outp.println(word.wid + "," + word.word + "," + word.MI);
			}
			outp.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void dumpWeiboTextpart() {
		try {
			NB nb = new NB();
			MISum mi = new MISum();
			Jahmm jahmm = new Jahmm(2, 3);
			LinkedList<String> outp_textpart_lines = new LinkedList<String>();
			LinkedList<String> weiboidlist = new LinkedList<String>();
			LinkedList<String> querylist = new LinkedList<String>();
			PrintStream outp_hmm = new PrintStream("hmm/hmm_o.txt");
			double[][] scale = new double[3][2];
			for (int i = 0; i < 3; i++) {
				scale[i][0] = Double.MAX_VALUE;
				scale[i][1] = -Double.MAX_VALUE;
			}

				dbConnector conn = new dbConnector("weibo");
				int id = 0;
				int batch = 10000;
				LinkedList<Weibo> weibos = null;
				while (true) {
					System.out.println("Begin to fetch...");
					weibos = conn.getWeiboSimplified(id, batch);
					System.out.println("Fetch line: "+weibos.size());
					for (Weibo weibo : weibos) {
						if (weibo.id > id)
							id = weibo.id;
						
						// split
						String[] words = weibo.content_segs.split("\\t");
						LinkedList<Textpart> parts = getTextpart(words);
						LinkedList<double[]> sequence = new LinkedList<double[]>();
						for (Textpart p : parts) {
							double nb_score = nb.getScore(p.seg);
							double miavg_score = mi.getAvgScore(p.seg);
							double misum_score = Math.log(mi.getSumScore(p.seg)+1);
							outp_textpart_lines.add(String.format("%.6f",
									nb_score)
									+ "\t"
									+ String.format("%.6f", miavg_score)
									+ "\t"
									+ String.format("%.6f", misum_score)
									+ "\t"
									+ p.text);
							outp_hmm.print("["
									+ String.format("%.10f", nb_score) + " "
									+ String.format("%.10f", miavg_score) + " "
									+ String.format("%.10f", misum_score)
									+ "]; ");
							if (nb_score < scale[0][0])
								scale[0][0] = nb_score;
							if (nb_score > scale[0][1])
								scale[0][1] = nb_score;
							if (miavg_score < scale[1][0])
								scale[1][0] = miavg_score;
							if (miavg_score > scale[1][1])
								scale[1][1] = miavg_score;
							if (misum_score < scale[2][0])
								scale[2][0] = misum_score;
							if (misum_score > scale[2][1])
								scale[2][1] = misum_score;
							double[] vector = { nb_score, miavg_score,
									misum_score };
							sequence.add(vector);
						}
						if (sequence.size() > 1) {
							jahmm.addSequence(sequence);
							outp_textpart_lines.add("#" + weibo.weiboId + " "
									+ sequence.size());
							weiboidlist.add(weibo.weiboId);
							querylist.add(weibo.query);
						} else {
							if (sequence.size() == 1)
								outp_textpart_lines.removeLast();
						}
						outp_hmm.println();

					}
					System.out.println("Seg OK! id: " + id);
					if (weibos.size() < batch)
						break;
					
					//break;
				}
			
			
			System.out.print("scale param: ");
			for(int i=0;i<3;i++){
				System.out.print("[ "+scale[i][0]+","+scale[i][1]+" ]");
			}
			System.out.println();
			jahmm.scale(scale);
			jahmm.learn(40);
			LinkedList<LinkedList<Integer>> lists = jahmm.getStateList();

			Iterator<String> iter = outp_textpart_lines.iterator();
			PrintStream outp_textpart = new PrintStream("weibo_textpart.txt");
			int count = 0;
			for (LinkedList<Integer> list : lists) {
				String label = "";
				for (Integer state : list) {
					outp_textpart.println(state + " - " + iter.next());
					if (state==0) label+="0";
					else label+="1";
				}
				outp_textpart.println(iter.next()+" "+label);
				conn.addLabelResult(querylist.get(count),weiboidlist.get(count),label,"yep_hmm");
				count++;
			}
			outp_textpart.close();
			outp_hmm.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public LinkedList<Textpart> getTextpart(String[] words) {
		LinkedList<Textpart> parts = new LinkedList<Textpart>();
		
		NewWeibo nw = new NewWeibo();
		nw.segs = words;
		nw.update();
		int length = nw.parts.size();
		
		for(int i=0;i<length;i++){
			Textpart part = null;
			part = new Textpart();
			part.text = nw.parts.get(i);
			part.seg = (LinkedList<String>)nw.partsegs.get(i);
			//System.out.println(part.text+" "+part.seg+" "+nw.parts.size());
			parts.add(part);
		}
		
		/*
		for (String word : words) {
			if (isPunctuation(word)) {
				if (part.text != "") {
					part.text += word;
					part.seg.add(word);
					parts.add(part);
				}
				part = new Textpart();
			} else {
				part.text += word;
				part.seg.add(word);
			}
		}
		if (part.text != "") {
			parts.add(part);
		}
		*/
		return parts;
	}

	private boolean isPunctuation(String word) {
		Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5\\w、\\(\\)（）]*?$");
		Matcher matcher = pattern.matcher(word);
		if (matcher.find())
			return false;
		return true;
	}

	public void dumpDianping() {
		try {
			PrintStream outp = new PrintStream("dumpdocs.txt");
			int doc_id = 1;

			try {
				// get dianping
				dbConnector conn = new dbConnector("dianping");
				int id = 0;
				int batch = 10000;
				LinkedList<Dianping> revs = null;
				while (true) {
					System.out.println("Begin to fetch...");
					revs = conn.getReview(id, batch / 10);
					id += batch;
					System.out.println("Fetch OK, begin to segement..."
							+ revs.size());

					for (Dianping rev : revs) {
						outp.print((doc_id++) + ", 2, " + rev.id + ", ");
						// split
						String[] words = rev.rev_segs.split(" ");
						HashSet<String> hashword = new HashSet<String>();
						for (String word : words) {
							if (rev.shop_name.contains(word))
								continue;
							if (!isMeaningfulWord(word))
								continue;
							if (!dict.containsKey(word))
								dict.put(word, new Word(wid++, word));
							if (!hashword.contains(word)) {
								dict.get(word).times += 1;
								hashword.add(word);
							}

							outp.print(dict.get(word).wid + " ");
						}
						outp.println();

					}
					if (revs.size() < batch / 10)
						break;
					System.out.println("Seg OK! id: " + id);
					// break;
				}
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			outp.close();

			LinkedList<Word> list = new LinkedList<Word>(dict.values());

			Collections.sort(list, new Comparator<Word>() {
				@Override
				public int compare(Word w1, Word w2) {
					return -(w1.times - w2.times);
				}

			});

			outp = new PrintStream("dict.txt");
			outp.println(doc_id - 1);
			for (Word word : list) {
				outp.println(word.wid + "," + word.word + "," + word.times);
			}
			outp.close();
			System.out.println("doc_num: " + doc_id);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private boolean isMeaningfulWord(String word) {
		Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5A-Za-z]+$");
		Matcher matcher = pattern.matcher(word);
		if (matcher.find()) {
			Pattern pattern2 = Pattern.compile("^[A-Za-z]{1,2}$");
			Matcher matcher2 = pattern2.matcher(word);
			if (matcher2.find())
				return false;
			return true;
		}
		return false;
	}
}
