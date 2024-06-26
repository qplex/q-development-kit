// Stub generated by FactoryMaker

package tree;

import parser.*;
import compiler.*;
import compiler.SymbolTable.SymbolAlreadyDeclaredException;

public class InterfaceStatementNode extends QNode implements QParserTreeConstants {
	public InterfaceStatementNode() {
		super(JJTINTERFACESTATEMENT);
	}

	void initialize() {
		boolean isPublic = isToken(0) && getToken(0).kind == PUBLIC;
		int k = isPublic ? 1 : 0;
		QNode parametersNode = getNode(k + 3);

		Signature signature = new Signature();
		signature._returnType = QType.getType((TypeNode) getNode(k + 1));
		for (int i = 0; i < parametersNode.jjtGetNumChildren(); i++) {
			signature._parameterTypes.add(QType.getType((TypeNode) parametersNode.getChild(i).getChild(0)));
			signature._parameterNames.add(parametersNode.getChild(i).getToken(1).image);
			signature._parameterNameTokens.add(parametersNode.getChild(i).getToken(1));
		}

		Symbol symbol = new Symbol();
		symbol._isPublic = isPublic;
		symbol._name = getToken(k + 2).image;
		symbol._signature = signature;
		symbol._type = new QType(INTERFACE, symbol._signature);

		try {
			Engine._instance._symbolTable.add(symbol);
		} catch (SymbolAlreadyDeclaredException e) {
			throw new CompileException("Duplicate symbol definition", getToken(k + 1));
		}

		Engine._instance._signatureTable.add(symbol._signature);
	}
}
