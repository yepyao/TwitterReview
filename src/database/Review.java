package database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Review {
	public int shop_id;
	public String shop_name;
	public int rev_id;
	public String raw_html;
	public String rev_text;
	public Review(int shop_id, String shop_name, int rev_id, String raw_html){
		this.shop_id = shop_id;
		this.shop_name = shop_name;
		this.rev_id = rev_id;
		this.raw_html = raw_html;
		parseReview();
	}
	
	private void parseReview(){
		String pattern = "<div class=\"J_brief-cont\">(.*?)</div>";
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		Matcher m = r.matcher(raw_html);
		if (m.find()) {
			rev_text = m.group(1).trim();
		}
	}
}
