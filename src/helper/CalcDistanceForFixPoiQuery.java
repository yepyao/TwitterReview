package helper;

import java.util.LinkedList;

import database.GPS;
import database.Query;
import database.Position;
import database.Weibo;
import database.dbConnector;

public class CalcDistanceForFixPoiQuery {
	dbConnector conn = null;

	public CalcDistanceForFixPoiQuery() {
		try {
			conn = new dbConnector("weibo");
			LinkedList<Query> querys = conn.getQuerys();
			for (Query query : querys) {
				updateDistance(query);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateDistance(Query query) {
		try {
			LinkedList<GPS> pois = conn.getGPS(query.query);
			LinkedList<Weibo> weibos = conn.getWeibo(query.query);
			System.out.println(query.query + " " + weibos.size());
			for (Weibo weibo : weibos) {
				String poi = weibo.poi;
				String[] arr = poi.split(",");
				double lnt = Double.parseDouble(arr[1]);
				double lat = Double.parseDouble(arr[2]);
				if (lnt < lat) {
					double temp = lnt;
					lnt = lat;
					lat = temp;
				}
				double distance = Double.MAX_VALUE;
				for (GPS pos : pois) {
					double dis = getShortestDistanceBetweenTowCandidates(lat,
							lnt, pos.lat, pos.lnt);
					if (dis < distance)
						distance = dis;
				}
				if (distance > 100000)
					distance = 0;
				// System.out.println(lat + "N, " + lnt + "E "
				// +query.lat + "N, " + query.lnt + "E "
				// + distance + "m");
				conn.setWeiboDistance(weibo.id, distance);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private final double EARTH_RADIUS = 6378.137; // 地球半径

	private static double rad(double d) {
		return d * Math.PI / 180.0; // 计算弧长
	}

	public double getShortestDistanceBetweenTowCandidates(double Lat1,
			double Lon1, double Lat2, double Lon2) {
		double radLat1 = rad(Lat1);
		double radLat2 = rad(Lat2);
		double a = radLat1 - radLat2;
		double b = rad(Lon1) - rad(Lon2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = s * 1000; // 换算成米
		return s;

	}
}
