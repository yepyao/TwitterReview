package Crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.Review;
import database.Shop;
import database.dbConnector;

public class DianpingParser {

	LinkedList<Shop> shoplist = new LinkedList<Shop>();

	public DianpingParser() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"dianping_shopids.txt"));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				if (temp.trim().length() == 0)
					continue;
				String[] arr = temp.trim().split(" ");
				shoplist.add(new Shop(Integer.parseInt(arr[0]), arr[1]));
				// break;
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void parseHtml() {
		try {
			dbConnector conn = new dbConnector("dianping");

			File dir = new File("html_ok/");
			File[] htmls = dir.listFiles();
			for (Shop shop : shoplist) {
				int id = shop.id;
				System.out.println("Shop: " + id + " " + shop.name);
				for (File html : htmls) {
					if (html.getName().startsWith(id + "_review_")) {
						// System.out.println("Shop: " + id + " " + shop.name
						// + " File: " + html.getName());
						LinkedList<Review> reviews = getReviews(html, shop);
						// insert into database
						for (Review review : reviews) {
							conn.insertReview(review);
						}
					}

				}
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private LinkedList<Review> getReviews(File html, Shop shop) {
		LinkedList<Review> reviews = new LinkedList<Review>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(html));
			String content = reader.readLine();

			String pattern = "<li id=\"rev_(\\d+)\" data-id=\"(\\d+)\">(.*?)</li>";
			// 创建 Pattern 对象
			Pattern r = Pattern.compile(pattern);
			// 现在创建 matcher 对象
			Matcher m = r.matcher(content);
			while (m.find()) {
				// System.out.println("Found value: " + m.group(0));
				Review new_rev = new Review(shop.id, shop.name, Integer.parseInt(m
						.group(1)), m.group(0));
				//System.out.println(new_rev.rev_text);
				reviews.add(new_rev);
			}

			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return reviews;
	}
}
