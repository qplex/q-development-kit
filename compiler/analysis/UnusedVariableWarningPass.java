package analysis;

import java.util.ArrayList;
import java.util.HashMap;

import compiler.Symbol;
import parser.QParserConstants;
import parser.QParserTreeConstants;
import parser.Token;
import tree.QNode;

/**
 * Warns about local variables that are declared but never used, the Q-level
 * analogue of a C++ compiler's -Wunused-variable. Without this pass an unused
 * Q local only surfaces as a warning on the generated C++, where the line
 * numbers and names no longer match the Q source.
 *
 * Only local variables declared inside function bodies are considered.
 * Engine-level declarations are shared state and interface-visible, function
 * parameters are part of the signature, and samples drawn from pmfs are
 * covered by the more precise {@link UnusedSampleWarningPass}.
 */
public class UnusedVariableWarningPass implements QParserTreeConstants, QParserConstants {

	public static void run(String sourceName, Symbol functionSymbol) {
		ArrayList<Token> declaredNameTokens = new ArrayList<Token>();
		HashMap<String, Integer> identifierOccurrenceCounts = new HashMap<String, Integer>();
		collectFromSubtree(functionSymbol._node, declaredNameTokens, identifierOccurrenceCounts);

		for (Token declaredNameToken : declaredNameTokens) {
			// The declaration itself accounts for one occurrence; any further
			// occurrence is a use.
			if (identifierOccurrenceCounts.get(declaredNameToken.image) == 1)
				Diagnostics.warn(sourceName, declaredNameToken.beginLine, //
						"In function '" + functionSymbol._name + "': variable '"
								+ declaredNameToken.image + "' is never used");
		}
	}

	/**
	 * Walks a subtree, recording the name token of every declaration statement
	 * and counting every identifier occurrence (including the declarations
	 * themselves).
	 */
	private static void collectFromSubtree(QNode currentNode, ArrayList<Token> declaredNameTokens,
			HashMap<String, Integer> identifierOccurrenceCounts) {
		boolean isVariableDeclaration = currentNode.getId() == JJTDECLARATIONSTATEMENT
				&& !declaresFunction(currentNode);
		boolean declaredNameSeen = false;

		for (int childIndex = 0; childIndex < currentNode.getTokenAndNodeCount(); childIndex++) {
			if (currentNode.isToken(childIndex)) {
				Token childToken = currentNode.getToken(childIndex);
				if (childToken.kind == IDENTIFIER) {
					Integer previousCount = identifierOccurrenceCounts.get(childToken.image);
					identifierOccurrenceCounts.put(childToken.image, previousCount == null ? 1 : previousCount + 1);
					// The declared name is the first identifier that is a direct
					// token of the declaration statement; the type and the
					// initializer are child nodes of their own.
					if (isVariableDeclaration && !declaredNameSeen) {
						declaredNameTokens.add(childToken);
						declaredNameSeen = true;
					}
				}
			} else {
				collectFromSubtree(currentNode.getNode(childIndex), declaredNameTokens, identifierOccurrenceCounts);
			}
		}
	}

	private static boolean declaresFunction(QNode declarationStatementNode) {
		for (int childIndex = 0; childIndex < declarationStatementNode.getTokenAndNodeCount(); childIndex++)
			if (!declarationStatementNode.isToken(childIndex)
					&& declarationStatementNode.getNode(childIndex).getId() == JJTFUNCTIONDECLARATIONBODY)
				return true;
		return false;
	}
}
