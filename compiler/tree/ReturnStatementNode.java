// Stub generated by FactoryMaker

package tree;

import java.util.ArrayList;

import parser.*;

public class ReturnStatementNode extends QNode implements QParserTreeConstants {
	public ReturnStatementNode() {
		super(JJTRETURNSTATEMENT);
	}

	void initialize() {
		if (jjtGetNumChildren() == 0)
			return;
		
		_isTerminal = true;

		_returnValueNodes = new ArrayList<QNode>();
		_returnValueNodes.add(getNode(1));
	}
}
