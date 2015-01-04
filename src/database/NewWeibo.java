package database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewWeibo {
	public String weiboId="";
	public String rawhtml="";
	public String userlink="";
	public String username="";
	public String content="";
	public int like=0;
	public int retweet=0;
	public int comment=0;
	public String time="2014-01-01 00:00:00";
	public String from="";
	public String query="";
	public String[] segs=new String[0];
	
	public NewWeibo(String weibo){
		rawhtml=weibo;
		Matcher m_id=Pattern.compile("<div class=\"c\" id=\"(.*?)\">").matcher(weibo);
		if (m_id.find())
			weiboId=m_id.group(1);
		else System.out.println(weibo+"\nParsing Error (WeiboId)");
		
		Matcher m_user=Pattern.compile("<a class=\"nk\" href=\"(.*?)\">(.*?)</a>").matcher(weibo);
		if (m_user.find()){
			userlink=m_user.group(1);
			username=m_user.group(2);
		}
		else System.out.println(weibo+"\nParsing Error (User)");
		
		Matcher m_content=Pattern.compile("<span class=\"ctt\">:(.*)</span>(.*?)</span>").matcher(weibo);
		if (m_content.find())
			content=m_content.group(1);
		else System.out.println(weibo+"\nParsing Error (Content)");
		
		Matcher m_like=Pattern.compile("赞\\[(\\d+)\\]").matcher(weibo);
		Matcher m_retweet=Pattern.compile("转发\\[(\\d+)\\]").matcher(weibo);
		Matcher m_comment=Pattern.compile("评论\\[(\\d+)\\]").matcher(weibo);
		if (m_like.find()) like=Integer.valueOf(m_like.group(1));
		if (m_retweet.find()) retweet=Integer.valueOf(m_retweet.group(1));
		if (m_comment.find()) comment=Integer.valueOf(m_comment.group(1));
		
		Matcher m_time=Pattern.compile("(\\d\\d)月(\\d\\d)日 (\\d\\d):(\\d\\d)").matcher(weibo);
		if (m_time.find())
			time="2014-"+m_time.group(1)+"-"+m_time.group(2)+" "+m_time.group(3)+":"+m_time.group(4)+":00";
		else{
			m_time=Pattern.compile("今天 (\\d\\d):(\\d\\d)").matcher(weibo);
			if (m_time.find())
				time=new SimpleDateFormat("yyyy-MM-dd ").format(new Date())+m_time.group(1)+":"+m_time.group(2)+":00";
			else {
				time=new SimpleDateFormat("yyyy-MM-dd hh:00:00").format(new Date());
			}
		}
		
		Matcher m_from=Pattern.compile("<span class=\"ct\">.*来自(<a href=\".*?\">)?(.*?)(</a>)?</span></div></div>").matcher(weibo);
		if (m_from.find())
			from=m_from.group(2);
		else System.out.println(weibo+"\nParsing Error (From)");
	}
	
	public NewWeibo(){
	}

	public String simplify;
	public List<String> parts;
	public List<List<String>> partsegs;
	
	public void update(){//require segs
		for (int i=0;i<segs.length;i++){
			if (segs[i]=="“"){
				int k=-1,l=-1;
				for (int j=i+1;j<segs.length;j++){
					if (segs[j]=="”"){
						k=j;
						break;
					}
					if (isPunctuation(segs[j])) l=j;
				}
				if (k==-1){
					segs[i]="";
					continue;
				}
				if (l!=-1){
					segs[k]=segs[i]="";
				}
			}
		}
		
		simplify="";
		parts=new LinkedList<String>();
		partsegs=new LinkedList<List<String>>();
		List<String> cursegs=new LinkedList<String>();
		String curS="";
		boolean end=false;
		for (String s:segs)
		if (s.length()>0){
			simplify+=s;
			if (isPunctuation(s))
				end=true;
			else if (end){
				if (curS.length()>0){
					parts.add(curS);
					partsegs.add(cursegs);
				}
				curS="";
				cursegs=new LinkedList<String>();
				end=false;
			}
			curS+=s;
			cursegs.add(s);
		}
		if (curS.length()>0){
			parts.add(curS);
			partsegs.add(cursegs);
		}
	}
	private boolean isPunctuation(String word) {
		if (word.equals(".")||word.equals("。")) return true;
		if (word.equals(",")||word.equals("，")) return true;
		if (word.equals("?")||word.equals("？")) return true;
		if (word.equals("!")||word.equals("！")) return true;
		if (word.equals("～")||word.equals("")) return true;
		if (word.equals(":")||word.equals("：")) return true;
		if (word.equals(";")||word.equals("；")) return true;
		if (word.equals("…")||word.equals("、")) return true;
		return false;
//		Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5\\w、\\(\\)（）]*?$");
//		Matcher matcher = pattern.matcher(word);
//		if (matcher.find()) return false;
//		System.out.println(word);
//		return false;
//		return true;
	}
	
	public String simplify(){
//		String res=content;
//		res=res.replaceAll("<.*?>", "");
//		return res;
		String res="";
		for (String s:segs)
			res=res+s;
		return res;
	}
	public String getSegments(){
		StringBuilder res=new StringBuilder();
		for (String s:segs)
			res.append(s+"\t");
		return res.toString();
	}
	public String getParts(){
		String res="";
		for (String s:parts)
			res+=(res.length()>0?"\n":"")+s;
		return res;
	}
	public String getPartSegs(){
		String res="";
		for (List<String> s:partsegs){
			String cur="";
			for (String ts:s)
				cur+=(cur.length()>0?"\t":"")+ts;
			res+=(res.length()>0?"\n":"")+cur;
		}
		return res;
	}
}
