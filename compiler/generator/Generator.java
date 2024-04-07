package generator;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import compiler.*;
import parser.QParserConstants;
import parser.Token;
import tree.QNode;

import java.time.LocalDateTime;

/** Generates CPP code. */
public class Generator implements QParserConstants {
	static boolean _logActivated;
	static String _moduleName;
	static ArrayList<Engine> _allEngines;

	static PrintWriter _cSourceWriter;

	static String bracketedPaddedLineNumber(QNode node) {
		return bracketedPaddedLineNumber(node.jjtGetFirstToken());
	}

	static String bracketedPaddedLineNumber(Token token) {
		int n = token.beginLine;
		String s = String.valueOf(n);
		s = "[" + n + "]";
		while (s.length() < 6)
			s += " ";
		return s;
	}

	public static void run(boolean isLogActivated, String modulename, ArrayList<Engine> allEngines) {
		_logActivated = isLogActivated;
		_moduleName = modulename;
		_allEngines = allEngines;

		String destFilename = _moduleName + ".cpp";

		try {
			_cSourceWriter = new PrintWriter(new FileWriter(destFilename));

			// Header with date and time
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MM/dd/yyyy HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String dateTimeString = dateTimeFormatter.format(now);
			_cSourceWriter.println("// Generated by QCompiler on " + dateTimeString);
			_cSourceWriter.println();

			// Boilerplate
			new TemplateExpander.FromFile("Boilerplate.txt") //
					.run();

			// Logging
			if (_logActivated) {
				String logFilename = _moduleName + ".log";
				File logFile = new File(logFilename);
				logFilename = logFile.getCanonicalPath();

				new TemplateExpander.FromFile("Logging.txt") //
						.substitute("@FILENAME", logFilename.replace("\\", "\\\\")) //
						.run();
			}

			// QObjects
			for (int i = 0; i < allEngines.size(); i++)
				generateQObject(modulename, allEngines.get(i));

			// Module
			generateModule(modulename, allEngines);

			_cSourceWriter.close();
		} catch (IOException x) {
			x.printStackTrace();
			System.exit(1);
		}
	}

	private static void generateQObject(String modulename, Engine engine) {
		String engineName = engine._engineName;
		FunctionGenerator functionGenerator = new FunctionGenerator(engine);

		_cSourceWriter.println("// " + engineName);
		_cSourceWriter.println();

		// Tokens
		for (String rawTokenName : engine._tokenTable.getTokenStrings()) {
			String tokenName = rawTokenName.substring(1, rawTokenName.length() - 1);
			String cTokenName = "_" + engine._engineName + "_token_" + tokenName;
			int tokenValue = engine._tokenTable.getValueOfToken(rawTokenName);
			_cSourceWriter.println("#define " + cTokenName + " " + tokenValue);
		}
		if (engine._tokenTable.getTokenStrings().size() > 0)
			_cSourceWriter.println();

		for (String rawTokenName : engine._tokenTable.getPublicTokenStrings()) {
			String name = rawTokenName.substring(1, rawTokenName.length() - 1);
			(new TemplateExpander.FromFile("PyToken.txt")) //
					.substitute("@NAME", name) //
					.substitute("@ENGINE", engine._engineName) //
					.run();
		}

		// Interface signatures (part 1)
		generateSignatureDeclarations(engineName, engine);

		// _XXX_object
		(new TemplateExpander.FromFile("PyObject.txt")) //
				.substitute("@ENGINE", engineName) //
				.run();

		for (String name : engine._symbolTable.globalNames()) {
			Symbol symbol = engine._symbolTable.get(name);
			QType type = symbol._type;
			switch (type._kind) {
			case INT:
			case REAL:
			case BOOLEAN:
			case INTARRAY:
			case REALARRAY:
			case BOOLEANARRAY:
			case INTMATRIX:
			case REALMATRIX:
			case BOOLEANMATRIX:
			case PMF:
			case PMFARRAY:
			case PMFMATRIX:
			case INTERFACEARRAY:
			case INTERFACEMATRIX:
				(new TemplateExpander.FromLine("    @CNAME _@NAME;")) //
						.substitute("@CNAME", type._cName) //
						.substitute("@NAME", name) //
						.run();
				break;
			case INTERFACE: {
				Signature signature = type._signature;
				int k = engine._signatureTable.getSignatures().indexOf(signature) + 1;
				(new TemplateExpander.FromLine("    _@ENGINE_Interface@INDEX *_@NAME;")) //
						.substitute("@ENGINE", engineName) //
						.substitute("@INDEX", String.valueOf(k)) //
						.substitute("@NAME", name) //
						.run();
				break;
			}
			case QType.FUNCTION_KIND:
				break;
			default:
				assert (false);
			}
		}
		_cSourceWriter.println("} _" + engineName + "_object;");
		_cSourceWriter.println();

		// _XXX_memRelease
		(new TemplateExpander.FromLine("void _@ENGINE_memRelease(_@ENGINE_object *self) {")) //
				.substitute("@ENGINE", engine._engineName) //
				.run();

		for (String name : engine._symbolTable.globalNames()) {
			Symbol symbol = engine._symbolTable.get(name);
			QType type = symbol._type;
			switch (type._kind) {
			case INTARRAY:
			case REALARRAY:
			case BOOLEANARRAY:
			case INTMATRIX:
			case REALMATRIX:
			case BOOLEANMATRIX:
			case PMF:
			case PMFARRAY:
			case PMFMATRIX:
			case INTERFACEARRAY:
			case INTERFACEMATRIX:
				_cSourceWriter.println("    flag(self->_" + name + ");");
				break;
			default:
				break;
			}
		}
		_cSourceWriter.println("    release((QObject *) self);");
		_cSourceWriter.println("}");
		_cSourceWriter.println();

		// get/set methods
		for (String name : engine._symbolTable.publicGlobalNames()) {
			Symbol symbol = engine._symbolTable.get(name);
			if (symbol._isPublic) {
				QType type = engine._symbolTable.get(name)._type;
				switch (type._kind) {

				case INTARRAY:
				case REALARRAY:
				case BOOLEANARRAY:
				case INTMATRIX:
				case REALMATRIX:
				case BOOLEANMATRIX:
					new TemplateExpander.FromFile("GetSet.txt") //
							.substitute("@Q", engineName) //
							.substitute("@NAME", name) //
							.substitute("@TYPE", type._xName) //
							.substitute("@VALUETYPE", type._cName) //
							.run();
					break;

				case PMF:
				case PMFARRAY:
				case PMFMATRIX: {
					StringBuilder b = new StringBuilder();
					b.append("confirm");

					Qualifier qualifier = type._qualifier;
					switch (qualifier._category) {
					case SIMPLE: {
						int n = qualifier._simpleRVNames.size();
						if (n == 1) {
							b.append("Simple");
							b.append(type._xName);
							b.append("(value");
						} else {
							b.append("Joint");
							b.append(type._xName);
							b.append("(value,");
							b.append(n);
						}
						break;
					}
					case COMPOUND: {
						int n = qualifier._compoundRVNames.size();
						b.append("Compound");
						b.append(type._xName);
						b.append("(value,");
						b.append(n);
						for (int i = 0; i < n; i++) {
							b.append(",");
							b.append(qualifier._compoundRVNames.get(i).size());
						}
						break;
					}
					}

					b.append(")");

					new TemplateExpander.FromFile("GetSetPmf.txt") //
							.substitute("@Q", engineName) //
							.substitute("@NAME", name) //
							.substitute("@TYPE", type._xName) //
							.substitute("@VALUETYPE", type._cName) //
							.substitute("@CONFIRM", b.toString()).run();
					break;
				}

				case INTERFACE:
					new TemplateExpander.FromFile("GetSetInterface.txt") //
							.substitute("@Q", engineName).substitute("@NAME", name) //
							.substitute("@INDEX", engine._signatureTable.getIndex(type._signature) + 1) //
							.run();
					break;
				case INTERFACEARRAY:
				case INTERFACEMATRIX:
					new TemplateExpander.FromFile("GetSetInterfaceArrayOrMatrix.txt") //
							.substitute("@Q", engineName).substitute("@NAME", name) //
							.substitute("@TYPE", type._xName) //
							.substitute("@INDEX", engine._signatureTable.getIndex(type._signature) + 1) //
							.run();
					break;
				}
			}
		}

		// Declared functions
		for (String name : engine._symbolTable.globalNames()) {
			Symbol symbol = engine._symbolTable.get(name);
			QType type = symbol._type;
			switch (type._kind) {
			case QType.FUNCTION_KIND:
				functionGenerator.writeFunctionImplementation(symbol);
				if (symbol._isPublic || symbol._name.equals("init")) {
					String templateFilename;
					if (symbol._signature._returnType == QType.VOID)
						templateFilename = "VoidFunctionWrapper.txt";
					else
						templateFilename = "FunctionWrapper.txt";
					functionGenerator.writePythonWrapper(symbol, templateFilename);
				}
				break;
			case INTERFACE:
				if (symbol._isPublic) {
					String templateFilename;
					if (symbol._signature._returnType == QType.VOID)
						templateFilename = "VoidInterfaceWrapper.txt";
					else
						templateFilename = "InterfaceWrapper.txt";
					functionGenerator.writePythonWrapper(symbol, templateFilename);
				}
				break;

			default:
				break;
			}
		}

		// Function lookup arrays
		generateFunctionLookupArrays(engineName, engine);

		// _XXX_new
		{
			boolean isInit = engine._symbolTable.get("init") != null;
			String templateFilename = isInit ? "PyNewWithInit.txt" : "PyNew.txt";
			
			TemplateExpander t = (new TemplateExpander.FromFile(templateFilename)) //
					.substitute("@ENGINE", engineName);

			for (String name : engine._symbolTable.globalNames()) {
				Symbol symbol = engine._symbolTable.get(name);
				QType type = symbol._type;

				if (type._kind != QType.FUNCTION_KIND) {

					t.repeat();
					t.substitute("@NAME", name);

					switch (type._kind) {
					case INT:
						t.substitute("@VALUE", "0");
						break;
					case REAL:
						t.substitute("@VALUE", "0.0");
						break;
					case BOOLEAN:
						t.substitute("@VALUE", "false");
						break;
					case INTARRAY:
					case REALARRAY:
					case BOOLEANARRAY:
					case INTMATRIX:
					case REALMATRIX:
					case BOOLEANMATRIX:
						t.substitute("@VALUE", "default" + type._qName + "((QObject *)self)");
						break;
					case PMF:
					case PMFARRAY:
					case PMFMATRIX:
						t.substitute("@VALUE", "default" + type._qName + "((QObject *)self, "
								+ ExpressionGenerator.generateQualifier(type._qualifier) + ")");
						break;
					case INTERFACE: {
						Signature signature = type._signature;
						int k = engine._signatureTable.getSignatures().indexOf(signature) + 1;
						t.substitute("@VALUE", "&_" + engineName + "_defaultImplementation" + k);
						break;
					}
					case INTERFACEARRAY: {
						Signature signature = type._signature;
						int k = engine._signatureTable.getSignatures().indexOf(signature) + 1;
						t.substitute("@VALUE", "createInterfaceArray((QObject *) self, (void *) &_" + engineName
								+ "_defaultImplementation" + k + ", 1)");
						break;
					}
					case INTERFACEMATRIX: {
						Signature signature = type._signature;
						int k = engine._signatureTable.getSignatures().indexOf(signature) + 1;
						t.substitute("@VALUE", "createInterfaceMatrix((QObject *) self, (void *) &_" + engineName
								+ "_defaultImplementation" + k + ", 1, 1)");
						break;
					}
					default:
						assert (false);
					}
				}
			}

			t.run();
		}

		// PyMethodDef
		{
			_cSourceWriter.println("PyMethodDef _" + engineName + "_methods[] = {");
			for (String name : engine._symbolTable.publicGlobalNames()) {
				Symbol symbol = engine._symbolTable.get(name);
				if (symbol._type._kind == QType.FUNCTION_KIND)
					_cSourceWriter.printf("    { \"%s\", _%s_%s_Py, METH_VARARGS, NULL },\n", toPythonName(name),
							engine._engineName, name);
			}
			_cSourceWriter.println("    { NULL }");
			_cSourceWriter.println("};");
			_cSourceWriter.println();
		}

		// PyMemberDef
		{
			_cSourceWriter.println("PyMemberDef _" + engineName + "_members[] = {");
			for (String name : engine._symbolTable.publicGlobalNames()) {
				Symbol symbol = engine._symbolTable.get(name);
				String membertype;
				switch (symbol._type._kind) {
				case INT:
					membertype = "T_INT";
					break;
				case REAL:
					membertype = "T_DOUBLE";
					break;
				case BOOLEAN:
					membertype = "T_BOOL";
					break;
				default:
					continue;
				}
				_cSourceWriter.printf("    { \"%s\", %s, offsetof(_%s_object, _%s), 0, NULL },\n", toPythonName(name),
						membertype, engineName, name);
			}

			(new TemplateExpander.FromLine(
					"    { \"current_memory_use\", T_ULONGLONG, offsetof(_@ENGINE_object, currentMemoryUse), READONLY, NULL },")) //
							.substitute("@ENGINE", engineName) //
							.run();
			(new TemplateExpander.FromLine(
					"    { \"peak_memory_use\", T_ULONGLONG, offsetof(_@ENGINE_object, peakMemoryUse), READONLY, NULL },")) //
							.substitute("@ENGINE", engineName) //
							.run();
			_cSourceWriter.println("    { NULL }");
			_cSourceWriter.println("};");
			_cSourceWriter.println();
		}

		// PyGetSetDef
		{
			_cSourceWriter.println("PyGetSetDef _" + engineName + "_getset[] = {");

			for (String rawTokenName : engine._tokenTable.getPublicTokenStrings()) {
				String name = rawTokenName.substring(1, rawTokenName.length() - 1);
				(new TemplateExpander.FromLine("    { \"@NAME\", (getter)_@ENGINE_get_@NAME, NULL, NULL, NULL },")) //
						.substitute("@ENGINE", engineName) //
						.substitute("@NAME", name) //
						.run();
			}

			for (String name : engine._symbolTable.publicGlobalNames()) {
				Symbol symbol = engine._symbolTable.get(name);
				switch (symbol._type._kind) {
				case INT:
				case REAL:
				case BOOLEAN:
				case QType.FUNCTION_KIND:
					continue;
				default:
					break;
				}

				(new TemplateExpander.FromLine(
						"    { \"@PYNAME\", (getter)_@ENGINE_get_@NAME, (setter)_@ENGINE_set_@NAME, NULL, NULL },")) //
								.substitute("@PYNAME", toPythonName(name)) //
								.substitute("@ENGINE", engineName) //
								.substitute("@NAME", name) //
								.run();
			}
			_cSourceWriter.println("    { NULL }");
			_cSourceWriter.println("};");
			_cSourceWriter.println();
		}

		// PyTypeObject
		new TemplateExpander.FromFile("PyTypeObject.txt") //
				.substitute("@MODULE", modulename) //
				.substitute("@ENGINE", engineName) //
				.run();
	}

	private static void generateSignatureDeclarations(String objectname, Engine engine) {
		List<Signature> signatureList = engine._signatureTable.getSignatures();
		int implementationCount = 0;
		for (String functionName : engine._symbolTable.globalNames()) {
			Symbol symbol = engine._symbolTable.get(functionName);
			if (symbol._type._kind != QType.FUNCTION_KIND)
				continue;
			Signature signature = symbol._signature;
			if (!engine._signatureTable.getSignatures().contains(signature))
				continue;
			implementationCount++;
		}
		_cSourceWriter.println("#define _" + objectname + "_IMPLEMENTATION_COUNT " + implementationCount);
		_cSourceWriter.println("typedef struct _" + objectname + "_object _" + objectname + "_object;");

		for (int i = 0; i < signatureList.size(); i++) {
			Signature signature = signatureList.get(i);
			TemplateExpander t = new TemplateExpander.FromLine(engine, //
//					"@RETURN-TYPE _@OBJ_defaultImplementation@INDEX(_@OBJ_object *self, @REPEAT(@PARAM-TYPE@,,@)) { throw CException(\"Uninitialized interface\"); }" //
					"@RETURN-TYPE _@OBJ_defaultImplementation@INDEX(_@OBJ_object *self@REPEAT(, @PARAM-TYPE@)) { throw CException(\"Uninitialized interface\"); }" //
			) //
					.substitute("@OBJ", objectname) //
					.substitute("@RETURN-TYPE", signature._returnType._cName) //
					.substitute("@INDEX", i + 1);

			for (int j = 0; j < signature._parameterTypes.size(); j++) {
				t.repeat() //
						.substitute("@PARAM-TYPE", signature._parameterTypes.get(j)._cName);
			}

			t.run();
		}

		for (int i = 0; i < signatureList.size(); i++) {
			Signature signature = signatureList.get(i);
			TemplateExpander t = new TemplateExpander.FromLine(engine, //
//					"typedef @RETURN-TYPE _@OBJ_Interface@INDEX(_@OBJ_object *, @REPEAT(@PARAM-TYPE@,,@));" //
					"typedef @RETURN-TYPE _@OBJ_Interface@INDEX(_@OBJ_object *@REPEAT(, @PARAM-TYPE@));" //
			) //
					.substitute("@OBJ", objectname) //
					.substitute("@RETURN-TYPE", signature._returnType._cName) //
					.substitute("@INDEX", i + 1);

			for (int j = 0; j < signature._parameterTypes.size(); j++) {
				t.repeat() //
						.substitute("@PARAM-TYPE", signature._parameterTypes.get(j)._cName);
			}

			t.run();
		}

		for (int i = 0; i < signatureList.size(); i++) {
			new TemplateExpander.FromLine(engine, //
					"#define _@OBJ_lookup@INDEX(a,b) ((_@OBJ_Interface@INDEX *) lookup(a,b))" //
			) //
					.substitute("@OBJ", objectname) //
					.substitute("@INDEX", i + 1) //
					.run();
		}

		_cSourceWriter.println();
	}

	private static void generateFunctionLookupArrays(String objectname, Engine engine) {
		ArrayList<String> functionNames = new ArrayList<String>();
		ArrayList<String> functionPythonNames = new ArrayList<String>();
		ArrayList<Signature> signatures = new ArrayList<Signature>();

		for (String functionName : engine._symbolTable.globalNames()) {
			Symbol symbol = engine._symbolTable.get(functionName);
			if (symbol._type._kind != QType.FUNCTION_KIND)
				continue;
			Signature signature = symbol._signature;
			if (!engine._signatureTable.getSignatures().contains(signature))
				continue;
			functionNames.add(functionName);
			functionPythonNames.add(toPythonName(functionName));
			signatures.add(signature);
		}

		if (functionNames.size() > 0) {
			TemplateExpander t = new TemplateExpander.FromLine( //
					"void *_@OBJ_functionPointers[] = {@REPEAT((void *)_@OBJ_@FUNCTION@,,@)};" //
			);
			t.substitute("@OBJ", objectname);
			for (String functionName : functionNames)
				t.repeat().substitute("@FUNCTION", functionName);
			t.run();
			_cSourceWriter.println();

			t = new TemplateExpander.FromLine( //
					"const char *_@OBJ_functionNames[] = {@REPEAT(@FUNCTION@,,@)};" //
			);
			t.substitute("@OBJ", objectname);
			for (String functionName : functionPythonNames)
				t.repeat().substitute("@FUNCTION", "\"" + functionName + "\"");
			t.run();
			_cSourceWriter.println();

			t = new TemplateExpander.FromLine( //
					"int _@OBJ_functionSignatureIndexes[] = {@REPEAT(@INDEX@,,@)};" //
			);
			t.substitute("@OBJ", objectname);
			for (Signature signature : signatures)
				t.repeat().substitute("@INDEX", engine._signatureTable.getSignatures().indexOf(signature) + 1);
			t.run();
			_cSourceWriter.println();
		} else {
			TemplateExpander t = new TemplateExpander.FromLine( //
					"void **_@OBJ_functionPointers = NULL;" //
			);
			t.substitute("@OBJ", objectname);
			t.run();

			t = new TemplateExpander.FromLine( //
					"const char **_@OBJ_functionNames = NULL;" //
			);
			t.substitute("@OBJ", objectname);
			t.run();

			t = new TemplateExpander.FromLine( //
					"int *_@OBJ_functionSignatureIndexes = NULL;" //
			);
			t.substitute("@OBJ", objectname);
			t.run();
			_cSourceWriter.println();
		}
	}

	private static void generateModule(String modulename, ArrayList<Engine> allEngines) {
		_cSourceWriter.println("// MODULE");
		_cSourceWriter.println();

		// PyModuleDef
		new TemplateExpander.FromFile("PyModuleDef.txt").substitute("@MODULE", modulename).run();

		// PyInit_XXX
		TemplateExpander t = new TemplateExpander.FromFile("Module.txt");
		t.substitute("@MODULE-NAME", modulename);
		for (Engine engine : allEngines) {
			String engineName = engine._engineName;
			t.repeat().substitute("@OBJECT-NAME", engineName);
		}
		t.run();
	}

	private static String toPythonName(String qname) {
		StringBuilder b = new StringBuilder();
		b.append(Character.toLowerCase(qname.charAt(0)));

		for (int i = 1; i < qname.length(); i++) {
			char c = qname.charAt(i);
			if (Character.isUpperCase(c))
				b.append('_');
			b.append(Character.toLowerCase(c));
		}

		return b.toString();
	}
}