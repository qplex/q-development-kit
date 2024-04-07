// Stub generated by FactoryMaker

package tree;

import compiler.QType;
import parser.*;

public class RelationalExpressionNode extends QNode implements QParserTreeConstants {
	public RelationalExpressionNode() {
		super(JJTRELATIONALEXPRESSION);
	}

	void initialize() {
		if (children == null)
			return;
		
		_type = QType.BOOLEAN;
		
		for (int i=0; i<2; i++) {
			QType t = getChild(i)._type;
			switch(t._kind) {
			case INT:
			case REAL:
				break;
			default:
				throw new CompileException("Expected a number", getChild(i));
			}
		}
		
	}
}