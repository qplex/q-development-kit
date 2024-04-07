package generator;

import java.io.*;
import java.util.ArrayList;

import compiler.Engine;
import tree.QNode;

/**
 * Utility class to generate CPP code from a template. The template may be a
 * *.txt file or a String. It contains markers starting with @ to be expanded.
 * <p>
 * To create a TemplateExpander instance, call constructor {@link FromLine} or
 * {@link FromFile}. Include the optional parameter <code>engine</code> if the
 * template expander requires access to the engine symbol table.
 * <p>
 * The simplest markers are specified by calls to method
 * <code>substitute</code>. For example,
 * <code>substitute("@KEY", value)</code> will replace every
 * instance of <code>"@KEY"</code> in the template with the given
 * <code>value</code>.
 * <p>
 * The markers <code>@REPEAT(</code> ... <code>@,</code> ... <code>@)</code> are
 * used to repeat a part of the template. For example, the template
 * <code>FunctionWrapper.txt</code> uses these markers to iterate through a list
 * of function parameters.
 * <p>
 * The markers <code>@IF_LOG(</code> ... <code>@)</code> designate a part of the
 * template to be included only if logging is activated.
 * <p>
 * The markers <code>@LOG(</code> ... <code>@,</code> ... <code>@)</code> identify a Log call if
 * logging is activated.  If logging is not activated, the final parameter
 * is written to source without the Log call.  Example:
 * <p>
 * <code>@LHS = @LOG(\"@PADDED_LINE_NUMBER@LHS <- \"@,@RHS@);</code> 
 * <p>
 * writes 
 * <p>
 * <code>LHS = LOG(stuff, RHS);</code>
 * <p>
 * if logging is enabled, or
 * <p>
 * <code>LHS = RHS;</code>
 * <p>
 * if logging is not enabled.
 * <p>
 * Finally, <code>@LOG_INTERFACE(</code> ... <code>@,</code> ... <code>@)</code> is
 * used in Python methods that represent interface variables as strings. 
 */

abstract class TemplateExpander {
	static class FromLine extends TemplateExpander {
		FromLine(String line) {
			super(line + "\n");
		}

		FromLine(Engine engine, String line) {
			super(engine, line + "\n");
		}
	}

	static class FromFile extends TemplateExpander {
		private static final String FOUR_SPACES = "    ";

		private static String readFile(String filename) {
			StringBuilder b = new StringBuilder();

			try {
				InputStream in = TemplateExpander.class.getResourceAsStream(filename);
				if (in == null)
					throw new IOException("File " + filename + " not found in package folder");
				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = r.readLine()) != null) {
					if (line.contains("\t"))
						for (int i = 0; i < line.length(); i++)
							if (line.charAt(i) == '\t')
								line = line.substring(0, i) + FOUR_SPACES.substring(0, 4 - i % 4)
										+ line.substring(i + 1);
					b.append(line);
					b.append("\n");
				}
				r.close();
			} catch (IOException x) {
				assert (false);
			}

			return b.toString();
		}

		FromFile(String filename) {
			super(readFile(filename));
		}

		FromFile(Engine engine, String filename) {
			super(engine, readFile(filename));
		}
	}

	private static class Repetition {
		private ArrayList<String> _keys = new ArrayList<String>();
		private ArrayList<String> _values = new ArrayList<String>();
	}

	private static class Macro {
		private StringBuffer _b;
		private int _pos;
		private String[] fields;

		Macro(StringBuffer b, String key) {
			_b = b;
			_pos = b.indexOf(key);
			if (_pos < 0)
				return;

			int endPos = b.indexOf("@)", _pos);
			assert (endPos >= 0);

			String s = b.substring(_pos + key.length(), endPos);
			b.replace(_pos, endPos + 2, "");
			fields = s.split("@,");
		}

		void insert(String s) {
			_b.insert(_pos, s);
			_pos += s.length();
		}
	}

	private String _source;
	private ArrayList<String> _keys = new ArrayList<String>();
	private ArrayList<String> _values = new ArrayList<String>();
	private ArrayList<Repetition> _repetitions = new ArrayList<Repetition>();
	private Repetition _currentRepetition;
	private Engine _engine;

	private TemplateExpander(String source) {
		_source = source;
	}

	private TemplateExpander(Engine engine, String source) {
		_engine = engine;
		_source = source;
	}

	TemplateExpander substitute(String key, String value) {
		if (_currentRepetition == null) {
			_keys.add(key);
			_values.add(value);
			return this;
		}

		_currentRepetition._keys.add(key);
		_currentRepetition._values.add(value);
		return this;
	}

	TemplateExpander substitute(String key, int intValue) {
		return substitute(key, String.valueOf(intValue));
	}

	TemplateExpander substitute(String key, QNode node) {
		return substitute(key, ExpressionGenerator.generate(_engine, node));
	}

	TemplateExpander repeat() {
		_currentRepetition = new Repetition();
		_repetitions.add(_currentRepetition);
		return this;
	}

	void run() {
		StringBuffer b = new StringBuffer(_source);
		for (int i = 0; i < _keys.size(); i++) {
			String key = _keys.get(i);
			String value = _values.get(i);
			int k;
			while ((k = b.indexOf(key)) >= 0)
				b.replace(k, k + key.length(), value);
		}

		while (true) {
			Macro m = new Macro(b, "@REPEAT(");
			if (m._pos < 0)
				break;
			String block = m.fields[0];
			String separator;
			if (m.fields.length > 1)
				separator = m.fields[1];
			else
				separator = null;

			boolean isFirstTime = true;
			for (Repetition repetition : _repetitions) {
				if (isFirstTime)
					isFirstTime = false;
				else if (separator != null)
					m.insert(separator);

				StringBuffer bb = new StringBuffer(block);
				for (int i = 0; i < repetition._keys.size(); i++) {
					String key = repetition._keys.get(i);
					String value = repetition._values.get(i);
					int k;
					while ((k = bb.indexOf(key)) >= 0)
						bb.replace(k, k + key.length(), value);
				}
				m.insert(bb.toString());
			}
		}

		while (true) {
			Macro m = new Macro(b, "@LOG(");
			if (m._pos < 0)
				break;
			if (Generator._logActivated) {
				if (m.fields.length == 1)
					m.insert("Log(" + m.fields[0] + ")");
				else
					m.insert("Log(" + m.fields[0] + "," + m.fields[1] + ")");
			} else if (m.fields.length > 1)
				m.insert(m.fields[1]);
		}

		while (true) {
			Macro m = new Macro(b, "@LOG_INTERFACE(");
			if (m._pos < 0)
				break;
			if (Generator._logActivated) {
				if (m.fields.length == 1)
					m.insert("Log(functionNameFromPointer((QObject *)self, (void *) (" + m.fields[0] + "))");
				else
					m.insert("Log(" + m.fields[0] + "," + "functionNameFromPointer((QObject *)self, (void *) ("
							+ m.fields[1] + ")))");
			} else if (m.fields.length > 1)
				m.insert(m.fields[1]);
		}

		while (true) {
			Macro m = new Macro(b, "@IF-LOG(");
			if (m._pos < 0)
				break;
			if (Generator._logActivated)
				m.insert(m.fields[0]);
		}

		assert (b.indexOf("@") < 0);

		Generator._cSourceWriter.print(b);
	}
}
