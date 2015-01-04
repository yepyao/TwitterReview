import java.util.HashMap;
import java.util.Stack;

import models.MISum;
import models.Weight;
import helper.CalcDistanceForFixPoiQuery;
import helper.DumpDoc;
import helper.HtmlParser;
import helper.ParseTMT;
import helper.SegDemo;
import helper.SegWapper;
import Crawler.DianpingCrawler;
import Crawler.DianpingParser;
import Crawler.DianpingPosition;
import Crawler.TimelineCrawler;
import Crawler.WeiboCrawler;

public class Main {
	public static void main(String args[]) {
		// WeiboCrawler weiboCrawler=new
		// WeiboCrawler("pet12321@163.com","program12321");
		// DianpingCrawler dianping = new DianpingCrawler();
		// DianpingParser parse = new DianpingParser();
		// parse.parseHtml();
		// SegWapper segwapper = new SegWapper();
		// segwapper.segWeibo();
		// segwapper.segDianping();
		//DumpDoc dumper = new DumpDoc();
		// dumper.dumpDianping();
		// dumper.calcMI();
		//dumper.dumpWeiboTextpart();

		// dumper.removeTimelineDup();
		// ParseTMT tmt = new ParseTMT();
		// Weight weight = new Weight();
		// weight.calcScore();
		// MISum mi = new MISum();
		// mi.calcScore();
		// TimelineCrawler crawl = new TimelineCrawler();
		// crawl.crawl();
		//HtmlParser hp = new HtmlParser();
		//hp.parse();
		//DianpingPosition pos = new DianpingPosition();
		new CalcDistanceForFixPoiQuery(); 
	}
}
