package generator;

import compiler.QType;
import parser.QParserConstants;

/** C++ names used by the original generator. */
public final class CppNames {

	private CppNames() {}

	public static String cppType(QType type) {
		switch (type._kind) {
		case INT: return "Int";
		case REAL: return "Real";
		case BOOLEAN: return "Boolean";
		case VOID: return "void";
		case INTARRAY: return "IntArray *";
		case REALARRAY: return "RealArray *";
		case BOOLEANARRAY: return "BooleanArray *";
		case INTMATRIX: return "IntMatrix *";
		case REALMATRIX: return "RealMatrix *";
		case BOOLEANMATRIX: return "BooleanMatrix *";
		case PMF: return "Pmf *";
		case PMFARRAY: return "PmfArray *";
		case PMFMATRIX: return "PmfMatrix *";
		case INTERFACEARRAY: return "InterfaceArray *";
		case INTERFACEMATRIX: return "InterfaceMatrix *";
		case INTERFACE:
		case FUNCTION:
		case RETURN:
			return null;
		default:
			throw new IllegalArgumentException("Unknown Q type " + type);
		}
	}

	public static String runtimeName(QType type) {
		String cppType = cppType(type);
		if (cppType == null)
			return null;
		return cppType.endsWith(" *") ? cppType.substring(0, cppType.length() - 2) : cppType;
	}

	/**
	 * C++ call name for a directly called built-in token. {@code BRANCHPROBABILITY}
	 * is excluded because its call also needs the current sampling depth.
	 */
	public static String name(int tokenKind) {
		switch (tokenKind) {
		case QParserConstants.BERNOULLI: return "bernoulli";
		case QParserConstants.BINOMIAL: return "binomial";
		case QParserConstants.COMPUTELEFTTAIL: return "computeLeftTail";
		case QParserConstants.COMPUTERIGHTTAIL: return "computeRightTail";
		case QParserConstants.CREATEPMFFROMREALARRAY: return "createPmfFromRealArray";
		case QParserConstants.CREATEBIVARIATEPMFFROMREALMATRIX: return "createBivariatePmfFromRealMatrix";
		case QParserConstants.EXP: return "exp";
		case QParserConstants.FLOOR: return "ifloor";
		case QParserConstants.CEILING: return "iceiling";
		case QParserConstants.HYPERGEOMETRIC: return "hypergeometric";
		case QParserConstants.ISSAMEPMFINSTANCE: return "isSamePmfInstance";
		case QParserConstants.LOG: return "safeLog";
		case QParserConstants.MAX: return "std::max";
		case QParserConstants.MIN: return "std::min";
		case QParserConstants.MULTINOMIAL: return "multinomial";
		case QParserConstants.MULTIVARIATEHYPERGEOMETRIC: return "multivariateHypergeometric";
		case QParserConstants.RANDOMINT: return "randomInt";
		case QParserConstants.RANDOMREAL: return "randomReal";
		case QParserConstants.POW: return "safePow";
		case QParserConstants.SQRT: return "safeSqrt";
		default: throw new IllegalArgumentException("Unknown built-in token " + tokenKind);
		}
	}
}
