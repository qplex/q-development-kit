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

	/**
	 * For all other kinds, the QParser keyword is used. But Q does not have a
	 * keyword for functions.
	 */
	public static final int FUNCTION_KIND = -1;

	/** The QParser keyword for this type. */
	public final int _kind;

	public final String _qName, _cName, _xName;
	public final Qualifier _qualifier;
	public final Signature _signature;

	private QType(int kind, String qName, String cName, String xName) {
		_kind = kind;
		_qName = qName;
		_cName = cName;
		_xName = xName;
		_qualifier = null;
		_signature = null;
	}

	/**
	 * Instantiates a Pmf type.
	 * 
	 * @param kind      The QParserConstants code for this type: PMF, PMFARRAY,
	 *                  PMFMATRIX or RETURN.
	 * @param qualifier The qualifier for this type.
	 */
	public QType(int kind, Qualifier qualifier) {
		_kind = kind;
		_qualifier = qualifier;
		_signature = null;

		switch (kind) {
		case QParserConstants.PMF:
			_qName = "Pmf";
			_cName = "Pmf *";
			_xName = "Pmf";
			return;
		case QParserConstants.PMFARRAY:
			_qName = "PmfArray";
			_cName = "PmfArray *";
			_xName = "PmfArray";
			return;
		case QParserConstants.PMFMATRIX:
			_qName = "PmfMatrix";
			_cName = "PmfMatrix *";
			_xName = "PmfMatrix";
			return;
		case QParserConstants.RETURN:
			_qName = _cName = _xName = null;
			return;
		default:
			assert (false);
			_qName = _cName = _xName = null;
		}
	}

	/**
	 * Instantiates a function or interface type.
	 * 
	 * @param kind      The QParserConstants code for this type: FUNCTION_NEW,
	 *                  INTERFACE, INTERFACEARRAY or INTERFACEMATRIX.
	 * @param signature The signature for this type.
	 */
	public QType(int kind, Signature signature) {
		_kind = kind;
		_qualifier = null;
		_signature = signature;
		switch (kind) {
		case QParserConstants.INTERFACEARRAY:
			_qName = _xName = "InterfaceArray";
			_cName = "InterfaceArray *";
			break;
		case QParserConstants.INTERFACEMATRIX:
			_qName = _xName = "InterfaceMatrix";
			_cName = "InterfaceMatrix *";
			break;
		default:
			_cName = _qName = _xName = null;
			break;
		}
	}

	/**
	 * Returns the QType associated with a type node in the parse tree.
	 * 
	 * @param node The type node whose qtype is to be produced.
	 * @return The desired QType.
	 */
	public static QType getType(TypeNode node) {
		if (node.getTokenAndNodeCount() > 1)
			return new QType(node.getToken(0).kind, new Qualifier((QualifierNode) node.getNode(1)));
		else {
			switch (node.getToken(0).kind) {
			case QParserConstants.INT:
				return QType.INT;
			case QParserConstants.REAL:
				return QType.REAL;
			case QParserConstants.BOOLEAN:
				return QType.BOOLEAN;
			case QParserConstants.VOID:
				return QType.VOID;
			case QParserConstants.INTARRAY:
				return QType.INTARRAY;
			case QParserConstants.REALARRAY:
				return QType.REALARRAY;
			case QParserConstants.BOOLEANARRAY:
				return QType.BOOLEANARRAY;
			case QParserConstants.INTMATRIX:
				return QType.INTMATRIX;
			case QParserConstants.REALMATRIX:
				return QType.REALMATRIX;
			case QParserConstants.BOOLEANMATRIX:
				return QType.BOOLEANMATRIX;
			case QParserConstants.PMF:
				return QType.SIMPLE_PMF;
			case QParserConstants.PMFARRAY:
				return QType.SIMPLE_PMFARRAY;
			case QParserConstants.PMFMATRIX:
				return QType.SIMPLE_PMFMATRIX;
			default:
				assert (false);
				return null;
			}
		}
	}

	/** An instance of QType for Q int types. */
	public static final QType INT//
			= new QType(QParserConstants.INT, "int", "Int", "Int");

	/** An instance of QType for Q real types. */
	public static final QType REAL//
			= new QType(QParserConstants.REAL, "real", "Real", "Real");

	/** An instance of QType for Q boolean types. */
	public static final QType BOOLEAN //
			= new QType(QParserConstants.BOOLEAN, "boolean", "Boolean", "Boolean");

	/** An instance of QType for Q void types. */
	public static final QType VOID //
			= new QType(QParserConstants.VOID, "void", "void", "void");

	/** An instance of QType for Q IntArray types. */
	public static final QType INTARRAY//
			= new QType(QParserConstants.INTARRAY, "IntArray", "IntArray *", "IntArray");

	/** An instance of QType for Q RealArray types. */
	public static final QType REALARRAY //
			= new QType(QParserConstants.REALARRAY, "RealArray", "RealArray *", "RealArray");

	/** An instance of QType for Q BooleanArray types. */
	public static final QType BOOLEANARRAY //
			= new QType(QParserConstants.BOOLEANARRAY, "BooleanArray", "BooleanArray *", "BooleanArray");

	/** An instance of QType for Q IntMatrix types. */
	public static final QType INTMATRIX //
			= new QType(QParserConstants.INTMATRIX, "IntMatrix", "IntMatrix *", "IntMatrix");

	/** An instance of QType for Q RealMatrix types. */
	public static final QType REALMATRIX //
			= new QType(QParserConstants.REALMATRIX, "RealMatrix", "RealMatrix *", "RealMatrix");

	/** An instance of QType for Q BooleanMatrix types. */
	public static final QType BOOLEANMATRIX //
			= new QType(QParserConstants.BOOLEANMATRIX, "BooleanMatrix", "BooleanMatrix *", "BooleanMatrix");

	/** An instance of QType for Q interface array types. */
	public static final QType INTERFACEARRAY //
			= new QType(QParserConstants.INTERFACEARRAY, "InterfaceArray", null, null);

	/** An instance of QType for Q interface matrix types. */
	public static final QType INTERFACEMATRIX //
			= new QType(QParserConstants.INTERFACEMATRIX, "InterfaceMatrix", null, null);

	/** An instance of QType for Q simple (unqualified) Pmf types. */
	public static final QType PMF //
			= new QType(QParserConstants.PMF, "Pmf", null, null);

	/** An instance of QType for Q simple (unqualified) PmfArray types. */
	public static final QType PMFARRAY //
			= new QType(QParserConstants.PMFARRAY, "PmfArray", null, null);

	/** An instance of QType for Q simple (unqualified) PmfMatrix types. */
	public static final QType PMFMATRIX //
			= new QType(QParserConstants.PMFMATRIX, "PmfMatrix", null, null);

	/** An instance of QType for Q simple (unqualified) Pmf types. */
	public static final QType SIMPLE_PMF //
			= new QType(QParserConstants.PMF, new Qualifier(1));

	/** An instance of QType for Q simple (unqualified) PmfArray types. */
	public static final QType SIMPLE_PMFARRAY //
			= new QType(QParserConstants.PMFARRAY, new Qualifier(1));

	/** An instance of QType for Q simple (unqualified) PmfMatrix types. */
	public static final QType SIMPLE_PMFMATRIX //
			= new QType(QParserConstants.PMFMATRIX, new Qualifier(1));

	/**
	 * Reports whether this QType is scalar
	 * 
	 * @return true is this QType is int, real or boolean. False otherwise.
	 */
	public boolean isScalar() {
		switch (_kind) {
		case QParserConstants.INT:
		case QParserConstants.REAL:
		case QParserConstants.BOOLEAN:
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
		case QParserConstants.INT:
		case QParserConstants.REAL:
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
		if (_kind == QParserConstants.REAL && t._kind == QParserConstants.INT)
			return true;
		
		switch (_kind) {
		case QParserConstants.PMF:
		case QParserConstants.PMFARRAY:
		case QParserConstants.PMFMATRIX:
			break;

		case QParserConstants.INTERFACE:
			if (t._kind != QType.FUNCTION_KIND)
				return false;
			if (t._signature == null)
				return true;
			return _signature.equals(t._signature);

		case QParserConstants.INTERFACEARRAY:
		case QParserConstants.INTERFACEMATRIX:
			if (_kind != t._kind)
				return false;
			return t._signature == null;

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
			return new QType(QParserConstants.PMF, qualifier);

		if (_qualifier._category == Qualifier.Category.COMPOUND) {
			int n = qualifier._simpleRVNames.size();
			for (int i = 0; i < _qualifier._compoundRVNames.size(); i++) {
				if (n != _qualifier._compoundRVNames.get(i).size())
					continue;
				if (isEqualIgnoreNull(qualifier._simpleRVNames, _qualifier._compoundRVNames.get(i), n))
					return new QType(QParserConstants.PMF, qualifier);
			}

			return null;
		}

		switch (qualifier._category) {
		case SIMPLE:
			if (qualifier._simpleRVNames.size() == 1 //
					&& isEqualIgnoreNull(qualifier._simpleRVNames, _qualifier._simpleRVNames, 1))
				return new QType(QParserConstants.PMF, new Qualifier(qualifier._simpleRVNames));

			if (qualifier._simpleRVNames.size() == _qualifier._simpleRVNames.size() //
					&& isEqualIgnoreNull(qualifier._simpleRVNames, _qualifier._simpleRVNames,
							qualifier._simpleRVNames.size()))
				return new QType(QParserConstants.PMF, new Qualifier(qualifier._simpleRVNames));

			return null;

		case CONDITIONAL: {
			int numConditions = qualifier._conditions.size();
			int numRetained = qualifier._simpleRVNames.size();
			int numOriginal = _qualifier._simpleRVNames.size();

			if (numConditions + numRetained > numOriginal)
				return null;

			if (numConditions + numRetained < numOriginal && numRetained != 1)
				return null;

			ArrayList<String> conditionNames = new ArrayList<String>();
			for (Qualifier.Condition condition : qualifier._conditions)
				conditionNames.add(condition._rvName);
			if (!isEqualIgnoreNull(conditionNames, _qualifier._simpleRVNames, numConditions))
				return null;

			List<String> availableNames = _qualifier._simpleRVNames.subList(numConditions, numOriginal);
			if (!isEqualIgnoreNull(availableNames, qualifier._simpleRVNames, numRetained))
				return null;

			return new QType(QParserConstants.PMF, new Qualifier(qualifier._simpleRVNames));
		}
		case COMPOUND:
			for (int i = 0; i < _qualifier._compoundRVNames.size(); i++) {
				ArrayList<String> a = _qualifier._compoundRVNames.get(i);
				Qualifier q = new Qualifier(a);
				QType t = new QType(QParserConstants.PMF, q);
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
			QType t = new QType(QParserConstants.PMF, q);
			if (t.castTo(qualifier) != null)
				return i;
		}

		return -1;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(QParserConstants.tokenImage[_kind].replace("\"", ""));
		if (_qualifier != null)
			b.append(_qualifier.toString());
		if (_signature != null)
			b.append(" " + _signature.toString());

		return b.toString();
	}

	public int hashCode() {
		return _kind;
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
