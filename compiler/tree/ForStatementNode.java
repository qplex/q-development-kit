// Stub generated by FactoryMaker

package tree;

import compiler.*;
import parser.*;

public class ForStatementNode extends QNode implements QParserTreeConstants {
	public ForStatementNode() {
		super(JJTFORSTATEMENT);
	}

	void initialize() {
		_samplingDepth = getChild(1)._samplingDepth;
		_hasSkipBeforeSampling = getChild(1)._hasSkipBeforeSampling;
		_isTerminal = false;
		_returnValueNodes = getChild(1)._returnValueNodes;
	}
}
