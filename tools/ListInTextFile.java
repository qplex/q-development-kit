import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class ListInTextFile {
	static final Comparator<String> UPPER_CASE_COMPARATOR = new Comparator<String>() {
		public int compare(String a, String b) {
			return a.toUpperCase().compareTo(b.toUpperCase());
		}
	};
		
	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Usage:  filename");
			System.exit(1);
		}
		String filename = args[0];

		ArrayList<String> names = new ArrayList<String>();
		File files[] = (new File(".")).listFiles();
		for (File f : files)
			if (!f.isDirectory())
				names.add(f.getName());
		Collections.sort(names, UPPER_CASE_COMPARATOR);
		String list = String.join("\n", names);
		
		String fileContents;

		try {
			fileContents = new Scanner(new File(filename)).useDelimiter("\\Z").next();

			fileContents = fileContents.replace("@LIST@", list);
			
			PrintWriter out = new PrintWriter(filename);
			out.print(fileContents);
			out.close();

			return;
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(1);
		}
	}
}
