package compiler;

import java.util.ArrayList;

import parser.Token;
import tree.CompileException;
import tree.QNode;
import tree.QualifierNode;

/** The configuration of a Pmf - for example, {A,B} or {A|B=2}. */
public class Qualifier implements parser.QParserConstants, parser.QParserTreeConstants {

	/**
	 * A condition associated with a conditional distribution - for example, the A=a
	 * part of {C|A=a,B=b}.
	 */
	public static class Condition {
		public final String _rvName; // The name of the random variable.
		public final QNode _valueNode; // The parse tree node that evaluates the given value.

		private Condition(String rvName, QNode valueNode) {
			_rvName = rvName;
			_valueNode = valueNode;
		}
	}

	/**
	 * The three categories of Pmf: SIMPLE, COMPOUND and CONDITIONAL.
	 */
	public enum Category {
		SIMPLE, COMPOUND, CONDITIONAL
	};

	/** The Category of this qualifier. */
	public final Category _category;

	/**
	 * For SIMPLE and CONDITIONAL qualifiers, the associated random variable names.
	 * For an associated question mark, the entry is null.
	 */
	public ArrayList<String> _simpleRVNames;

	/**
	 * For COMPOUND qualifiers, the list of lists of associated random variable
	 * names.
	 */
	public ArrayList<ArrayList<String>> _compoundRVNames;

	/** For CONDITIONAL qualifiers, the list of conditions. */
	public ArrayList<Condition> _conditions;

	private static String filterQuestionMark(String s) {
		if (s.equals("?"))
			return null;
		else
			return s;
	}

	private static boolean isRVName(Token token) {
		switch (token.kind) {
		case RV_IDENTIFIER:
		case IDENTIFIER:
		case NUMBER:
		case QUESTION_MARK:
			return true;
		default:
			return false;
		}
	}

	private static boolean isComma(Token token) {
		return token.kind == COMMA;
	}

	private static boolean isGiven(Token token) {
		return token.kind == GIVEN;
	}

	private static boolean isAssign(Token token) {
		return token.kind == ASSIGN;
	}

	private static boolean isLParen(Token token) {
		return token.kind == LPAREN;
	}

	private static boolean isRParen(Token token) {
		return token.kind == RPAREN;
	}

	// Traverses a sublist of children and constructs an array list of names.
	private static ArrayList<String> getNames(QNode node, int startIndex, int endIndex) {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = startIndex; i < endIndex; i++) {
			switch ((i - startIndex) % 2) {
			case 0:
				if (!isRVName(node.getToken(i)))
					throw new CompileException("Syntax error", node.getToken(i));
				a.add(filterQuestionMark(node.getToken(i).image));
				continue;
			case 1:
				if (!isComma(node.getToken(i)))
					throw new CompileException("Syntax error", node.getToken(i));
				continue;
			}
		}
		return a;
	}

	// Traverses a sublist of children and constructs an array list of Conditions.
	private static ArrayList<Condition> getConditions(QNode node, int startIndex, int endIndex) {
		ArrayList<Condition> a = new ArrayList<Condition>();
		for (int i = startIndex; i < endIndex; i++) {
			switch ((i - startIndex) % 4) {
			case 0:
				if (!isRVName(node.getToken(i)))
					throw new CompileException("Syntax error", node.getToken(i));
				continue;
			case 1:
				if (!isAssign(node.getToken(i)))
					throw new CompileException("Syntax error", node.getToken(i));
				continue;

			case 2: {
				String rvName = filterQuestionMark(node.getToken(i - 2).image);
				Condition condition = new Condition(rvName, node.getNode(i));
				a.add(condition);
				continue;
			}
			case 3:
				if (!isComma(node.getToken(i)))
					throw new CompileException("Syntax error", node.getToken(i));
				continue;
			}
		}
		return a;
	}

	/**
	 * Creates a Qualifier instance from a parse tree node that specifies the
	 * qualifier.
	 */
	public Qualifier(QualifierNode node) {
		QNode child = node.getChild(0);
		switch (child.getId()) {
		case JJTSIMPLEQUALIFIER: {
			int n = child.getTokenAndNodeCount();

			int k = n;
			for (int i = 0; i < n; i++) {
				if (child.isToken(i) && isGiven(child.getToken(i))) {
					k = i;
					break;
				}
			}

			_simpleRVNames = getNames(child, 0, k);
			if (k % 2 != 1)
				throw new CompileException("Syntax error", child.getToken(k));

			if (k == n) {
				_category = Category.SIMPLE;
				break;
			} else {
				_conditions = getConditions(child, k + 1, n);
				_category = Category.CONDITIONAL;
				break;
			}
		}
		case JJTCOMPOUNDQUALIFIER: {
			int n = child.jjtGetNumChildren();
			if (n == 1) {
				QNode grandChild = child.getChild(0);
				int m = grandChild.getTokenAndNodeCount();
				_simpleRVNames = getNames(grandChild, 0, m);
				_category = Category.SIMPLE;
				break;
			} else {
				_compoundRVNames = new ArrayList<ArrayList<String>>();
				for (int i = 0; i < n; i++) {
					QNode grandChild = child.getChild(i);
					int m = grandChild.getTokenAndNodeCount();
					_compoundRVNames.add(getNames(grandChild, 0, m));
				}
				_category = Category.COMPOUND;
				break;
			}
		}
		default:
			assert (false);
			_category = null;
			break;
		}
	}

	/**
	 * Creates a Qualifier instance for {?,?...} with a specified number of
	 * question marks.
	 */
	public Qualifier(int n) {
		_category = Category.SIMPLE;
		_simpleRVNames = new ArrayList<String>();
		for (int i = 0; i < n; i++)
			_simpleRVNames.add(null);
	}

	/**
	 * Creates a Qualifier from an array of random variable counts. If the array has
	 * size greater than one, then the created Qualified is COMPOUND. Otherwise, it
	 * is SIMPLE.
	 */
	public Qualifier(int[] a) {
		ArrayList<ArrayList<String>> b = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < a.length; i++) {
			ArrayList<String> c = new ArrayList<String>();
			for (int j = 0; j < a[i]; j++)
				c.add(null);
			b.add(c);
		}
		if (a.length == 1) {
			_category = Category.SIMPLE;
			_simpleRVNames = b.get(0);
		} else {
			_category = Category.COMPOUND;
			_compoundRVNames = b;
		}
	}

	/** Creates a SIMPLE qualifier from an array of RV names. */
	public Qualifier(ArrayList<String> a) {
		_category = Category.SIMPLE;
		_simpleRVNames = a;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("{");

		switch (_category) {
		case SIMPLE:
			for (int i = 0; i < _simpleRVNames.size(); i++) {
				if (i > 0)
					b.append(",");
				String s = _simpleRVNames.get(i);
				if (s == null)
					s = "?";
				b.append(s);
			}
			break;
		case COMPOUND:
			b.append("(");
			for (int i = 0; i < _compoundRVNames.size(); i++) {
				if (i > 0)
					b.append("),(");
				ArrayList<String> a = _compoundRVNames.get(i);
				for (int j = 0; j < a.size(); j++) {
					if (j > 0)
						b.append(",");
					b.append(a.get(j));
				}
			}
			b.append(")");
			break;
		case CONDITIONAL:
			for (int i = 0; i < _simpleRVNames.size(); i++) {
				if (i > 0)
					b.append(",");
				b.append(_simpleRVNames.get(i));
			}
			b.append("|");
			for (int i = 0; i < _conditions.size(); i++) {
				Condition condition = _conditions.get(i);
				if (i > 0)
					b.append(",");
				b.append(condition._rvName);
				b.append("=#");
			}
			break;
		}

		b.append("}");
		return b.toString();
	}
	
	public boolean equals(Object x) {
		return toString().equals(x.toString());
	}
}
