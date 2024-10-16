package compiler;

import java.io.*;
import java.util.Collection;

import parser.*;
import tree.BlockNode;
import tree.CompileException;
import tree.DeclarationStatementNode;
import tree.ForStatementNode;
import tree.QNode;
import tree.SamplingStatementNode;

/**
 * Digest of a Q source file, including the parse tree root, symbol table, etc.
 */
public class Engine {
	public static Engine _instance;

	public final SignatureTable _signatureTable = new SignatureTable();
	public final SymbolTable _symbolTable = new SymbolTable();

	public final TokenTable _tokenTable = new TokenTable();
	public final String _engineName;

	private QNode _root;
	private FileInputStream _fis;
	private String _filename;

	public Engine(String filename) {
		_instance = this;
		_filename = filename;
		_engineName = filename.substring(0, filename.length() - 2);

		try {
			_fis = new FileInputStream(filename);
			QParser parser = new QParser(_fis);
			_root = (QNode) parser.run();
			checkForNameConflict();
		} catch (IOException x) {
			System.err.println(_filename);
			System.err.println(x.getMessage());
			System.exit(1);
		} catch (ParseException x) {
			Token lastSuccessfulToken = x.currentToken;
			if (lastSuccessfulToken.next != null) {
				abort("Syntax error", lastSuccessfulToken.next);
			} else {
				abort("Syntax error at end of input", lastSuccessfulToken);				
			}
		} catch (CompileException x) {
			// x.printStackTrace();
			abort(x._message, x._token);
		} catch (TokenMgrError x) {
			System.err.println(_filename);
			System.err.print(x.getMessage());
			System.exit(1);
		} catch (Exception x) {
			System.err.println(_filename);
			x.printStackTrace();
			System.exit(1);
		}
	}

	public QNode getRoot() {
		return _root;
	}

	private void showMessageLineAndCaret(String message, Token token) throws IOException {
		int lineNumber = token.beginLine;
		int columnNumber = token.beginColumn;

		BufferedReader r;
		r = new BufferedReader(new FileReader(_filename));

		String line = "";
		for (int i = 0; i < lineNumber; i++) {
			line = r.readLine();
		}

		// Fix line to match the parser's column number, which assumes a tab of 8.
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c != '\t') {
				b.append(c);
			} else
				do {
					b.append(' ');
				} while (b.length() % 8 != 0);
		}
		line = b.toString();

		System.err.println(_filename);
		System.err.println(message.trim() + " at Line " + lineNumber + ", Column " + columnNumber);
		System.err.println(line);
		b = new StringBuilder();
		for (int i = 1; i < columnNumber; i++)
			b.append(" ");
		b.append("^");
		System.err.println(b.toString());

		r.close();
	}

	private void abort(String message, Token token) {
		try {
			_fis.close();
			showMessageLineAndCaret(message, token);
		} catch (Exception x) {
		}

		System.exit(1);
	}

	/*
	 * A fix for an unfortunate oversight in the original Q language spec, which was
	 * not discovered until beta testing. The problem is that a function parameter
	 * or local variable name may conflict with a global declaration that occurs
	 * after the function in the Q source.
	 */
	private Collection<String> _globalNames;

	private void checkForNameConflict() {
		_globalNames = _symbolTable.globalNames();

		for (String name : _globalNames) {
			Symbol symbol = _symbolTable.get(name);
			Signature functionSignature = symbol._signature;

			if (functionSignature != null) {
				checkFunctionParameters(functionSignature);
				if (symbol._node != null)
					checkBlock((BlockNode) symbol._node.getChild(1).getChild(1));
			}
		}
	}

	private void checkFunctionParameters(Signature functionSignature) {
		for (int i = 0; i < functionSignature._parameterNames.size(); i++) {
			String paramName = functionSignature._parameterNames.get(i);
			Token paramToken = functionSignature._parameterNameTokens.get(i);
			if (_globalNames.contains(paramName))
				checkToken(paramToken);
		}
	}

	private void checkBlock(BlockNode block) {
		int n = block.jjtGetNumChildren();
		for (int i = 0; i < n; i++) {
			QNode child = (QNode) block.jjtGetChild(i);

			switch (child.getId()) {
			case QParserTreeConstants.JJTDECLARATIONSTATEMENT:
				checkToken(child.getToken(1));
				break;
			case QParserTreeConstants.JJTSAMPLINGSTATEMENT: {
				child = child.getChild(0);
				int m = child.getTokenAndNodeCount();
				for (int j=0; j<m; j+=2) 
					checkToken(child.getToken(j));
				break;
			}
			case QParserTreeConstants.JJTIFSTATEMENT:
				checkBlock((BlockNode) child.getChild(1));
				if (child.jjtGetNumChildren() > 2)
					checkBlock((BlockNode) child.getChild(2));
				break;
			case QParserTreeConstants.JJTFORSTATEMENT:
				checkToken(child.getNode(1).getToken(1));
				checkBlock((BlockNode) child.getChild(1));
				break;
			case QParserTreeConstants.JJTWHILESTATEMENT:
				checkBlock((BlockNode) child.getChild(1));
				break;
			}
		}
	}

	private void checkToken(Token localDeclarationToken) {
		String paramName = localDeclarationToken.toString();
		if (_globalNames.contains(paramName)) {
			Symbol globalSymbol = _symbolTable.get(paramName);
			int k = globalSymbol._isPublic ? 2 : 1;
			QNode globalDeclarationNode = globalSymbol._node;
			Token globalDeclarationToken = globalDeclarationNode.getToken(k);

			System.err.println("Conflicting declarations");
			System.err.println();

			try {
				showMessageLineAndCaret("Local " + paramName, localDeclarationToken);
				showMessageLineAndCaret("Global " + paramName, globalDeclarationToken);
			} catch (Exception x) {
			}
			System.exit(1);
		}
	}
}
