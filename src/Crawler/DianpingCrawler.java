package Crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import database.Position;
import database.Shop;

public class DianpingCrawler {
	HttpClient client;
	HttpClientContext context;
	LinkedList<Shop> shopids;
	int banedtime = 0;

	public DianpingCrawler() {
		HttpHost proxy = new HttpHost("172.16.2.20", 3128);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(
				proxy);
		RequestConfig globalConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BEST_MATCH).build();
		client = HttpClients.custom()
		// .setProxy(proxy)
				.setDefaultRequestConfig(globalConfig).build();
		// client = new DefaultHttpClient();
		// client = HttpClients.createSystem();

		context = HttpClientContext.create();
		CookieStore cookie = new BasicCookieStore();
		context.setCookieStore(cookie);

	}

	public void crawl() {
		shopids = new LinkedList<Shop>();
		getIds();
		getHtmls();
	}

	private void getHtmls() {
		for (Shop shop : shopids) {
			System.out.println("Shop begin: " + shop.id + " " + shop.name);
			try {
				PrintStream outp;
				outp = new PrintStream("html/" + shop.id + "_main.html");
				outp.println(getContent("http://www.dianping.com/shop/"
						+ shop.id));
				outp.close();

				int maxpage = getMaxReviewPage(shop.id);
				System.out.println(shop.name + " max page " + maxpage);
				for (int i = 1; i <= maxpage; i++) {
					// skip already download file
					File f = new File("html/" + shop.id + "_review_" + i
							+ ".html");
					if (f.exists() && f.length() > 20 * 1024) {
						System.out.println("Skip file: " + shop.id + "_review_"
								+ i + ".html " + shop.name);
						continue;
					}

					outp = new PrintStream("html/" + shop.id + "_review_" + i
							+ ".html");
					outp.println(getContent("http://www.dianping.com/shop/"
							+ shop.id + "/review_all?pageno=" + i));
					outp.close();
					if (i % 30 == 0)
						System.out.println("Saved review page: " + i
								+ ", max page: " + maxpage + " " + shop.id
								+ "_review_" + i + ".html " + shop.name);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("Shop end: " + shop.id + " " + shop.name);
		}

	}

	private int getMaxReviewPage(int id) {
		String review = getContent("http://www.dianping.com/shop/" + id
				+ "/review_all?pageno=1");
		// System.out.println(review);
		String pattern = "<a href=\"\\?pageno=(\\d+)\"";

		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);

		// 现在创建 matcher 对象
		Matcher m = r.matcher(review);
		int max = 0;
		while (m.find()) {
			// System.out.println("new page: " + m.group(1));
			int newnum = Integer.parseInt(m.group(1));
			if (newnum > max)
				max = newnum;
		}
		return max;

	}

	private void getIds() {
		String urlp = "http://www.dianping.com/search/category/1/10/o10";
		for (int i = 1; i <= 50; i++) {
			shopids.addAll(getShopids(urlp + "p" + i));
			System.out.println(shopids.size());
		}
		try {
			PrintStream outp = new PrintStream("dianping_shopids.txt");
			for (Shop shop : shopids) {
				outp.println(shop.id + " " + shop.name);
			}
			outp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private LinkedList<Shop> getShopids(String url) {
		String content = getContent(url);

		LinkedList<Shop> list = new LinkedList<Shop>();
		String pattern = "title=\"([^\"]+)\" target=\"_blank\" href=\"/shop/(\\d+)\"  >";

		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);

		// 现在创建 matcher 对象
		Matcher m = r.matcher(content);
		while (m.find()) {
			// System.out.println("Found value: " + m.group(1));
			list.add(new Shop(Integer.parseInt(m.group(2)), m.group(1)));
		}
		// System.out.println("Get shops: "+list.size());
		return list;
	}

	private String getContent(String url) {
		boolean baned = true;
		String content = "";
		while (baned) {
			try {

				// System.out.println("Get URL: "+url);
				HttpGet get = new HttpGet(url);

				get.setHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
				get.setHeader("Referer",
						"http://www.dianping.com/search/category/1/0/o10");
				HttpResponse res = client.execute(get, context);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						res.getEntity().getContent()));

				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				rd.close();

				content = result.toString();
				PrintStream outp = new PrintStream("recent_get.html");

				outp.println("URL: " + url);
				outp.println("Request:");
				HttpClientContext clientContext = HttpClientContext
						.adapt(context);
				Header headers[] = clientContext.getRequest().getAllHeaders();
				for (Header h : headers) {
					outp.println(h.getName() + ": " + h.getValue());
				}
				outp.println();
				outp.println("Response:");
				headers = res.getAllHeaders();
				for (Header h : headers) {
					outp.println(h.getName() + ": " + h.getValue());
				}
				outp.println();
				outp.println("Cookies:");
				List<Cookie> cookies = clientContext.getCookieStore()
						.getCookies();
				for (Cookie c : cookies) {
					outp.println(c.getName() + ": " + c.getValue());
				}

				outp.println();
				outp.println(content);
				outp.println();
				outp.close();

				Thread.sleep(2000);

				baned = getBanedState(content, url);
			} catch (Exception ex) {
				ex.printStackTrace();
				// System.exit(1);
			}
		}
		return content;
	}

	private boolean getBanedState(String content, String referer) {
		String pattern = "/alpaca/captcha\\.jpg";
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		Matcher m = r.matcher(content);
		if (m.find()) {
			// System.out.println("banedtime: " + banedtime);
			// if (banedtime == 0)
			// context.getCookieStore().clear();
			banedtime = 1;
			System.out.println("IP has been baned! Sleep for 10s*" + banedtime);
			try {
				Thread.sleep(10000 * banedtime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Wake Up!");

			// BasicClientCookie newCookie = new BasicClientCookie("_hc.v",
			// "\"\\\"22d88fa0-dcb1-4caa-8f5f-0c25b44cc21b.1416658093\\\"\"");
			// newCookie.setVersion(0);
			// newCookie.setDomain(".dianping.com");
			// newCookie.setPath("/");
			//
			// context.getCookieStore().addCookie(newCookie);
			// download captcha
			try {
				HttpGet get = new HttpGet(
						"http://www.dianping.com/alpaca/captcha.jpg");
				get.setHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
				get.setHeader("Referer", referer);
				get.setHeader("Accept", "image/webp,*/*;q=0.8");

				HttpResponse res = client.execute(get, context);
				//res.getEntity().getContent().close();
				//EntityUtils.consume(res.getEntity());

				// HttpClientContext clientContext
				// =HttpClientContext.adapt(context);
				// Header headers[] = clientContext.getRequest().getAllHeaders();
				// for(Header h:headers){
				// System.out.println(h.getName() + ": " + h.getValue());
				// }
				// System.out.println();

				File storeFile = new File("captcha.jpg");
				FileOutputStream output = new FileOutputStream(storeFile);

				// 得到网络资源的字节数组,并写入文件
				HttpEntity entity = res.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();

					byte b[] = new byte[1024];
					int j = 0;
					while ((j = instream.read(b)) != -1) {
						output.write(b, 0, j);
					}
					output.flush();
					output.close();
				}

				Scanner input = new Scanner(System.in);
				System.out.println("输入验证码:");
				String code = input.nextLine();

				HttpPost login = new HttpPost(
						"http://www.dianping.com/alpaca/captcha.jpg");
				login.setHeader("Referer", referer);
				LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
				params.add(new BasicNameValuePair("vode", code));
				login.setEntity(new UrlEncodedFormEntity(params));
				res = client.execute(login, context);
				System.out.println("code has been send!");
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						res.getEntity().getContent()));

				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}

				content = result.toString();

				System.out.println(content);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			return true;
		}
		banedtime = 0;
		return false;
	}

	public LinkedList<Shop> getShopidsByQuery(String query) {
		LinkedList<Shop> shops = getShopids("http://www.dianping.com/search/keyword/1/0_"
				+ query);
		return shops;
	}

	public Position getPosition(Shop shop) {
		String content = getContent("http://www.dianping.com/shop/" + shop.id);

		String pattern = "\\{lng:([\\d\\.]*?),lat:([\\d\\.]*?)\\}";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(content);
		if (m.find()) {
			return new Position(shop, m.group(1) + "," + m.group(2));
		}
		System.out.println(shop.id + " " + shop.name + " no position!");
		return null;

	}

}
