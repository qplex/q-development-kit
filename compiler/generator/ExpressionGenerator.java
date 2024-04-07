package generator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import compiler.Engine;
import compiler.QType;
import compiler.Qualifier;
import compiler.Signature;
import compiler.Symbol;
import parser.QParserConstants;
import parser.QParserTreeConstants;
import parser.Token;
import tree.QNode;
import tree.QualifierNode;
import tree.SuffixedExpressionNode;

/** Methods to generate CPP code from a node of the parse tree. */
class ExpressionGenerator implements QParserTreeConstants, QParserConstants {
	private enum PmfConfigType {
		SIMPLE, JOINT, COMPOUND
	};

	/** Generate CPP code for a given node of the parse tree. */
	static String generate(Engine engine, QNode node) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Token firstToken = node.jjtGetFirstToken();

		switch (node.getId()) {
		case JJTPRIMARYEXPRESSION:
			switch (firstToken.kind) {
			case LPAREN:
				pw.print("(");
				if (node.jjtGetNumChildren() > 0)
					pw.print(generate(engine, node.getChild(0)));
				pw.print(")");
				return sw.toString();
			case IDENTIFIER: {
				String name = node.getToken(0).image;
				if (engine != null) {
					Symbol symbol = engine._symbolTable.get(name);
					if (symbol != null && symbol._level == 0) {
						if (symbol._type._kind == QType.FUNCTION_KIND)
							pw.print("_" + engine._engineName);
						else
							pw.print("self->");
					}
				}
				pw.print("_");
				pw.print(name);
				return sw.toString();
			}
			case TOKEN_IDENTIFIER: {
				String token = firstToken.image;
				token = "_" + engine._engineName + "_token_" + token.substring(1, token.length() - 1);
				pw.print(token);
				return sw.toString();
			}
			case MIN:
				return "std::min";
			case MAX:
				return "std::max";
			case FLOOR:
				return "ifloor";
			case CEILING:
				return "iceiling";
			case LOG:
				return "safeLog";
			case POW:
				return "safePow";
			case SQRT:
				return "safeSqrt";
			case NUMBER:
			case TRUE:
			case FALSE:
			case BERNOULLI:
			case BINOMIAL:
			case MULTINOMIAL:
			case BRANCHPROBABILITY:
			case RANDOMINT:
			case RANDOMREAL:
			case CREATEPMFFROMREALARRAY:
			case EXP:
			case HYPERGEOMETRIC:
			case MULTIVARIATEHYPERGEOMETRIC:
			case COMPUTELEFTTAIL:
			case COMPUTERIGHTTAIL:
			case ISSAMEPMFINSTANCE:
				return firstToken.image;
			default:
				assert (false);
			}

		case JJTADDITIVEEXPRESSION:
		case JJTMULTIPLICATIVEEXPRESSION:
		case JJTANDEXPRESSION:
		case JJTOREXPRESSION:
		case JJTNEGATIVEEXPRESSION:
		case JJTNOTEXPRESSION:
		case JJTRELATIONALEXPRESSION:
		case JJTEQUALITYEXPRESSION: {
			boolean isDivision = false;
			for (int i = 0; i < node.getTokenAndNodeCount(); i++)
				if (node.isToken(i)) {
					Token token = node.getToken(i);
					pw.print(token.image);
					if (token.kind == DIV) {
						pw.print("checkDenominator(");
						isDivision = true;
					}
				} else {
					pw.print(generate(engine, node.getNode(i)));
					if (isDivision) {
						pw.print(")");
						isDivision = false;
					}
				}
			return sw.toString();
		}

		case JJTNEGATIVE:
			pw.print("-");
			return sw.toString();

		case JJTNOT:
			pw.print("!");
			return sw.toString();

		case JJTARRAYINITIALIZER: {
			QNode listNode = node.getChild(0);
			int n = listNode.jjtGetNumChildren();

			switch (node._type._kind) {
			case INTARRAY:
			case REALARRAY:
			case BOOLEANARRAY:
			case PMFARRAY:
			case INTERFACEARRAY:
				pw.print("initialize" + node._type._qName + "((QObject *)self," + n);
				for (int i = 0; i < n; i++) {
					pw.print(",");
					pw.print(generate(engine, listNode.getChild(i)));
				}
				pw.print(")");
				return sw.toString();
			case INTMATRIX:
			case REALMATRIX:
			case BOOLEANMATRIX:
			case PMFMATRIX:
			case INTERFACEMATRIX: {
				pw.print("initialize" + node._type._qName + "((QObject *) self," + n);
				for (int i = 0; i < n; i++) {
					int m = listNode.getChild(i).getChild(0).jjtGetNumChildren();
					pw.print(",");
					pw.print(m);
					for (int j = 0; j < m; j++) {
						pw.print(",");
						pw.print(generate(engine, listNode.getChild(i).getChild(0).getChild(j)));
					}
				}
				pw.print(")");
				return sw.toString();
			}
			default:
				assert (false);
				return sw.toString();
			}
		}

		case JJTPMFINITIALIZER: {
			PmfConfigType pmfConfigType;
			if (node.isToken(0) && node.getToken(0).kind == LPAREN)
				pmfConfigType = PmfConfigType.COMPOUND;
			else {
				QNode firstElement = node.getChild(0).getChild(0);
				if (firstElement.isToken(0) && firstElement.getToken(0).kind == LPAREN)
					pmfConfigType = PmfConfigType.JOINT;
				else
					pmfConfigType = PmfConfigType.SIMPLE;
			}

			switch (pmfConfigType) {
			case SIMPLE:
				pw.print("(new SimpleAccumulator((QObject *)self))");
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->toPmfAndDelete()");
				return sw.toString();
			case JOINT:
				pw.print("(new JointAccumulator((QObject *)self, "
						+ (node.getChild(0).getChild(0).jjtGetNumChildren() - 1) + "))");
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->toPmfAndDelete()");
				return sw.toString();

			case COMPOUND: {
				int n = node.jjtGetNumChildren();
				pw.print("createCompoundPmf((QObject *)self, " + n);
				for (int i = 0; i < n; i++) {
					pw.print(",");
					if (node.getChild(i).getChild(0).isToken(0)
							&& node.getChild(i).getChild(0).getToken(0).kind == LPAREN)
						pw.print("(new JointAccumulator((QObject *)self, " + n + "))");
					else
						pw.print("(new SimpleAccumulator((QObject *)self))");
					pw.print(generate(engine, node.getChild(i)));
					pw.print("->toPmfAndDelete()");
				}
				pw.print(")");
				return sw.toString();
			}
			}
		}

		case JJTPMFINITIALIZERGROUP: {
			int n = node.jjtGetNumChildren();
			for (int i = 0; i < n; i++)
				pw.print(generate(engine, node.getChild(i)));
			return sw.toString();
		}

		case JJTPMFINITIALIZERELEMENT:
			if (node.isToken(0) && node.getToken(0).kind == LPAREN) {
				int n = node.jjtGetNumChildren() - 1;
				if (n == 2)
					pw.print("->putDouble(");
				else
					pw.print("->put(");
				pw.print(generate(engine, node.getChild(n)));
				for (int i = 0; i < n; i++) {
					pw.print(",");
					pw.print(generate(engine, node.getChild(i)));
				}
				pw.print(")");
				return sw.toString();
			} else {
				pw.print("->putSingle(");
				pw.print(generate(engine, node.getChild(1)));
				pw.print(",");
				pw.print(generate(engine, node.getChild(0)));
				pw.print(")");
				return sw.toString();
			}

		case JJTCALL:
			pw.printf("_%s_%s(self", engine._engineName, node.getToken(0).image, engine._engineName);

			if (node.jjtGetNumChildren() > 0) {
				QNode listNode = node.getChild(0);
				int n = listNode.jjtGetNumChildren();
				for (int i = 0; i < n; i++) {
					pw.print(", ");
					pw.print(generate(engine, listNode.getChild(i)));
				}
			}

			pw.print(")");
			return sw.toString();

		case JJTEXPRESSIONLIST:
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				if (i > 0)
					pw.print(",");
				pw.print(generate(engine, node.getChild(i)));
			}
			return sw.toString();

		case JJTSUFFIXEDEXPRESSION: {
			QType[] types = ((SuffixedExpressionNode) node)._types;
			int n = node.jjtGetNumChildren();

			for (int i = n - 1; i > 0; i--) {
				switch (node.getChild(i).getId()) {
				case JJTLOOKUP:
					if (types[i - 1]._kind == PMF)
						pw.print("lookupProbability(");
					else if (types[i - 1]._kind == INTERFACEARRAY) {
						Signature signature = types[i - 1]._signature;
						int k = engine._signatureTable.getSignatures().indexOf(signature) + 1;
						pw.print("_" + engine._engineName + "_lookup" + k + "(");
					} else
						pw.print("lookup(");
					break;

				case JJTQUALIFIER: {
					QType sourceType = types[i - 1];
					Qualifier sourceQualifier = sourceType._qualifier;
					Qualifier destQualifier = types[i]._qualifier;
					Qualifier opQualifier = new Qualifier((QualifierNode) node.getChild(i));

					int k = -1;
					if (sourceQualifier._category == Qualifier.Category.COMPOUND) {
						k = sourceType.compoundLookup(opQualifier);
						sourceQualifier = new Qualifier(sourceQualifier._compoundRVNames.get(k));
					}

					int sourceRVCount = sourceQualifier._simpleRVNames.size();
					if (opQualifier._category == Qualifier.Category.CONDITIONAL)
						sourceRVCount -= opQualifier._conditions.size();
					int destRVCount = destQualifier._simpleRVNames.size();
					if (sourceRVCount > 1 && destRVCount == 1)
						pw.print("lookupMarginalPmf(");
					if (opQualifier._category == Qualifier.Category.CONDITIONAL)
						pw.print("lookupConditionalPmf(");

					if (k >= 0)
						pw.print("lookupCompoundPmf(");
					break;
				}
				case JJTCALL:
					break;
				default:
					assert (false);
				}
			}

			pw.print(generate(engine, node.getChild(0)));

			for (int i = 1; i < n; i++) {
				switch (node.getChild(i).getId()) {
				case JJTLOOKUP:
					pw.print(",");
					pw.print(generate(engine, node.getChild(i).getChild(0)));
					pw.print(")");
					break;
				case JJTQUALIFIER: {
					QType sourceType = types[i - 1];
					Qualifier sourceQualifier = sourceType._qualifier;
					Qualifier opQualifier = new Qualifier((QualifierNode) node.getChild(i));
					Qualifier destQualifier = types[i]._qualifier;

					if (sourceQualifier._category == Qualifier.Category.COMPOUND) {
						int k = sourceType.compoundLookup(opQualifier);
						sourceQualifier = new Qualifier(sourceQualifier._compoundRVNames.get(k));
						pw.print(",");
						pw.print(k);
						pw.print(")");
					}

					int sourceRVCount = sourceQualifier._simpleRVNames.size();
					if (opQualifier._category == Qualifier.Category.CONDITIONAL)
						sourceRVCount -= opQualifier._conditions.size();
					int destRVCount = destQualifier._simpleRVNames.size();

					if (opQualifier._category == Qualifier.Category.CONDITIONAL) {
						pw.print(",");
						pw.print(opQualifier._conditions.size());
						for (Qualifier.Condition condition : opQualifier._conditions) {
							pw.print(",");
							pw.print(generate(engine, condition._valueNode));
						}
						pw.print(")");
					}

					if (sourceRVCount > 1 && destRVCount == 1)
						pw.print(")");

					break;
				}
				case JJTCALL: {
					boolean needsComma;
					pw.print("(");
					switch (firstToken.kind) {
					case BERNOULLI:
					case BINOMIAL:
					case MULTINOMIAL:
					case HYPERGEOMETRIC:
					case MULTIVARIATEHYPERGEOMETRIC:
					case CREATEPMFFROMREALARRAY:
						pw.print("(QObject *) self");
						needsComma = true;
						break;
					case COMPUTELEFTTAIL:
					case COMPUTERIGHTTAIL:
					case ISSAMEPMFINSTANCE:
					case MIN:
					case MAX:
					case FLOOR:
					case CEILING:
					case EXP:
					case LOG:
					case SQRT:
					case POW:
					case RANDOMINT:
					case RANDOMREAL:
						needsComma = false;
						break;	
					default:
						pw.print("self");
						needsComma = true;
						break;
					}

					if (node.getChild(i).jjtGetNumChildren() > 0) {
						QNode argsNode = node.getChild(i).getChild(0);
						for (int j = 0; j < argsNode.jjtGetNumChildren(); j++) {
							if (needsComma || j > 0)
								pw.print(",");
							pw.print(generate(engine, argsNode.getChild(j)));
						}
					}
					pw.print(")");
					break;
				}
				default:
					assert (false);
				}
			}

			return sw.toString();
		}

		case JJTLOOKUP:
			return sw.toString();

		case JJTATTRIBUTEEXPRESSION:
			switch (node.getNode(2).getToken(0).kind) {
			case MIN_VALUE:
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->simple.offset");
				return sw.toString();
			case MAX_VALUE:
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->simple.offset+");
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->simple.length-1");
				return sw.toString();
			case LENGTH:
				pw.print(generate(engine, node.getChild(0)));
				pw.print("->length");
				return sw.toString();
			default:
				assert (false);
			}

		case JJTRETURNEXPRESSION:
			return generate(engine, node.getChild(0).getChild(0));

		case JJTQUALIFIER: {
			Qualifier q = new Qualifier((QualifierNode) node);
			return generateQualifier(q);
		}
		case JJTCREATEEXPRESSION:
			switch (node._type._kind) {
			case INTARRAY:
			case REALARRAY:
			case BOOLEANARRAY:
				return "create" + node._type._qName + "((QObject *)self, "
						+ generate(engine, node.getChild(0).getChild(0)) + ")";
			case PMFARRAY:
				return "create" + node._type._qName + "((QObject *)self, " + generateQualifier(node._type._qualifier)
						+ "," + generate(engine, node.getChild(0).getChild(0)) + ")";
			case INTERFACEARRAY: {
				Signature signature = node.getParent().getNode(0)._type._signature;
				int i = engine._signatureTable.getIndex(signature) + 1;
				return "createInterfaceArray((QObject *) self, (void *) &_" + engine._engineName
						+ "_defaultImplementation" + i + "," + generate(engine, node.getChild(0).getChild(0))
						+ ")";
			}
			case INTMATRIX:
			case REALMATRIX:
			case BOOLEANMATRIX:
				return "create" + node._type._qName + "((QObject *)self, "
						+ generate(engine, node.getChild(0).getChild(0)) + ","
						+ generate(engine, node.getChild(0).getChild(1)) + ")";
			case PMFMATRIX:
				return "create" + node._type._qName + "((QObject *)self, " + generateQualifier(node._type._qualifier)
						+ "," + generate(engine, node.getChild(0).getChild(0)) + ","
						+ generate(engine, node.getChild(0).getChild(1)) + ")";
			case INTERFACEMATRIX: {
				Signature signature = node.getParent().getNode(0)._type._signature;
				int i = engine._signatureTable.getIndex(signature) + 1;
				return "createInterfaceMatrix((QObject *) self, (void *) &_" + engine._engineName
						+ "_defaultImplementation" + i + "," + generate(engine, node.getChild(0).getChild(0)) + ","
						+ generate(engine, node.getChild(0).getChild(1)) + ")";
			}

			default:
				assert false;
				return null;
			}

		default:
			assert false;
			return null;
		}
	}

	/** Generate CPP code for a {@link compiler.Qualifier}. */
	static String generateQualifier(Qualifier q) {
		if (q._category == Qualifier.Category.SIMPLE) {
			if (q._simpleRVNames.size() == 1)
				return "createSimplePmfConfig((QObject *)self)";
			else
				return "createJointPmfConfig((QObject *)self, " + q._simpleRVNames.size() + ")";
		}

		StringBuilder b = new StringBuilder();
		b.append("createCompoundPmfConfig((QObject *) self, ");
		b.append(q._compoundRVNames.size());

		for (ArrayList<String> a : q._compoundRVNames) {
			b.append(",");
			b.append(a.size());
		}

		b.append(")");
		return b.toString();
	}
}
