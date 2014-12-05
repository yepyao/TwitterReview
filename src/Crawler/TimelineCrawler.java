package Crawler;

import helper.SegDemo;
import helper.SegWapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import database.dbConnector;

public class TimelineCrawler {
	String token = "2.00ohBunB11LxADd42921ac2bNC76HC";
	HttpClient client;
	SegDemo segmenter;

	public TimelineCrawler() {
		client = new DefaultHttpClient();
		segmenter = new SegDemo();
		segmenter.load();
	}

	public void crawl() {
		while (true) {
			crawlTimeline();
			try {
				Thread.sleep(5 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void crawlTimeline() {
		try {
			dbConnector conn = new dbConnector("weibo");
			HttpGet get = new HttpGet(
					"https://api.weibo.com/2/statuses/public_timeline.json?access_token="
							+ token + "&count=200");
			HttpResponse res = client.execute(get);

			BufferedReader rd = new BufferedReader(new InputStreamReader(res
					.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();

			String content = result.toString();

			JSONObject jsonObject = new JSONObject(content);

			JSONArray statuses = jsonObject.getJSONArray("statuses");
			System.out.println("Crawl timeline ok! length: "
					+ statuses.length());
			for (int i = 0; i < statuses.length(); i++) {
				JSONObject weibo = statuses.getJSONObject(i);
				String text = weibo.getString("text");
				// System.out.println(text);
				String weiboId = weibo.getString("id");
				text = SegWapper.escapeHTML(dbConnector.prepString(text));
				List<String> segs = segmenter.segment(text);
				conn.insertTimeline(weiboId, text, SegWapper.transSegs(segs),
						weibo.toString());
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
