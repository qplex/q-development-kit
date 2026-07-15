package analysis;

/**
 * Emits compiler warnings. Warnings go to stderr and affect neither the exit
 * code nor the generated C++.
 */
public class Diagnostics {
	public static void warn(String sourceName, int lineNumber, String message) {
		System.err.println("Warning (" + sourceName + ", line " + lineNumber + "): " + message);
	}
}
