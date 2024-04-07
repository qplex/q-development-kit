// Stub generated by FactoryMaker

package tree;

import compiler.QType;
import parser.*;

public class OrExpressionNode extends QNode implements QParserTreeConstants {
	public OrExpressionNode() {
		super(JJTOREXPRESSION);
	}

	void initialize() {
		if (children == null)
			return;
		
		_type = QType.BOOLEAN;
		
		for (int i=0; i<jjtGetNumChildren(); i++) {
			if (getChild(i)._type != QType.BOOLEAN) 
				throw new CompileException("Expected a boolean", getChild(i));
		}
	}
}