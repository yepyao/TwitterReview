package database;

public class Weibo {
	public int id;
	public String weiboId;
	public String content;
	public String content_segs;

	public Weibo(int id, String weiboId, String content, String content_segs) {
		this.id = id;
		this.weiboId = weiboId;
		this.content = content;
		this.content_segs = content_segs;
	}
}
