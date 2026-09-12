package compiler;

import java.util.ArrayList;
import java.util.List;

import parser.QParserConstants;
import tree.QualifierNode;
import tree.TypeNode;

/**
 * A Q-language type. For simple types (such as int), an immutable instance is
 * available as a final constant. Pmf types must be accompanied by a qualifier.
 * Function and interface types must be accompanied by a signature.
 */
public class QType {

	/** The semantic category of a Q type. */
	public enum Kind {
		INT, REAL, BOOLEAN, VOID,
		INTARRAY, REALARRAY, BOOLEANARRAY,
		INTMATRIX, REALMATRIX, BOOLEANMATRIX,
		PMF, PMFARRAY, PMFMATRIX,
		INTERFACE, INTERFACEARRAY, INTERFACEMATRIX, FUNCTION, RETURN
	}

	/** The semantic kind of this type. */
	public final Kind _kind;

	public final Qualifier _qualifier;
	public final Signature _signature;

	private QType(Kind kind) {
		_kind = kind;
		_qualifier = null;
		_signature = null;
	}

	/**
	 * Instantiates a Pmf type.
	 * 
	 * @param kind      The kind of this type: PMF, PMFARRAY, PMFMATRIX or RETURN.
	 * @param qualifier The qualifier for this type.
	 */
	public QType(Kind kind, Qualifier qualifier) {
		_kind = kind;
		_qualifier = qualifier;
		_signature = null;
		switch (kind) {
		case PMF:
		case PMFARRAY:
		case PMFMATRIX:
		case RETURN:
			return;
		default:
			throw new IllegalArgumentException("Not a Pmf or return type: " + kind);
		}
	}

	/**
	 * Instantiates a function or interface type.
	 * 
	 * @param kind      The kind of this type: FUNCTION, INTERFACE, INTERFACEARRAY
	 *                  or INTERFACEMATRIX.
	 * @param signature The signature for this type.
	 */
	public QType(Kind kind, Signature signature) {
		_kind = kind;
		_qualifier = null;
		_signature = signature;
		switch (kind) {
		case INTERFACEARRAY:
		case INTERFACEMATRIX:
		case INTERFACE:
		case FUNCTION:
			return;
		default:
			throw new IllegalArgumentException("Not a function or interface type: " + kind);
		}
	}

	/**
	 * Whether this type carries a conditional qualifier, such as Pmf{A|B}.
	 * No expression can produce a value of such a type (conditional extraction
	 * requires fixed values and yields an ordinary pmf).
	 * 
	 * @return Whether this type carries a conditional qualifier.
	 */
	public boolean hasConditionalQualifier() {
		return _qualifier != null && _qualifier._category == Qualifier.Category.CONDITIONAL;
	}

	/**
	 * Returns the QType associated with a type node in the parse tree.
	 * 
	 * @param node The type node whose qtype is to be produced.
	 * @return The desired QType.
	 */
	public static QType getType(TypeNode node) {
		Kind kind = kindForParserToken(node.getToken(0).kind);
		if (node.getTokenAndNodeCount() == 2)  // The second item is a QualifierNode.
			return new QType(kind, new Qualifier((QualifierNode) node.getNode(1)));
		switch (kind) {
			case INT:
				return QType.INT;
			case REAL:
				return QType.REAL;
			case BOOLEAN:
				return QType.BOOLEAN;
			case VOID:
				return QType.VOID;
			case INTARRAY:
				return QType.INTARRAY;
			case REALARRAY:
				return QType.REALARRAY;
			case BOOLEANARRAY:
				return QType.BOOLEANARRAY;
			case INTMATRIX:
				return QType.INTMATRIX;
			case REALMATRIX:
				return QType.REALMATRIX;
			case BOOLEANMATRIX:
				return QType.BOOLEANMATRIX;
			case PMF:
				return QType.SIMPLE_PMF;
			case PMFARRAY:
				return QType.SIMPLE_PMFARRAY;
			case PMFMATRIX:
				return QType.SIMPLE_PMFMATRIX;
			default:
				throw new IllegalArgumentException("Not a source type: " + kind);
		}
	}

	private static Kind kindForParserToken(int tokenKind) {
		switch (tokenKind) {
		case QParserConstants.INT: return Kind.INT;
		case QParserConstants.REAL: return Kind.REAL;
		case QParserConstants.BOOLEAN: return Kind.BOOLEAN;
		case QParserConstants.VOID: return Kind.VOID;
		case QParserConstants.INTARRAY: return Kind.INTARRAY;
		case QParserConstants.REALARRAY: return Kind.REALARRAY;
		case QParserConstants.BOOLEANARRAY: return Kind.BOOLEANARRAY;
		case QParserConstants.INTMATRIX: return Kind.INTMATRIX;
		case QParserConstants.REALMATRIX: return Kind.REALMATRIX;
		case QParserConstants.BOOLEANMATRIX: return Kind.BOOLEANMATRIX;
		case QParserConstants.PMF: return Kind.PMF;
		case QParserConstants.PMFARRAY: return Kind.PMFARRAY;
		case QParserConstants.PMFMATRIX: return Kind.PMFMATRIX;
		default: throw new IllegalArgumentException("Not a Q type token: " + tokenKind);
		}
	}

	/** The shared Q type for {@code int}. */
	public static final QType INT = new QType(Kind.INT);

	/** The shared Q type for {@code real}. */
	public static final QType REAL = new QType(Kind.REAL);

	/** The shared Q type for {@code boolean}. */
	public static final QType BOOLEAN = new QType(Kind.BOOLEAN);

	/** The shared Q type for {@code void}. */
	public static final QType VOID = new QType(Kind.VOID);

	/** The shared Q type for {@code IntArray}. */
	public static final QType INTARRAY = new QType(Kind.INTARRAY);

	/** The shared Q type for {@code RealArray}. */
	public static final QType REALARRAY = new QType(Kind.REALARRAY);

	/** The shared Q type for {@code BooleanArray}. */
	public static final QType BOOLEANARRAY = new QType(Kind.BOOLEANARRAY);

	/** The shared Q type for {@code IntMatrix}. */
	public static final QType INTMATRIX = new QType(Kind.INTMATRIX);

	/** The shared Q type for {@code RealMatrix}. */
	public static final QType REALMATRIX = new QType(Kind.REALMATRIX);

	/** The shared Q type for {@code BooleanMatrix}. */
	public static final QType BOOLEANMATRIX = new QType(Kind.BOOLEANMATRIX);

	/** The shared Q type for {@code InterfaceArray}. */
	public static final QType INTERFACEARRAY = new QType(Kind.INTERFACEARRAY);

	/** The shared Q type for {@code InterfaceMatrix}. */
	public static final QType INTERFACEMATRIX = new QType(Kind.INTERFACEMATRIX);

	/** The shared qualifier-free Q type for {@code Pmf}. */
	public static final QType PMF = new QType(Kind.PMF);

	/** The shared qualifier-free Q type for {@code PmfArray}. */
	public static final QType PMFARRAY = new QType(Kind.PMFARRAY);

	/** The shared qualifier-free Q type for {@code PmfMatrix}. */
	public static final QType PMFMATRIX = new QType(Kind.PMFMATRIX);

	/** The shared one-variable Q type for {@code Pmf}. */
	public static final QType SIMPLE_PMF = new QType(Kind.PMF, new Qualifier(1));

	/** The shared one-variable Q type for {@code PmfArray}. */
	public static final QType SIMPLE_PMFARRAY = new QType(Kind.PMFARRAY, new Qualifier(1));

	/** The shared one-variable Q type for {@code PmfMatrix}. */
	public static final QType SIMPLE_PMFMATRIX = new QType(Kind.PMFMATRIX, new Qualifier(1));

	/**
	 * Reports whether this QType is scalar
	 * 
	 * @return true is this QType is int, real or boolean. False otherwise.
	 */
	public boolean isScalar() {
		switch (_kind) {
		case INT:
		case REAL:
		case BOOLEAN:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Reports whether this QType is a number
	 * 
	 * @return true is this QType is int or real. False otherwise.
	 */
	public boolean isNumber() {
		switch (_kind) {
		case INT:
		case REAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Reports whether this QType is a data object (a pmf, or one of the eight
	 * array/matrix types)
	 *
	 * @return true if this QType is one of the nine data-object types. False otherwise.
	 */
	public boolean isDataObject() {
		switch (_kind) {
		case PMF:
		case PMFARRAY:
		case PMFMATRIX:
		case INTARRAY:
		case REALARRAY:
		case BOOLEANARRAY:
		case INTMATRIX:
		case REALMATRIX:
		case BOOLEANMATRIX:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Reports whether this QType can be typecast.
	 * 
	 * @param t The new type to be cast to.
	 * @return True if the cast is permissible. False otherwise.
	 */
	public boolean isAssignableFrom(QType t) {
		if (_kind == Kind.REAL && t._kind == Kind.INT)
			return true;
		
		switch (_kind) {
		case PMF:
		case PMFARRAY:
		case PMFMATRIX:
			break;
		case INTERFACE:
			if (t._kind != Kind.FUNCTION)
				return false;
			if (t._signature == null)
				return true;
			return _signature.equals(t._signature);
		case INTERFACEARRAY:
		case INTERFACEMATRIX:
			if (_kind != t._kind)
				return false;
			if (t._signature == null)
				return true;
			return _signature.equals(t._signature);
		default:
			return _kind == t._kind;
		}

		if (_kind != t._kind)
			return false;

		if (_qualifier == null && t._qualifier == null)
			return true;

		if (_qualifier == null || t._qualifier == null) {
			Qualifier q;
			if (_qualifier != null)
				q = _qualifier;
			else 
				q = t._qualifier;
			
			return q._simpleRVNames.size() == 1;
		}

		if (_qualifier._category != t._qualifier._category)
			return false;

		switch (_qualifier._category) {
		case SIMPLE: {
			int n1 = t._qualifier._simpleRVNames.size();
			int n2 = this._qualifier._simpleRVNames.size();
			if (n1 != n2)
				return false;
			for (int i = 0; i < n1; i++) {
				String s1 = t._qualifier._simpleRVNames.get(i);
				String s2 = this._qualifier._simpleRVNames.get(i);
				if (s1 == null || s2 == null || s1.equals(s2))
					continue;
				return false;
			}
			return true;
		}
		case COMPOUND: {
			int n1 = t._qualifier._compoundRVNames.size();
			int n2 = this._qualifier._compoundRVNames.size();
			if (n1 != n2)
				return false;
			for (int i = 0; i < n1; i++) {
				ArrayList<String> a1 = t._qualifier._compoundRVNames.get(i);
				ArrayList<String> a2 = this._qualifier._compoundRVNames.get(i);
				if (a1.size() != a2.size())
					return false;

				for (int j = 0; j < a1.size(); j++) {
					String s1 = a1.get(j);
					String s2 = a2.get(j);
					if (s1 == null || s2 == null || s1.equals(s2))
						continue;
					return false;
				}
			}
			return true;
		}
		default:
			return true;
		}
	}

	private static boolean isEqualIgnoreNull(String a, String b) {
		return a == null || b == null || a.equals(b);
	}

	private static boolean isEqualIgnoreNull(List<String> a, List<String> b, int n) {
		for (int i = 0; i < n; i++)
			if (!isEqualIgnoreNull(a.get(i), b.get(i)))
				return false;
		return true;
	}

	/**
	 * Attempts to cast this PMF type to a new qualifier (configuration).
	 * 
	 * @param qualifier The new qualifier.
	 * @return The new QType, if cast is permissible. Null otherwise.
	 */
	public QType castTo(Qualifier qualifier) {
		if (_qualifier == null)
			return new QType(Kind.PMF, qualifier);

		if (_qualifier._category == Qualifier.Category.COMPOUND) {
			for (int i = 0; i < _qualifier._compoundRVNames.size(); i++) {
				Qualifier componentQualifier = new Qualifier(_qualifier._compoundRVNames.get(i));
				QType componentType = new QType(Kind.PMF, componentQualifier);
				QType result = componentType.castTo(qualifier);
				if (result != null)
					return result;
			}

			// No match found, invalid PMF extraction
			return null;
		}

		switch (qualifier._category) {
		case SIMPLE:
			int prefixLength = qualifier._simpleRVNames.size();
			if (prefixLength >= 1 && prefixLength <= _qualifier._simpleRVNames.size() //
					&& isEqualIgnoreNull(qualifier._simpleRVNames, _qualifier._simpleRVNames, prefixLength))
				return new QType(Kind.PMF, new Qualifier(qualifier._simpleRVNames));

			return null;
		case CONDITIONAL: {
			int numConditions = qualifier._conditions.size();
			int numRetained = qualifier._simpleRVNames.size();
			int numOriginal = _qualifier._simpleRVNames.size();

			if (numConditions + numRetained > numOriginal)
				return null;

			ArrayList<String> conditionNames = new ArrayList<String>();
			for (Qualifier.Condition condition : qualifier._conditions)
				conditionNames.add(condition._rvName);
			if (!isEqualIgnoreNull(conditionNames, _qualifier._simpleRVNames, numConditions))
				return null;

			List<String> availableNames = _qualifier._simpleRVNames.subList(numConditions, numOriginal);
			if (!isEqualIgnoreNull(availableNames, qualifier._simpleRVNames, numRetained))
				return null;

			return new QType(Kind.PMF, new Qualifier(qualifier._simpleRVNames));
		}
		case COMPOUND:
			for (int i = 0; i < _qualifier._compoundRVNames.size(); i++) {
				ArrayList<String> a = _qualifier._compoundRVNames.get(i);
				Qualifier q = new Qualifier(a);
				QType t = new QType(Kind.PMF, q);
				QType tt = t.castTo(qualifier);
				if (tt != null)
					return tt;
			}
			return null;
		default:
			assert (false);
			return null;
		}
	}

	/**
	 * Identifies a simple Pmf in this compound Pmf that matches the given qualifier
	 * (configuration).
	 * 
	 * @param qualifier The given qualifier.
	 * @return The index of the matching distribution. -1 if none was found.
	 */
	public int compoundLookup(Qualifier qualifier) {
		assert (_qualifier._category == Qualifier.Category.COMPOUND);

		for (int i = 0; i < _qualifier._compoundRVNames.size(); i++) {
			ArrayList<String> a = _qualifier._compoundRVNames.get(i);
			Qualifier q = new Qualifier(a);
			QType t = new QType(Kind.PMF, q);
			if (t.castTo(qualifier) != null)
				return i;
		}

		return -1;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(_kind);
		if (_qualifier != null)
			b.append(_qualifier.toString());
		if (_signature != null)
			b.append(" " + _signature.toString());

		return b.toString();
	}

	public int hashCode() {
		return _kind.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof QType))
			return false;
		QType t = (QType) obj;
		if (t._kind != _kind)
			return false;
		if ((t._qualifier == null) != (_qualifier == null))
			return false;
		if ((t._signature == null) != (_signature == null))
			return false;
		if (_qualifier != null && !t._qualifier.equals(_qualifier))
			return false;
		if (_signature != null && !t._signature.equals(_signature))
			return false;
		return true;
	}
}
