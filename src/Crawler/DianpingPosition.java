package Crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.LinkedList;

import database.Position;
import database.Shop;
import database.dbConnector;

public class DianpingPosition {
	LinkedList<String> shop_names = new LinkedList<String>();
	DianpingCrawler crawler = new DianpingCrawler();

	public DianpingPosition() {
		try {
			dbConnector conn = new dbConnector("weibo");
			BufferedReader reader = new BufferedReader(new FileReader(
					"dianping_shopids_clean.txt"));
			String temp = "";
			HashSet<String> querys = new HashSet<String>();
			while ((temp = reader.readLine()) != null) {
				if (querys.contains(temp))
					continue;
				querys.add(temp);
				if (!querys.contains("烧肉达人")) continue;
				LinkedList<Position> positions = getPosition(temp);
				for (Position pos : positions) {
					System.out.println(pos);
					conn.addPos(temp, pos);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public LinkedList<Position> getPosition(String query) {
		LinkedList<Position> list = new LinkedList<Position>();
		LinkedList<Shop> ids = crawler.getShopidsByQuery(query
				.replace(" ", "_"));
		for (int i = 0; i < ids.size(); i++) {
			if (ids.get(i).name.contains(query)) {
				Position pos = crawler.getPosition(ids.get(i));
				if (pos != null)
					list.add(pos);
			}
		}
		return list;
	}
}
