package helper;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.Weibo;
import database.dbConnector;

public class HtmlParser {
	public HtmlParser() {

	}

	public void parse() {
		try {
			dbConnector conn = new dbConnector("weibo");
			int id = 0;
			int t;
			int batch = 10000;
			LinkedList<Weibo> weibos = null;
			while (true) {
				System.out.println("Begin to fetch...");
				weibos = conn.getWeibo(id, batch);
				// id += batch;
				System.out.println("Fetch OK, begin to segement..."
						+ weibos.size());
				t = 0;

				for (Weibo weibo : weibos) {
					if (weibo.id > id)
						id = weibo.id;

					Pattern poi = Pattern
							.compile("<a href=\"http://place.weibo.com/imgmap/poiid=([^\"]*)&amp;center=([^\"]*)&amp;backurl=[^\"]*\">显示地图</a>");
					Matcher matcher = poi.matcher(weibo.rawhtml);
					if (matcher.find()) {
						// System.out.println(matcher.group(1)+","+matcher.group(2));
						conn.setWeiboPoi(weibo.id, matcher.group(1) + ","
								+ matcher.group(2));
					}

					int pic_count = 0;
					Pattern pic = Pattern.compile("原图");
					matcher = pic.matcher(weibo.rawhtml);
					if (matcher.find()) {
						pic_count = 1;
					}
					Pattern picset = Pattern.compile("组图共(\\d*)张");
					matcher = picset.matcher(weibo.rawhtml);
					if (matcher.find()) {
						pic_count = Integer.parseInt(matcher.group(1));
					}
					if (pic_count > 0)
						conn.setWeiboPic(weibo.id, pic_count);

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
