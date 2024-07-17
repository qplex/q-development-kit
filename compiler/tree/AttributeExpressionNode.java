// Stub generated by FactoryMaker

package tree;

import compiler.QType;
import parser.*;

public class AttributeExpressionNode extends QNode implements QParserTreeConstants {
	public AttributeExpressionNode() {
		super(JJTATTRIBUTEEXPRESSION);
	}

	void initialize() {
		if (children == null)
			return;
		
		QType sourceType = getChild(0)._type;
		switch(getNode(2).getToken(0).kind) {
		case MIN_VALUE:
		case MAX_VALUE:
			if (!sourceType.isAssignableFrom(QType.SIMPLE_PMF))
				throw new CompileException("Expected Pmf{?}", this);
			_type = QType.INT;
			return;
		case LENGTH:
			switch (sourceType._kind) {
			case INTARRAY:
			case REALARRAY:
			case BOOLEANARRAY:
			case PMFARRAY:
			case INTMATRIX:
			case REALMATRIX:
			case BOOLEANMATRIX:
			case PMFMATRIX:
				_type = QType.INT;
				return;
			default:
				throw new CompileException("Expected array or matrix", this);
			}
			default:
				assert(false);
		}
	}
}
