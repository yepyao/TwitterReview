package database;

import java.util.LinkedList;

public class Textpart {
	public String text;
	public LinkedList<String> seg;
	public Textpart(){
		seg = new LinkedList<String>();
		text = "";
	}
}
