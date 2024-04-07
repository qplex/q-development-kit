package generator;

import compiler.*;
import parser.QParserConstants;
import parser.QParserTreeConstants;
import parser.Token;
import tree.QNode;
import tree.TypeNode;

/** Generates CPP code for a function implementation. */
class FunctionGenerator implements QParserTreeConstants, QParserConstants {

	private enum SamplingType {
		SIMPLE, JOINT, COMPOUND, NONE
	};

	FunctionGenerator(Engine engine) {
		_engine = engine;
	}

	private SamplingType _samplingType;
	private QType _returnType;
	private IndentationManager _indentationManager;
	private Engine _engine;

	/** Generates CPP code for a function implementation. */
	void writeFunctionImplementation(Symbol symbol) {
		int samplingDepth = symbol._node.getChild(1).getNode(1)._samplingDepth;
		if (samplingDepth == 0)
			_samplingType = SamplingType.NONE;
		else {
			Qualifier returnTypeQualifier = symbol._signature._returnType._qualifier;
			if (returnTypeQualifier._category == Qualifier.Category.SIMPLE) {
				if (returnTypeQualifier._simpleRVNames.size() == 1)
					_samplingType = SamplingType.SIMPLE;
				else
					_samplingType = SamplingType.JOINT;
			} else
				_samplingType = SamplingType.COMPOUND;
		}

		_indentationManager = new IndentationManager();

		Signature signature = symbol._signature;
		_returnType = signature._returnType;

		TemplateExpander t = new TemplateExpander.FromLine( //
				"@RETURN-TYPE _@OBJ_@NAME(@REPEAT(@PARAM-TYPE @PARAM-NAME@,, @)) {" //
		) //
				.substitute("@OBJ", _engine._engineName) //
				.substitute("@RETURN-TYPE", symbol._signature._returnType._cName) //
				.substitute("@NAME", symbol._name); //

		t.repeat() //
				.substitute("@PARAM-TYPE", "_" + _engine._engineName + "_object *") //
				.substitute("@PARAM-NAME", "self");
		for (int i = 0; i < signature._parameterTypes.size(); i++) {
			t.repeat() //
					.substitute("@PARAM-TYPE", signature._parameterTypes.get(i)._cName) //
					.substitute("@PARAM-NAME", "_" + signature._parameterNames.get(i));
		}

		t.run();

		_indentationManager.pushBlock();

		if (Generator._logActivated) {
			String lineNumberString = Generator.bracketedPaddedLineNumber(symbol._node);

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine( //
					"Log(popLineNumber(), \"CALL @NAME\");" //
			) //
					.substitute("@NAME", symbol._name) //
					.run();

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine( //
					"Log(\"@LINE-NUMBERFUNCTION @NAME\");" //
			) //
					.substitute("@LINE-NUMBER", lineNumberString) //
					.substitute("@NAME", symbol._name) //
					.run();

			for (int i = 0; i < symbol._signature._parameterNames.size(); i++) {
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine( //
						"Log(\"@LINE-NUMBER@NAME <- \", _@NAME);" //
				) //
						.substitute("@LINE-NUMBER", lineNumberString) //
						.substitute("@NAME", symbol._signature._parameterNames.get(i)) //
						.run();
			}
		}

		switch (_samplingType) {
		case NONE:
			break;
		case SIMPLE:
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine("SamplingStack samplingStack(@DEPTH);") //
					.substitute("@DEPTH", samplingDepth) //
					.run();

			_indentationManager.writeIndent();
			Generator._cSourceWriter.println("SimpleAccumulator accumulator((QObject *) self);");
			break;

		case JOINT: {
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine("SamplingStack samplingStack(@DEPTH);") //
					.substitute("@DEPTH", samplingDepth) //
					.run();

			Qualifier returnTypeQualifier = symbol._signature._returnType._qualifier;
			int returnCount = returnTypeQualifier._simpleRVNames.size();
			_indentationManager.writeIndent();
			Generator._cSourceWriter.printf("JointAccumulator accumulator((QObject *) self, %d);\n", returnCount);
			break;
		}

		case COMPOUND: {
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine("SamplingStack samplingStack(@DEPTH);") //
					.substitute("@DEPTH", samplingDepth) //
					.run();

			Qualifier returnTypeQualifier = symbol._signature._returnType._qualifier;

			_indentationManager.writeIndent();
			Generator._cSourceWriter.print("PmfConfig *returnPmfConfig = ");
			Generator._cSourceWriter.print(ExpressionGenerator.generateQualifier(returnTypeQualifier));
			Generator._cSourceWriter.println(";");

			_indentationManager.writeIndent();
			Generator._cSourceWriter.println("CompoundAccumulator accumulator((QObject *) self, returnPmfConfig);");
			break;
		}
		}

		writeBlock(symbol._node.getChild(1).getChild(1));

		if (Generator._logActivated && !symbol._node.getChild(1).getChild(1)._isTerminal) {
			_indentationManager.writeIndent();
			QNode blockNode = symbol._node.getChild(1).getChild(1);
			Token endLBraceToken = blockNode.getToken(blockNode.getTokenAndNodeCount() - 1);
			new TemplateExpander.FromLine("Log(\"@PADDED-LINE-NUMBERreturn\");") //
					.substitute("@PADDED-LINE-NUMBER", Generator.bracketedPaddedLineNumber(endLBraceToken)) //
					.run();
		}

		_indentationManager.pop();
		Generator._cSourceWriter.println("}");
		Generator._cSourceWriter.println();

	}

	private void writeBlock(QNode blockNode) {
		for (int i = 0; i < blockNode.jjtGetNumChildren(); i++) {
			QNode statementNode = blockNode.getChild(i);

			if (Generator._logActivated)
				pushCalls(statementNode, Generator.bracketedPaddedLineNumber(statementNode));

			switch (statementNode.getId()) {

			case JJTRETURNSTATEMENT:
				generateReturnStatement(statementNode);
				break;

			case JJTASSIGNMENTSTATEMENT:
				generateAssignmentStatement(statementNode);
				break;

			case JJTFAILSTATEMENT:
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine("throw CException(@MSG);") //
						.substitute("@MSG", statementNode.getToken(1).image) //
						.run();
				break;

			case JJTIFSTATEMENT:
				_indentationManager.writeIndent();
				writeIfStatement(statementNode, 0);

				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("}");
				break;

			case JJTFORSTATEMENT: {
				String indexVariableName = statementNode.getChild(0).getToken(1).image;
				int id = statementNode.jjtGetFirstToken().beginLine;

				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "Int min@ID = @VALUE;") //
						.substitute("@ID", id) //
						.substitute("@VALUE", statementNode.getChild(0).getChild(0)) //
						.run();

				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "Int max@ID = @VALUE;") //
						.substitute("@ID", id) //
						.substitute("@VALUE", statementNode.getChild(0).getChild(1)) //
						.run();

				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine,  //
						"for (Int _@INDEX-VAR=min@ID; _@INDEX-VAR<=max@ID; _@INDEX-VAR++) {" //
				) //
						.substitute("@ID", id) //
						.substitute("@INDEX-VAR", indexVariableName) //
						.run();

				_indentationManager.pushBlock();
				writeBlock(statementNode.getChild(1));
				_indentationManager.pop();

				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("}");
				break;
			}

			case JJTWHILESTATEMENT:
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine( _engine, //
						"while (@CONDITION) {" //
				) //
						.substitute("@CONDITION", statementNode.getChild(0)) //
						.run();
				_indentationManager.pushBlock();
				writeBlock(statementNode.getChild(1));
				_indentationManager.pop();

				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("}");
				break;

			case JJTSAMPLINGSTATEMENT: {
				QNode sampleVariablesNode = statementNode.getChild(0);
				int sampleVariablesCount = (sampleVariablesNode.getTokenAndNodeCount() + 1) / 2;
				QNode distributionNode = statementNode.getNode(2);
				Qualifier distributionQualifier = distributionNode._type._qualifier;
				int distributionDepth = distributionQualifier._simpleRVNames.size();

				String sampleVariableName = null;
				String distributionVariableName = null;

				for (int j = 0; j < sampleVariablesCount; j++) {
					String oldSampleVariableName = sampleVariableName;
					sampleVariableName = statementNode.getNode(0).getToken(2 * j).image;
					String oldDistributionVariableName = distributionVariableName;
					distributionVariableName = "dist_" + sampleVariableName;
					boolean isJoint = j < distributionDepth - 1;

					_indentationManager.writeIndent();
					if (j == 0) {
						Generator._cSourceWriter.printf("Pmf *%s = ", distributionVariableName);
						Generator._cSourceWriter.print(ExpressionGenerator.generate(_engine, distributionNode));
						Generator._cSourceWriter.println(";");
					} else {
						Generator._cSourceWriter.printf("Pmf *%s = lookupConditionalPmf(%s, 1, _%s);\n",
								distributionVariableName, oldDistributionVariableName, oldSampleVariableName);
					}

					writeSamplingLoop(statementNode, sampleVariableName, distributionVariableName, isJoint);
				}

				break;
			}

			case JJTDECLARATIONSTATEMENT: {
				QType type = QType.getType((TypeNode) (statementNode.getNode(0)));
				String variableName = statementNode.getToken(1).image;
				boolean isAssignment = statementNode.getToken(2).kind == ASSIGN;

				_indentationManager.writeIndent();
				Generator._cSourceWriter.print(type._cName);
				Generator._cSourceWriter.print(" _");
				Generator._cSourceWriter.print(variableName);
				if (isAssignment) {
					Generator._cSourceWriter.print(" = ");
					if (Generator._logActivated) {
						Generator._cSourceWriter.print("Log(\"");
						Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(statementNode));
						Generator._cSourceWriter.print(variableName);
						Generator._cSourceWriter.print(" <- \", ");
					}
					Generator._cSourceWriter
							.print(ExpressionGenerator.generate(_engine, statementNode.getNode(3)));
					if (Generator._logActivated) {
						Generator._cSourceWriter.print(")");
					}
				} else if (type.isScalar()) {
					Generator._cSourceWriter.print(" = ");
					switch (type._kind) {
					case INT:
						Generator._cSourceWriter.print("0");
						break;
					case REAL:
						Generator._cSourceWriter.print("0.0");
						break;
					case BOOLEAN:
						Generator._cSourceWriter.print("false");
						break;
					default:
						assert (false);
					}
				} else {
					Generator._cSourceWriter.print(" = default");
					Generator._cSourceWriter.print(type._xName);
					switch (type._kind) {
					case PMF:
					case PMFARRAY:
					case PMFMATRIX:
						Generator._cSourceWriter
								.print("((QObject *)self, " + ExpressionGenerator.generateQualifier(type._qualifier) + ")");
						break;
					default:
						Generator._cSourceWriter.print("((QObject *)self)");
						break;
					}
				}

				Generator._cSourceWriter.println(";");
				break;
			}

			case JJTSKIPSTATEMENT:
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "@LOG(\"@PADDED-LINE-NUMBERskip\"@);") //
						.substitute("@PADDED-LINE-NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
						.run();

				if (_indentationManager.getSampleCount() <= 0) {
					_indentationManager.writeIndent();
					Generator._cSourceWriter.println("Pmf *retPmf = accumulator.toPmf();");

					_indentationManager.writeIndent();
					Generator._cSourceWriter.print("return ");
					if (Generator._logActivated) {
						Generator._cSourceWriter.print("Log(\"");
						Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(statementNode));
						Generator._cSourceWriter.print("return \", ");
					}

					Generator._cSourceWriter.print("retPmf");

					if (Generator._logActivated)
						Generator._cSourceWriter.print(")");
					Generator._cSourceWriter.println(";");

				} else {
					_indentationManager.writeIndent();
					Generator._cSourceWriter.println("continue;");
				}
				break;

			default:
				assert (false);
			}
		}

		boolean b = false;
		while (_indentationManager.peekIsSample()) {
			b = true;
			_indentationManager.pop();
			_indentationManager.writeIndent();
			Generator._cSourceWriter.println("}");
		}

		if (b) {
			if (_indentationManager.getSampleCount() > 0) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("continue;");
			} else {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("Pmf *retPmf = accumulator.toPmf();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter.print("return ");
				if (Generator._logActivated) {
					Generator._cSourceWriter.print("Log(\"");
					Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(blockNode.jjtGetLastToken()));
					Generator._cSourceWriter.print("return \", ");
				}

				Generator._cSourceWriter.print("retPmf");

				if (Generator._logActivated)
					Generator._cSourceWriter.print(")");
				Generator._cSourceWriter.println(";");
			}
		}
	}

	private void pushCalls(QNode node, String statementLineNumber) {
		if (node.getId() == JJTCALL) {
			_indentationManager.writeIndent();
			Generator._cSourceWriter.println("pushLineNumber(\"" + statementLineNumber + "\");");
		}

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			QNode child = node.getChild(i);
			pushCalls(child, statementLineNumber);
		}
	}

	void writePythonWrapper(Symbol symbol, String templateFilename) {
		int n = symbol._signature._parameterNames.size();

		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			QType qtype = symbol._signature._parameterTypes.get(i);
			String prefix = "";
			String suffix = "";

			switch(qtype._kind) { 
			case PMF:
			case PMFARRAY:
			case PMFMATRIX:
				Qualifier qualifier = qtype._qualifier;

				String category, confirmParams;
				if (qualifier._category == Qualifier.Category.SIMPLE) {
					if (qualifier._simpleRVNames.size() == 1) {
						category = "Simple";
						confirmParams = "";
					} else {
						category = "Joint";
						confirmParams = "," + qualifier._simpleRVNames.size();
					}
				} else {
					category = "Compound";
					confirmParams = "," + qualifier._compoundRVNames.size();
					for (int j=0; j<qualifier._compoundRVNames.size(); j++)
						confirmParams += "," + qualifier._compoundRVNames.get(j).size();
				}
				
				String qname = qtype._qName;

				prefix = "confirm" + category + qname + "(";
			    suffix = confirmParams + ")";
			}
			
			b.append(", ");
			b.append(prefix);
			b.append("arg");
			b.append(String.valueOf(i));
			b.append(suffix);
		}
		String allArgs = b.toString();

		TemplateExpander t = new TemplateExpander.FromFile(_engine, templateFilename);

		t //
				.substitute("@OBJ", _engine._engineName) //
				.substitute("@NAME", symbol._name) //
				.substitute("@NUM_PARAMS", String.valueOf(n)) //
				.substitute("@RETURN_TYPE", symbol._signature._returnType._xName) //
				.substitute("@ALL_ARGS", allArgs);

		for (int i = 0; i < n; i++) {
			QType argType = symbol._signature._parameterTypes.get(i);
			String index = String.valueOf(i);
			t. //
					repeat() //
					.substitute("@CTYPE", argType._cName) //
					.substitute("@XTYPE", argType._xName) //
					.substitute("@INDEX", index);
		}

		t.run();
	}

	void writeSamplingLoop(QNode statementNode, String sampleVariableName, String distributionVariableName,
			boolean isJoint) {
		String offsetPrefix = isJoint ? "joint.marginal->" : "";

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("Int min_%s = %s->%ssimple.offset;", sampleVariableName,
				distributionVariableName, offsetPrefix);
		Generator._cSourceWriter.println();

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("Int max_%s = %s->%ssimple.offset+%s->%ssimple.length - 1;", sampleVariableName,
				distributionVariableName, offsetPrefix, distributionVariableName, offsetPrefix);
		Generator._cSourceWriter.println();

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("for (Int _%s = min_%s; _%s <= max_%s; _%s++) {", sampleVariableName,
				sampleVariableName, sampleVariableName, sampleVariableName, sampleVariableName);
		Generator._cSourceWriter.println();

		_indentationManager.pushSample();

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("Real prob_%s = %s->%ssimple.elements[_%s-%s->%ssimple.offset];",
				sampleVariableName, distributionVariableName, offsetPrefix, sampleVariableName,
				distributionVariableName, offsetPrefix);

		Generator._cSourceWriter.println();

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("if (prob_%s <= 0)", sampleVariableName);
		Generator._cSourceWriter.println();

		_indentationManager.writeIndent();
		Generator._cSourceWriter.printf("    continue;");
		Generator._cSourceWriter.println();

		_indentationManager.writeIndent();
		if (_indentationManager.getSampleCount() == 1)
			Generator._cSourceWriter.printf("samplingStack.set(%d, prob_%s);", _indentationManager.getSampleCount() - 1,
					sampleVariableName);
		else
			Generator._cSourceWriter.printf("samplingStack.branch(%d, prob_%s);",
					_indentationManager.getSampleCount() - 1, sampleVariableName);
		Generator._cSourceWriter.println();

		if (Generator._logActivated) {
			_indentationManager.writeIndent();
			String lineNumber = Generator.bracketedPaddedLineNumber(statementNode);
			Generator._cSourceWriter.printf("Log(\"%s%s <~ \", _%s, prob_%s);", lineNumber, sampleVariableName,
					sampleVariableName, sampleVariableName);
			Generator._cSourceWriter.println();
		}
	}

	private void writeIfStatement(QNode statementNode, int k) {
		new TemplateExpander.FromLine(_engine, //
				"if (@CONDITION) {" //
		) //
				.substitute("@CONDITION", statementNode.getChild(k)) //
				.run();
		_indentationManager.pushBlock();
		writeBlock(statementNode.getChild(k + 1));
		_indentationManager.pop();

		if (statementNode.jjtGetNumChildren() > k + 2) {
			QNode elseNode = statementNode.getChild(k + 2);
			_indentationManager.writeIndent();

			switch (elseNode.getId()) {
			case JJTBLOCK:
				Generator._cSourceWriter.println("} else {");

				_indentationManager.pushBlock();
				writeBlock(elseNode);
				_indentationManager.pop();
				break;

			case JJTIFSTATEMENT:
				Generator._cSourceWriter.print("} else ");
				writeIfStatement(statementNode.getChild(k + 2), 0);
			}
		}
	}

	private void generateReturnStatement(QNode statementNode) {
		switch (_samplingType) {
		case NONE:
			if (_returnType != QType.VOID) {
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, //
						"@TYPE returnValue = @VALUE;" //
				) //
						.substitute("@TYPE", _returnType._cName) //
						.substitute("@VALUE", statementNode.getChild(0)) //
						.run();
				if (Generator._logActivated) {
					_indentationManager.writeIndent();
					new TemplateExpander.FromLine( _engine, //
							"Log(\"@PADDED-LINE-NUMBERreturn \", returnValue);" //
					) //
							.substitute("@PADDED-LINE-NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
							.run();
				}

				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("return returnValue;");
				break;
			} else {
				if (Generator._logActivated) {
					_indentationManager.writeIndent();
					new TemplateExpander.FromLine(_engine, "Log(\"@PADDED-LINE-NUMBERreturn\");") //
							.substitute("@PADDED-LINE-NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
							.run();
				}

				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("return;");
				break;
			}
		case SIMPLE:
			_indentationManager.writeIndent();
			Generator._cSourceWriter.print("Int sample = ");
			Generator._cSourceWriter.print(
					ExpressionGenerator.generate(_engine, statementNode.getChild(0).getChild(0).getChild(0)));
			Generator._cSourceWriter.println(";");

			_indentationManager.writeIndent();
			if (_indentationManager.getSampleCount() > 0)
				Generator._cSourceWriter.printf("Real prob = samplingStack.branchProbability(%d);\n",
						_indentationManager.getSampleCount() - 1);
			else
				Generator._cSourceWriter.println("Real prob = 1;");

			_indentationManager.writeIndent();
			Generator._cSourceWriter.println("accumulator.putSingle(prob, sample);");

			if (Generator._logActivated) {
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine( _engine, //
						"Log(\"@PADDED-LINE-NUMBERpmf <+ \", sample, prob);" //
				) //
						.substitute("@PADDED-LINE-NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
						.run();
			}

			if (_indentationManager.getSampleCount() <= 0) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("Pmf *retPmf = accumulator.toPmf();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter.print("return ");
				if (Generator._logActivated) {
					Generator._cSourceWriter.print("Log(\"");
					Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(statementNode));
					Generator._cSourceWriter.print("return \", ");
				}

				Generator._cSourceWriter.print("retPmf");

				if (Generator._logActivated)
					Generator._cSourceWriter.print(")");
				Generator._cSourceWriter.println(";");

			} else {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("continue;");
			}
			break;

		case JOINT: {
			int returnCount = statementNode.getChild(0).jjtGetNumChildren();
			for (int j = 0; j < returnCount; j++) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.printf("Int sample%d = ", j + 1);
				Generator._cSourceWriter.print(
						ExpressionGenerator.generate(_engine, statementNode.getChild(0).getChild(j).getChild(0)));
				Generator._cSourceWriter.println(";");
			}

			_indentationManager.writeIndent();
			if (_indentationManager.getSampleCount() > 0)
				Generator._cSourceWriter.printf("Real prob = samplingStack.branchProbability(%d);\n",
						_indentationManager.getSampleCount() - 1);
			else
				Generator._cSourceWriter.println("Real prob = 1;");

			_indentationManager.writeIndent();
			if (returnCount == 2)
				Generator._cSourceWriter.print("accumulator.putDouble(prob");
			else
				Generator._cSourceWriter.print("accumulator.put(prob");
			for (int j = 1; j <= returnCount; j++)
				Generator._cSourceWriter.printf(",sample%d", j);
			Generator._cSourceWriter.println(");");

			if (Generator._logActivated) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("initLog();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter
						.print("*logFile << \"" + Generator.bracketedPaddedLineNumber(statementNode) + "pmf <+ (\"");
				for (int j = 1; j <= returnCount; j++) {
					if (j > 1)
						Generator._cSourceWriter.print(" << \",\"");
					Generator._cSourceWriter.printf(" << sample%d", j);
				}
				Generator._cSourceWriter.println(" << \"):\" << prob << std::endl;");
			}

			if (_indentationManager.getSampleCount() <= 0) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("Pmf *retPmf = accumulator.toPmf();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter.print("return ");
				if (Generator._logActivated) {
					Generator._cSourceWriter.print("Log(\"");
					Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(statementNode));
					Generator._cSourceWriter.print("return \", ");
				}

				Generator._cSourceWriter.print("retPmf");

				if (Generator._logActivated)
					Generator._cSourceWriter.print(")");
				Generator._cSourceWriter.println(";");

			} else {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("continue;");
			}
			break;
		}

		case COMPOUND: {
			int returnCount = 0;
			for (int j = 0; j < statementNode.getChild(0).jjtGetNumChildren(); j++) {
				for (int k = 0; k < statementNode.getChild(0).getChild(j).jjtGetNumChildren(); k++) {
					_indentationManager.writeIndent();
					Generator._cSourceWriter.printf("Int sample%d = ", ++returnCount);
					Generator._cSourceWriter.print(ExpressionGenerator.generate(_engine,
							statementNode.getChild(0).getChild(j).getChild(k)));
					Generator._cSourceWriter.println(";");
				}
			}

			_indentationManager.writeIndent();
			if (_indentationManager.getSampleCount() > 0)
				Generator._cSourceWriter.printf("Real prob = samplingStack.branchProbability(%d);\n",
						_indentationManager.getSampleCount() - 1);
			else
				Generator._cSourceWriter.println("Real prob = 1;");

			_indentationManager.writeIndent();
			Generator._cSourceWriter.print("accumulator.put(prob");
			for (int j = 1; j <= returnCount; j++)
				Generator._cSourceWriter.printf(",sample%d", j);
			Generator._cSourceWriter.println(");");

			if (Generator._logActivated) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("initLog();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter
						.print("*logFile << \"" + Generator.bracketedPaddedLineNumber(statementNode) + "pmf <+ (\"");

				returnCount = 0;
				int jMax = statementNode.getChild(0).jjtGetNumChildren();
				for (int j = 0; j < jMax; j++) {
					if (j > 0)
						Generator._cSourceWriter.print(" << \"),(\"");
					int kMax = statementNode.getChild(0).getChild(j).jjtGetNumChildren();
					for (int k = 0; k < kMax; k++) {
						if (k > 0)
							Generator._cSourceWriter.print(" << \",\"");
						Generator._cSourceWriter.printf(" << sample%d", ++returnCount);
					}
				}

				Generator._cSourceWriter.println(" << \"):\" << prob << std::endl;");
			}

			if (_indentationManager.getSampleCount() <= 0) {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("Pmf *retPmf = accumulator.toPmf();");

				_indentationManager.writeIndent();
				Generator._cSourceWriter.print("return ");
				if (Generator._logActivated) {
					Generator._cSourceWriter.print("Log(\"");
					Generator._cSourceWriter.print(Generator.bracketedPaddedLineNumber(statementNode));
					Generator._cSourceWriter.print("return \", ");
				}

				Generator._cSourceWriter.print("retPmf");

				if (Generator._logActivated)
					Generator._cSourceWriter.print(")");
				Generator._cSourceWriter.println(";");
			} else {
				_indentationManager.writeIndent();
				Generator._cSourceWriter.println("continue;");
			}

			break;
		}
		}
	}

	private void generateAssignmentStatement(QNode statementNode) {
		if (statementNode.jjtGetNumChildren() == 1) {
			// Void expression with no assignment
			_indentationManager.writeIndent();
			Generator._cSourceWriter
					.println(ExpressionGenerator.generate(_engine, statementNode.getChild(0)) + ";");
			return;
		}

		if (statementNode.getChild(1)._type._kind == QType.FUNCTION_KIND) {
			if (statementNode.getChild(0).jjtGetNumChildren() == 0) {
				// Assigning a function to an interface variable
				String targetVariableName = statementNode.getChild(0).getToken(0).image;
				Signature signature = _engine._symbolTable.get(targetVariableName)._signature;
				int index = _engine._signatureTable.getSignatures().indexOf(signature) + 1;

				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "self->_@LHS = (_@OBJ_Interface@ID *) &_@OBJ_@RHS;") //
						.substitute("@LHS", targetVariableName) //
						.substitute("@OBJ", _engine._engineName) //
						.substitute("@ID", String.valueOf(index)) //
						.substitute("@RHS", statementNode.getChild(1).getToken(0).image) //
						.run();
				if (Generator._logActivated) {
					_indentationManager.writeIndent();
					new TemplateExpander.FromLine(_engine, "Log(\"@PADDED_LINE_NUMBER@LHS <- @RHS\");") //
							.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode))
							.substitute("@LHS", targetVariableName) //
							.substitute("@RHS", statementNode.getChild(1).getToken(0).image) //
							.run();
				}
				return;
			}

			if (statementNode.getChild(0).jjtGetNumChildren() == 2) {
				// Assigning a function to an InterfaceArray element
				String lhsArrayName = statementNode.getChild(0).getChild(0).getToken(0).image;
				Symbol symbol = _engine._symbolTable.get(lhsArrayName);
				if (symbol != null && symbol._level == 0)
					lhsArrayName = "self->_" + lhsArrayName;
				else
					lhsArrayName = "_" + lhsArrayName;
				String indexValue = ExpressionGenerator.generate(_engine,
						statementNode.getChild(0).getChild(1).getChild(0));
				String indexVariable = "index" + statementNode.getToken(1).beginLine;
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE = @INDEX_VALUE;") //
						.substitute("@INDEX_VARIABLE", indexVariable) //
						.substitute("@INDEX_VALUE", indexValue) //
						.run();
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, "lookup(@LHS, @INDEX) = (void *) &@RHS;") //
						.substitute("@LHS", lhsArrayName) //
						.substitute("@INDEX", indexVariable)//
						.substitute("@RHS", statementNode.getChild(1)) //
						.run();
				if (Generator._logActivated) {
					_indentationManager.writeIndent();
					new TemplateExpander.FromLine(_engine, "Log(\"@PADDED_LINE_NUMBER@LHS[\", @INDEX, \"] <- @RHS\");") //
							.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode))
							.substitute("@LHS", lhsArrayName) //
							.substitute("@INDEX", indexVariable)//
							.substitute("@RHS", statementNode.getChild(1).getToken(0).image) //
							.run();
				}
				return;
			}

			// Assigning a function to an InterfaceMatrix element
			String lhsMatrixName = statementNode.getChild(0).getChild(0).getToken(0).image;
			Symbol symbol = _engine._symbolTable.get(lhsMatrixName);
			if (symbol != null && symbol._level == 0)
				lhsMatrixName = "self->_" + lhsMatrixName;
			else
				lhsMatrixName = "_" + lhsMatrixName;
			String indexValueA = ExpressionGenerator.generate(_engine,
					statementNode.getChild(0).getChild(1).getChild(0));
			String indexVariableA = "index" + statementNode.getToken(1).beginLine + "A";
			String indexValueB = ExpressionGenerator.generate(_engine,
					statementNode.getChild(0).getChild(2).getChild(0));
			String indexVariableB = "index" + statementNode.getToken(1).beginLine + "B";

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE = @INDEX_VALUE;") //
					.substitute("@INDEX_VARIABLE", indexVariableA) //
					.substitute("@INDEX_VALUE", indexValueA) //
					.run();
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE = @INDEX_VALUE;") //
					.substitute("@INDEX_VARIABLE", indexVariableB) //
					.substitute("@INDEX_VALUE", indexValueB) //
					.run();
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "lookup(lookup(@LHS, @INDEXA), @INDEXB) = (void *) &@RHS;") //
					.substitute("@LHS", lhsMatrixName) //
					.substitute("@INDEXA", indexVariableA)//
					.substitute("@INDEXB", indexVariableB)//
					.substitute("@RHS", statementNode.getChild(1)) //
					.run();
			if (Generator._logActivated) {
				_indentationManager.writeIndent();
				new TemplateExpander.FromLine(_engine, 
						"Log(\"@PADDED_LINE_NUMBER@LHS[\", @INDEXA, \"][\", @INDEXB, \"] <- @RHS\");") //
								.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode))
								.substitute("@LHS", lhsMatrixName) //
								.substitute("@INDEXA", indexVariableA)//
								.substitute("@INDEXB", indexVariableB)//
								.substitute("@RHS", statementNode.getChild(1).getToken(0).image) //
								.run();
			}
			return;
		}

		if (statementNode.getChild(1).getId() == JJTSUFFIXEDEXPRESSION
				&& statementNode.getChild(1).getChild(0).getToken(0).kind == QParserConstants.CREATEPMFARRAY) {

			int depth = statementNode.getChild(0)._type._qualifier._simpleRVNames.size();
			String size = ExpressionGenerator.generate(_engine,
					statementNode.getChild(1).getChild(1).getChild(0).getChild(0));
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, 
					"@LHS = @LOG(\"@PADDED_LINE_NUMBER@LHS <- \"@, createPmfArray((QObject *)self, @SIZE, createJointPmfConfig(@DEPTH))@);") //
							.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
							.substitute("@LHS", statementNode.getChild(0)) //
							.substitute("@DEPTH", depth) //
							.substitute("@SIZE", size) //
							.run();
			return;
		}

		if (statementNode.getChild(1).getId() == JJTSUFFIXEDEXPRESSION
				&& statementNode.getChild(1).getChild(0).getToken(0).kind == QParserConstants.CREATEPMFMATRIX) {

			int depth = statementNode.getChild(0)._type._qualifier._simpleRVNames.size();
			String sizeA = ExpressionGenerator.generate(_engine,
					statementNode.getChild(1).getChild(1).getChild(0).getChild(0));
			String sizeB = ExpressionGenerator.generate(_engine,
					statementNode.getChild(1).getChild(1).getChild(0).getChild(1));
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, 
					"@LHS = @LOG(\"@PADDED_LINE_NUMBER@LHS <- \"@, createPmfArray((QObject *)self, @SIZEA, @SIZEB, createJointPmfConfig(@DEPTH))@);") //
							.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
							.substitute("@LHS", statementNode.getChild(0)) //
							.substitute("@DEPTH", depth) //
							.substitute("@SIZEA", sizeA) //
							.substitute("@SIZEB", sizeB) //
							.run();
			return;
		}

		if (statementNode.getChild(1).getId() == JJTSUFFIXEDEXPRESSION
				&& statementNode.getChild(1).getChild(0).getToken(0).kind == QParserConstants.CREATEPMFMATRIX) {
			return;
		}

		if (statementNode.getChild(0).jjtGetNumChildren() == 0) {
			// variable = expression
			String targetVariableName = statementNode.getChild(0).getToken(0).image;
			Symbol symbol = _engine._symbolTable.get(targetVariableName);
			String lhs;
			if (symbol != null && symbol._level == 0)
				lhs = "self->_" + targetVariableName;
			else
				lhs = "_" + targetVariableName;

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "@LHS = @LOG(\"@PADDED_LINE_NUMBER@LHS <- \"@,@RHS@);") //
					.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode))
					.substitute("@LHS", lhs) //
					.substitute("@RHS", statementNode.getChild(1)) //
					.run();
			return;
		}

		int suffixCount = statementNode.getChild(0).jjtGetNumChildren();
		if (statementNode.getChild(0).getChild(suffixCount - 2).getId() != JJTLOOKUP) {
			// array-expression[index] = expression
			String lhsIndexBase;
			if (statementNode.getChild(0).jjtGetNumChildren() == 2)
				lhsIndexBase = statementNode.getChild(0).getChild(0).getToken(0).image;
			else
				lhsIndexBase = "...";
			String indexVariableName = "index" + statementNode.getToken(1).beginLine;
			int n = statementNode.getChild(0).jjtGetNumChildren();
			String indexVariableValue = ExpressionGenerator.generate(_engine,
					statementNode.getChild(0).getChild(n - 1).getChild(0));
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE_NAME = @RHS;") //
					.substitute("@INDEX_VARIABLE_NAME", indexVariableName) //
					.substitute("@RHS", indexVariableValue) //
					.run();

			String lhs = ExpressionGenerator.generate(_engine, statementNode.getChild(0));
			assert (lhs.endsWith("," + indexVariableValue + ")"));
			lhs = lhs.substring(0, lhs.length() - indexVariableValue.length() - 2);
			lhs = lhs + "," + indexVariableName + ")";

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "@LHS = @LOG(\"@PADDED_LINE_NUMBER@BASE[\",@INDEX,\"] <- \"@,@RHS@);") //
					.substitute("@LHS", lhs) //
					.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
					.substitute("@BASE", lhsIndexBase) //
					.substitute("@INDEX", indexVariableName) //
					.substitute("@RHS", statementNode.getChild(1)) //
					.run();
			return;
		}

		{
			// matrix-expression[indexA][indexB] = expression
			String lhsIndexBase;
			if (statementNode.getChild(0).jjtGetNumChildren() == 3)
				lhsIndexBase = statementNode.getChild(0).getChild(0).getToken(0).image;
			else
				lhsIndexBase = "...";
			String indexVariableA = "index" + statementNode.getToken(1).beginLine + "A";
			String indexVariableB = "index" + statementNode.getToken(1).beginLine + "B";
			int n = statementNode.getChild(0).jjtGetNumChildren();
			String indexVariableValueA = ExpressionGenerator.generate(_engine,
					statementNode.getChild(0).getChild(n - 2).getChild(0));
			String indexVariableValueB = ExpressionGenerator.generate(_engine,
					statementNode.getChild(0).getChild(n - 1).getChild(0));

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE_NAME = @RHS;") //
					.substitute("@INDEX_VARIABLE_NAME", indexVariableA) //
					.substitute("@RHS", indexVariableValueA) //
					.run();
			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, "int @INDEX_VARIABLE_NAME = @RHS;") //
					.substitute("@INDEX_VARIABLE_NAME", indexVariableB) //
					.substitute("@RHS", indexVariableValueB) //
					.run();

			String lhs = ExpressionGenerator.generate(_engine, statementNode.getChild(0));
			String tag = "," + indexVariableValueA + ")" + "," + indexVariableValueB + ")";
			assert (lhs.endsWith(tag));
			lhs = lhs.substring(0, lhs.length() - tag.length());
			lhs = lhs + "," + indexVariableA + ")" + "," + indexVariableB + ")";

			_indentationManager.writeIndent();
			new TemplateExpander.FromLine(_engine, 
					"@LHS = @LOG(\"@PADDED_LINE_NUMBER@BASE[\",@INDEXA,\"][\",@INDEXB,\"] <- \"@,@RHS@);") //
							.substitute("@LHS", lhs) //
							.substitute("@PADDED_LINE_NUMBER", Generator.bracketedPaddedLineNumber(statementNode)) //
							.substitute("@BASE", lhsIndexBase) //
							.substitute("@INDEXA", indexVariableA) //
							.substitute("@INDEXB", indexVariableB) //
							.substitute("@RHS", statementNode.getChild(1)) //
							.run();
		}
	}
}