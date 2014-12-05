package database;

import java.awt.print.Printable;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class dbConnector {
	Connection conn;
	String database;

	public dbConnector(String database) {
		this.database = database;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://172.16.14.6:3306/"
					+ database + "?useUnicode=true&characterEncoding=utf8",
					database, "dog12321");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void close() {
		try {
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void insertReview(Review review) {
		if (database != "dianping") {
			System.err.println("wrong database");
			System.exit(1);
			return;
		}
		String cmd = "";
		try {
			cmd = "INSERT INTO `dianping`.`review` ("
					+ "`rev_id`, `shop_id`, `shop_name`, `raw_html`, `rev_text`)"
					+ "VALUES (" + "'" + review.rev_id + "'," + "'"
					+ review.shop_id + "'," + "'"
					+ prepString(review.shop_name) + "'," + "'"
					+ prepString(review.raw_html) + "'," + "'"
					+ prepString(review.rev_text) + "'" + ")";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			if (!ex.getMessage().contains("Duplicate")) {
				System.out.println(cmd);
				ex.printStackTrace();
			}
		}
	}

	public void setWeiboSeg(int id, String transSegs) {
		String cmd = "";
		try {
			cmd = "UPDATE `weibo` " + "SET content_segs = '"
					+ prepString(transSegs) + "' " + "WHERE id='" + id + "'";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

	public LinkedList<Weibo> getWeibo(int id, int limit) {
		if (database != "weibo") {
			System.err.println("wrong database");
			System.exit(1);
			return null;
		}
		LinkedList<Weibo> weibos = new LinkedList<Weibo>();
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT id,weiboId,content,content_segs FROM weibo WHERE id> '"
							+ id + "' ORDER BY id LIMIT " + limit);

			// 循环读取数据
			while (rs.next()) {
				// 打印数据
				weibos.add(new Weibo(Integer.parseInt(rs.getString("id")), rs
						.getString("weiboId"), recoverString(rs
						.getString("content")), rs.getString("content_segs")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return weibos;
	}

	public LinkedList<Weibo> getWeiboTimeline(int id, int limit) {
		if (database != "weibo") {
			System.err.println("wrong database");
			System.exit(1);
			return null;
		}
		LinkedList<Weibo> weibos = new LinkedList<Weibo>();
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT id,weiboId,content,content_segs FROM weibo_timeline WHERE id> '"
							+ id + "' ORDER BY id LIMIT " + limit);

			// 循环读取数据
			while (rs.next()) {
				// 打印数据
				weibos.add(new Weibo(Integer.parseInt(rs.getString("id")), rs
						.getString("weiboId"), recoverString(rs
						.getString("content")), rs.getString("content_segs")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return weibos;
	}

	public void setReviewSeg(int id, String transSegs) {
		String cmd = "";
		try {
			cmd = "UPDATE `review` " + "SET rev_segs = '"
					+ prepString(transSegs) + "' " + "WHERE id='" + id + "'";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

	public LinkedList<Dianping> getReview(int id, int limit) {
		if (database != "dianping") {
			System.err.println("wrong database");
			System.exit(1);
			return null;
		}
		LinkedList<Dianping> revs = new LinkedList<Dianping>();
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT id,rev_text,rev_segs,shop_name FROM review WHERE id> '"
							+ id + "' ORDER BY id LIMIT " + limit);

			// 循环读取数据
			while (rs.next()) {
				// 打印数据
				revs.add(new Dianping(Integer.parseInt(rs.getString("id")),
						recoverString(rs.getString("rev_text")), rs
								.getString("rev_segs"), rs
								.getString("shop_name")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return revs;
	}

	private static String recoverString(String s) {
		return s.toString().replace("''", "'");
	}

	public static String prepString(String s) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			if (!isEmojiCharacter(s.charAt(i)))
				res.append(s.charAt(i));
		return res.toString().replace("'", "''");
	}

	private static boolean isEmojiCharacter(char codePoint) {
		return !((codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
				|| (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
				|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
	}

	public void insertScore(String weiboId, double score, String method) {
		String cmd = "";
		try {
			cmd = "INSERT INTO `score` (`weiboId`, `score`, `method`) VALUES ('"
					+ weiboId + "'," + score + ",'" + method + "')";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

	public void insertTimeline(String weiboId, String content,
			String content_segs, String rawhtml) {
		if (content == "")
			return;
		String cmd = "";
		try {
			cmd = "INSERT INTO `weibo_timeline` (`weiboId`, `content`, `content_segs`,`rawhtml`) VALUES ('"
					+ weiboId
					+ "','"
					+ prepString(content)
					+ "','"
					+ content_segs + "','" + prepString(rawhtml) + "')";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

	public void deleteTimeline(int id) {
		String cmd = "";
		try {
			cmd = "DELETE FROM `weibo_timeline` WHERE `id`=" + id;
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);

		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

	public void clearScore(String method) {
		String cmd = "";
		try {
			cmd = "DELETE FROM `score` WHERE method='" + method + "'";
			Statement stat = conn.createStatement();
			stat.executeUpdate(cmd);
			System.out.println("delete method score '" + method + "' OK!");
		} catch (Exception ex) {
			System.out.println(cmd);
			ex.printStackTrace();
		}

	}

}
