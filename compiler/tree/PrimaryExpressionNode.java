// Stub generated by FactoryMaker

package tree;

import java.util.ArrayList;
import java.util.Arrays;

import compiler.*;
import parser.*;

public class PrimaryExpressionNode extends QNode implements QParserTreeConstants {

	public PrimaryExpressionNode() {
		super(JJTPRIMARYEXPRESSION);
	}

	void initialize() {
		Token firstToken = getToken(0);
		String image = firstToken.image;
		switch (firstToken.kind) {
		case TOKEN_IDENTIFIER:
			if (Engine._instance._tokenTable.getValueOfToken(image) < 0)
				throw new CompileException("Undefined token", this);
			_type = QType.INT;
			break;

		case IDENTIFIER: {
			Symbol symbol = Engine._instance._symbolTable.get(image);
			if (symbol == null)
				throw new CompileException("Unknown symbol " + image, this);
			_type = symbol._type;
			break;
		}
		case LPAREN:
			_type = getNode(1)._type;
			break;
		case NUMBER:
			_type = image.contains(".") ? QType.REAL : QType.INT;
			break;
		case TRUE:
		case FALSE:
			_type = QType.BOOLEAN;
			break;
		case MAX:
		case MIN:
			_type = builtinFunctionType(QType.INT, QType.INT, QType.INT);
			break;
		case BERNOULLI:
			_type = builtinFunctionType(QType.SIMPLE_PMF, QType.REAL);
			break;
		case COMPUTELEFTTAIL:
		case COMPUTERIGHTTAIL:
			_type = builtinFunctionType(QType.INT, QType.SIMPLE_PMF, QType.REAL);
			break;
		case BINOMIAL:
			_type = builtinFunctionType(QType.SIMPLE_PMF, QType.INT, QType.REAL);
			break;
		case HYPERGEOMETRIC:
			_type = builtinFunctionType(QType.SIMPLE_PMF, QType.INT, QType.INT, QType.INT);
			break;
		case BRANCHPROBABILITY:
		case RANDOMREAL:
			_type = builtinFunctionType(QType.REAL);
			break;
		case FLOOR:
		case CEILING:
			_type = builtinFunctionType(QType.INT, QType.REAL);
			break;
		case MULTINOMIAL:
			_type = builtinFunctionType(new QType(PMF, (Qualifier) null), QType.INT, QType.INT, QType.SIMPLE_PMF);
			break;
		case MULTIVARIATEHYPERGEOMETRIC:
			_type = builtinFunctionType(new QType(PMF, (Qualifier) null), QType.INT, QType.INT, QType.INT, QType.INTARRAY);
			break;
		case CREATEPMFFROMREALARRAY:
			_type = builtinFunctionType(QType.SIMPLE_PMF, QType.REALARRAY);
			break;
		case EXP:
		case LOG:
		case SQRT:
			_type = builtinFunctionType(QType.REAL, QType.REAL);
			break;
		case POW:
			_type = builtinFunctionType(QType.REAL, QType.REAL, QType.REAL);
			break;
		case RANDOMINT:
		case FACTORIAL:
			_type = builtinFunctionType(QType.INT, QType.INT);
			break;
		case ISSAMEPMFINSTANCE:
			_type = builtinFunctionType(QType.BOOLEAN, QType.PMF, QType.PMF);
			break;
		default:
			assert(false);
		}
	}
	
	static QType builtinFunctionType(QType returnType, QType... args) {
		Signature signature = new Signature();
		signature._returnType = returnType;
		signature._parameterTypes = new ArrayList<QType>(Arrays.asList(args));
		return new QType(QType.FUNCTION_KIND, signature);
	}
}