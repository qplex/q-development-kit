package compiler;

import java.util.ArrayList;

import parser.Token;

/**
 * Return type and parameter list for interfaces, functions, for-loops and
 * while-loops.
 */
public class Signature {
	public QType _returnType;
	public boolean _isSamplingFunction;
	public ArrayList<QType> _parameterTypes = new ArrayList<QType>();
	public ArrayList<String> _parameterNames = new ArrayList<String>();
	
	/**
	 * The parser tree tokens where each parameter name occurs. This is used in
	 * error messages.
	 */
	public ArrayList<Token> _parameterNameTokens = new ArrayList<Token>();

	private static Signature _lastSignatureCreated;

	public Signature() {
		_lastSignatureCreated = this;
	}

	/**
	 * Returns a signature instance cached by the constructor, and clears the cache.
	 * <P>
	 * Here's why: In a function declaration, the parameter list node and
	 * implementation block node are children of the function declaration node. The
	 * signature is determined by the parameter list, but the implementation block
	 * needs to know it in order to recognize parameter variables. Since children
	 * are created before their parents, these two child nodes have no way to
	 * connect through the tree when they are being initialized. So we need another
	 * way. This is it.
	 * <P>
	 * For-loop and while-loop blocks use the same mechanism to connect with their
	 * declarations.
	 * <P>
	 * If-blocks do not get local variables from a signature, so the if statement
	 * clears the cache preemptively.
	 */
	public static Signature getLastSignatureCreated() {

		Signature signature = _lastSignatureCreated;
		_lastSignatureCreated = null;
		return signature;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Signature))
			return false;
		Signature s = (Signature) obj;
		
		return (s._returnType.equals(_returnType) && s._parameterTypes.equals(_parameterTypes));
	}

	public int hashCode() {
		return _returnType.hashCode() + _parameterTypes.hashCode();
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(_returnType);
		b.append("(");

		boolean firstTime = true;
		for (int i = 0; i < _parameterTypes.size(); i++) {
			if (firstTime)
				firstTime = false;
			else
				b.append(",");
			b.append(_parameterTypes.get(i));
			b.append(" ");
			b.append(_parameterNames.get(i));
		}

		b.append(")");

		if (_isSamplingFunction)
			b.append(" sampling");
		return b.toString();
	}
}
