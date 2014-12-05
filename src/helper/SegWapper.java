package helper;

import java.util.LinkedList;
import java.util.List;

import database.Dianping;
import database.Weibo;
import database.dbConnector;

public class SegWapper {

	SegDemo segmenter;

	public SegWapper() {
		segmenter = new SegDemo();
		segmenter.load();
	}
	
	public void test(){
		String test = "点评了商户<span class=\"kt\">金钱豹国际美食百汇</span>(中关村店)，总体评价五星：消费比较高， 海鲜自助， 尤其是龙虾腿吃个比较爽！螃蟹也很大，生蚝也不错， 口味比较重！！！！ <a href=\"http://weibo.cn/sinaurl?f=w&amp;u=http%3A%2F%2Ft.cn%2F8s192V2&amp;ep=B2AJjjSgM%2C1823261101%2CB2AJjjSgM%2C1823261101&amp;vt=4\">http://t.cn/8s192V2</a>";
		System.out.println(test = escapeHTML(test));
		List<String> testsegs = segmenter.segment(test);
		System.out.println(testsegs);
	}

	public void segWeibo() {
		try {
			dbConnector conn = new dbConnector("weibo");
			int id = 0;
			int t;
			int batch = 1000;
			LinkedList<Weibo> weibos = null;
			while (true) {
				System.out.println("Begin to fetch...");
				weibos = conn.getWeibo(id, batch);
				id += batch;
				System.out.println("Fetch OK, begin to segement..."+weibos.size());
				t = 0;
				
				for (Weibo weibo : weibos) {
					if (t==0){
						System.out.println(weibo.weiboId);
					}
					t++;
					if (t*10%batch ==0) System.out.print("..."+t*10/batch+"0%");
					String content = weibo.content;
					// System.out.println(content);
					content = escapeHTML(content);
					// System.out.println(content);
					List<String> segs = segmenter.segment(content);
					//System.out.println(weibo.weiboId+" Seg OK!");
					conn.setWeiboSeg(weibo.id, transSegs(segs));
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
	
	public void segDianping() {
		try {
			dbConnector conn = new dbConnector("dianping");
			int id = 0;
			int t;
			int batch = 1000;
			LinkedList<Dianping> revs = null;
			while (true) {
				System.out.println("Begin to fetch...");
				revs = conn.getReview(id, batch);
				id += batch;
				System.out.println("Fetch OK, begin to segement..."+revs.size());
				t = 0;
				
				for (Dianping rev : revs) {
					t++;
					if (t*10%batch ==0) System.out.print("..."+t*10/batch+"0%");
					String content = rev.review;
					// System.out.println(content);
					content = escapeHTML(content);
					// System.out.println(content);
					List<String> segs = segmenter.segment(content);
					//System.out.println(weibo.weiboId+" Seg OK!");
					conn.setReviewSeg(rev.id, transSegs(segs));
				}
				if (revs.size() < batch)
					break;
				System.out.println("Seg OK! id: " + id);
				// break;
			}

			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String transSegs(List<String> segs) {
		String rs = "";
		for (String seg : segs) {
			rs += seg + " ";
		}
		return rs;
	}

	public static String escapeHTML(String weibo) {
		weibo = weibo.replaceAll("<.*?>", "");
		weibo = weibo.replaceAll("[【】]", " ");
		weibo = weibo.replaceAll("#", " ");
		weibo = weibo.replaceAll("\\[.*?\\]", "");
		weibo = weibo.replaceAll("\\(签到\\d+次\\)", "");
		weibo = weibo.replaceAll("@\\S*", "");
		weibo = weibo.replaceAll("http://[/\\.\\w\\d]*", "");
		return weibo;
	}
}
