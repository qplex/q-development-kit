package analysis;

import compiler.Engine;
import compiler.QType;
import compiler.Symbol;

/**
 * Entry point for AST-level passes that run after parsing and semantic
 * checking and before code generation.
 *
 * Passes registered here may inspect the tree (warnings, statistics) or, in
 * the future, transform it (optimizations such as splitting a sampling
 * function into independent parts); code generation then runs on the result.
 * Passes should build on {@link FunctionSamplingAnalysis} where possible so
 * that the dependency model stays in one place.
 */
public class AnalysisRunner {

	public static void run(Engine engine) {
		String sourceName = engine._engineName + ".q";
		for (String globalName : engine._symbolTable.globalNames()) {
			Symbol globalSymbol = engine._symbolTable.get(globalName);
			if (globalSymbol._type._kind == QType.FUNCTION_KIND)
				UnusedSampleWarningPass.run(sourceName, globalSymbol);
		}
	}
}
