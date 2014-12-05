package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;

public class ParseTMT {
	HashMap<Integer, String> dict;

	public ParseTMT() {
		try {
			dict = new HashMap<Integer, String>();
			BufferedReader reader = new BufferedReader(new FileReader(
					"dict.txt"));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				String[] arr = temp.split(",");
				dict.put(Integer.parseInt(arr[0]), arr[1]);
			}
			reader.close();

			reader = new BufferedReader(new FileReader("tmt/summary.txt"));
			PrintStream outp = new PrintStream("tmt/summary_parsed.txt");
			while ((temp = reader.readLine()) != null) {
				if (temp.startsWith("\t")) {
					String[] arr = temp.trim().split("\t");
					temp = "\t" + dict.get(Integer.parseInt(arr[0])) + "\t"
							+ arr[1];
				}
				outp.println(temp);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
