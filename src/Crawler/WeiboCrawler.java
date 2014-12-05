package Crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base32;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;

public class WeiboCrawler {
	private String username;
	private String password;
	HttpClient client;
	HttpClientContext context;
	public WeiboCrawler(String username,String password){
		this.username=username;
		this.password=password;
		init();
		System.out.println(getContent("http://weibo.cn/"));
	}
	private void init(){
		try{
			client=new DefaultHttpClient();
			context=HttpClientContext.create();
			//client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			CookieStore cookie=new BasicCookieStore();
			context.setCookieStore(cookie);
			
			String content=getContent("http://login.weibo.cn/login/");
			//System.out.println(content);
			String rand=getRand(content);
			System.out.println("rand "+rand);
			String vk=getVk(content);
			System.out.println("vk "+vk);
			String pid=getPasswordId(content);
			System.out.println("pid "+pid);
			System.out.println();
			
			HttpPost login=new HttpPost("http://login.weibo.cn/login/?rand="+rand);
			login.setHeader("User-Agent",
	                "Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			login.setHeader("Referer", "http://login.weibo.cn/login/");
			login.setHeader("Origin", "http://login.weibo.cn/login/");
			login.setHeader("Content-Type", "application/x-www-form-urlencoded");
	 
			LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("mobile", username));
			params.add(new BasicNameValuePair(pid, password));
			params.add(new BasicNameValuePair("remember", "on"));
			params.add(new BasicNameValuePair("backURL", "http://weibo.cn"));
			params.add(new BasicNameValuePair("backTitle", ""));
			params.add(new BasicNameValuePair("tryCount", ""));
			params.add(new BasicNameValuePair("vk", vk));
			params.add(new BasicNameValuePair("submit", "登录"));
			login.setEntity(new UrlEncodedFormEntity(params));
			
			List<Cookie> cookies = context.getCookieStore().getCookies();
			for(Cookie c:cookies){
				//System.out.println(c.toString());
			}
			HttpResponse res=client.execute(login,context);
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			
			Header headers[] = clientContext.getRequest().getAllHeaders();
			for(Header h:headers){
				System.out.println(h.getName() + ": " + h.getValue());
			}
			
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(res.getEntity().getContent()));
		 
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			PrintStream outp = new PrintStream("output.txt");
			outp.println(result);
			outp.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private String getRand(String content){
		String p="(.*)?rand=(\\d+)(.*)";
		Pattern r=Pattern.compile(p);
		Matcher matcher=r.matcher(content);
		if (matcher.find())
			return matcher.group(2);
		return "NOMATCH";
	}
	private String getVk(String content){
		String p="(.*)name=\"vk\" value=\"(.*?)\"(.*)";
		Pattern r=Pattern.compile(p);
		Matcher matcher=r.matcher(content);
		if (matcher.find())
			return matcher.group(2);
		return "NOMATCH";
	}
	private String getPasswordId(String content){
		String p="(.*)\"(password_.*?)\"(.*)";
		Pattern r=Pattern.compile(p);
		Matcher matcher=r.matcher(content);
		if (matcher.find())
			return matcher.group(2);
		return "NOMATCH";
	}
	private String getContent(String url){
		try{
			HttpGet get=new HttpGet(url);
			
			HttpResponse res=client.execute(get,context);
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(res.getEntity().getContent()));
		 
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
			return result.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	
}
