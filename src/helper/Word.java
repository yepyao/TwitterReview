package helper;

public class Word {
	public String word;
	public int times;
	public int times_timeline = 0;
	public int wid;
	public double MI;
	public double pos_NB;
	public double neg_NB;

	public Word(int wid, String word) {
		this.word = word;
		this.wid = wid;
		this.times = 0;
		this.times_timeline = 0;
	}
}
