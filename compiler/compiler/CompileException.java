package compiler;

import parser.Token;
import tree.QNode;

/** An exception that captures the offending token or node. */
public class CompileException extends RuntimeException {
	public final String _message;
	public final Token _token;

	public CompileException(String message, Token token) {
		_message = message;
		_token = token;
	}

	public CompileException(String message, QNode node) {
		_message = message;
		_token = node.jjtGetFirstToken();
	}
}