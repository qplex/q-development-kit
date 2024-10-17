import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import compiler.Engine;
import generator.Generator;

/**
 * Application entry point
 */
public class QCompiler {

	/**
	 * Application entry point. Reads command line parameters. Then uses
	 * {@link compiler.Engine} to compile each Q source file, and
	 * {@link generator.Generator} to generate the CPP.
	 */
	public static void main(String[] args) {
		try {
			boolean isLogActivated;
			String modulename;
			ArrayList<String> sourceFilenames = new ArrayList<String>();
			ArrayList<Engine> allEngines = new ArrayList<Engine>();

			System.out.println("QCompiler " + Version.VERSION);

			if (args.length < 2)
				abortUsage();

			isLogActivated = args[0].equals("-log");

			int k = isLogActivated ? 1 : 0;
			modulename = args[k];

			if (!isValidModulename(modulename)) {
				System.err.println("Not a valid module name: " + modulename);
				System.exit(1);
			}

			{
				HashSet<String> h = new HashSet<String>();
				for (int i = k + 1; i < args.length; i++) {
					String name = args[i].toLowerCase();
					if (h.contains(name))
						abort("Duplicate source filename");
					h.add(name);
					sourceFilenames.add(args[i]);
				}
			}

			for (String name : sourceFilenames) {
				if (!isValidSourceFilename(name)) {
					System.err.println("Not a valid source file name: " + name);
					System.exit(1);
				}
				if (!(new File(name).exists() && new File(name).isFile())) {
					System.err.println("File not found: " + name);
					System.exit(1);
				}
			}

			if (isLogActivated && sourceFilenames.size() > 1)
				abort("Logging is not permitted when there is more than one source file");

			for (int i = 0; i < sourceFilenames.size(); i++) {
				String filename = sourceFilenames.get(i);
				Engine engine = new Engine(filename);
				allEngines.add(engine);
			}

			Generator.run(isLogActivated, modulename, allEngines);

			System.out.println("File " + modulename + ".cpp successfully created.");
			if (isLogActivated) {
				String logFilename = modulename + ".log";
				File logFile = new File(logFilename);
				logFilename = logFile.getCanonicalPath();
				System.out.println("Will log to " + logFilename);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	// A valid module name contains lower-case letters, digits and underscores, and
	// starts with a letter or an underscore.
	private static boolean isValidModulename(String name) {
		char c = name.charAt(0);
		if (Character.isDigit(c))
			return false;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i);
			if (Character.isLowerCase(c))
				continue;
			if (c == '_')
				continue;
			if (Character.isDigit(c))
				continue;
			return false;
		}
		return true;
	}

	// A valid source file name contains letters and digits, and
	// starts with an upper-case letter.
	private static boolean isValidSourceFilename(String name) {
		if (!name.endsWith(".q"))
			return false;

		name = name.substring(0, name.length() - 2);

		char c = name.charAt(0);
		if (!Character.isUpperCase(c))
			return false;
		for (int i = 1; i < name.length(); i++) {
			c = name.charAt(i);
			if (Character.isLetter(c))
				continue;
			if (Character.isDigit(c))
				continue;
			return false;
		}
		return true;
	}

	private static void abortUsage() {
		// Logging remains as an undocumented feature.
		// System.err.println("Usage: java -jar QCompiler.jar [-log] module_name SourceFilename.q ...");
		System.err.println("Usage: java -jar QCompiler.jar module_name SourceFilename.q ...");
		System.exit(1);
	}

	private static void abort(String msg) {
		System.err.println("Error:  " + msg);
		System.exit(1);
	}
}
