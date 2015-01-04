package database;

public class Weibo {
	public int id;
	public String weiboId;
	public String content;
	public String content_segs;
	public String query;
	public String rawhtml;
	public String poi;

	public Weibo(int id, String weiboId, String content, String content_segs,String query,String rawhtml,String poi) {
		this.id = id;
		this.weiboId = weiboId;
		this.content = content;
		this.content_segs = content_segs;
		this.query = query;
		this.rawhtml = rawhtml;
		this.poi = poi;
	}
}
