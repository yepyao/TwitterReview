package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.Dianping;
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

	public void dumpWeibo() {
		try {
			PrintStream outp = new PrintStream("dumpdocs.txt");

			int doc_id = 1;
			/*
			 * try { dbConnector conn = new dbConnector("weibo");
			 * 
			 * int id = 0; int batch = 10000; LinkedList<Weibo> weibos = null;
			 * while (true) { System.out.println("Begin to fetch..."); weibos =
			 * conn.getWeibo(id, batch); id += weibos.size();
			 * 
			 * for (Weibo weibo : weibos) {
			 * 
			 * outp.print((doc_id++)+", 1, "+weibo.id+", "); // split String[]
			 * words = weibo.content_segs.split(" "); for (String word : words)
			 * { if (!isMeaningfulWord(word)) continue; if
			 * (!dict.containsKey(word)) dict.put(word, cid++);
			 * outp.print(dict.get(word) + " "); } outp.println(); } if
			 * (weibos.size() < batch) break; System.out.println("Seg OK! id: "
			 * + id); // break; } doc_size = id; conn.close(); } catch
			 * (Exception ex) { ex.printStackTrace(); }
			 */
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
