package analysis;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import compiler.Symbol;
import parser.QParserConstants;
import parser.QParserTreeConstants;
import parser.Token;
import tree.QNode;

/**
 * Per-function analysis of sampling statements and the data dependencies
 * between them.
 *
 * The analysis models a function as a list of {@link SamplingSite}s (one per
 * sampling statement, in source order) plus the set of identifiers that are
 * read outside of sampling statements: in return statements, conditions,
 * assignments, indices and call arguments. Each site records the identifiers
 * read by its own pmf expression, so the sites form a dependency graph: a
 * site depends on the sites that declared the samples its pmf expression
 * reads.
 *
 * This graph is intended as shared infrastructure for AST-level analyses and
 * future transformations (for example, splitting a sampling function into
 * independent parts). The unused-sample warning is its first client.
 */
public class FunctionSamplingAnalysis implements QParserTreeConstants, QParserConstants {

	public final Symbol _functionSymbol;
	public final ArrayList<SamplingSite> _samplingSites = new ArrayList<SamplingSite>();
	public final LinkedHashSet<String> _identifiersReadOutsideSamplingStatements = new LinkedHashSet<String>();

	/**
	 * Whether the function calls branchProbability(). The value of
	 * branchProbability() reflects every draw made so far along the current
	 * branch, so when this flag is set, even a never-read draw affects
	 * observable behavior and must not be reported as removable.
	 */
	public boolean _callsBranchProbability;

	public FunctionSamplingAnalysis(Symbol functionSymbol) {
		_functionSymbol = functionSymbol;
		collectFromSubtree(functionSymbol._node, _identifiersReadOutsideSamplingStatements);
	}

	/**
	 * Walks a subtree, adding identifier reads to the given sink. A sampling
	 * statement is handled specially: its left-hand names are declarations
	 * rather than reads, and the reads of its pmf expression are collected
	 * into the site itself instead of the caller's sink.
	 */
	private void collectFromSubtree(QNode currentNode, LinkedHashSet<String> identifierSink) {
		if (currentNode.getId() == JJTSAMPLINGSTATEMENT) {
			SamplingSite samplingSite = new SamplingSite(currentNode);
			QNode identifierListNode = currentNode.getChild(0);
			for (int listIndex = 0; listIndex < identifierListNode.getTokenAndNodeCount(); listIndex += 2)
				samplingSite._sampleNames.add(identifierListNode.getToken(listIndex).image);
			collectFromSubtree(currentNode.getChild(1), samplingSite._identifiersReadByPmfExpression);
			_samplingSites.add(samplingSite);
			return;
		}

		for (int childIndex = 0; childIndex < currentNode.getTokenAndNodeCount(); childIndex++) {
			if (currentNode.isToken(childIndex)) {
				Token childToken = currentNode.getToken(childIndex);
				if (childToken.kind == IDENTIFIER)
					identifierSink.add(childToken.image);
				else if (childToken.kind == BRANCHPROBABILITY)
					_callsBranchProbability = true;
			} else {
				collectFromSubtree(currentNode.getNode(childIndex), identifierSink);
			}
		}
	}

	/**
	 * Returns the set of live names: identifiers whose value can affect the
	 * function's observable behavior. Seeded with all identifiers read outside
	 * sampling statements, then closed under the rule that a live sampling
	 * site's pmf expression reads are live as well. Sample names absent from
	 * the result belong to draws that cannot influence the returned
	 * distribution.
	 */
	public LinkedHashSet<String> computeLiveSampleNames() {
		LinkedHashSet<String> liveNames = new LinkedHashSet<String>(_identifiersReadOutsideSamplingStatements);
		boolean liveSetChanged = true;
		while (liveSetChanged) {
			liveSetChanged = false;
			for (SamplingSite samplingSite : _samplingSites) {
				boolean siteIsLive = false;
				for (String sampleName : samplingSite._sampleNames)
					if (liveNames.contains(sampleName))
						siteIsLive = true;
				if (siteIsLive && liveNames.addAll(samplingSite._identifiersReadByPmfExpression))
					liveSetChanged = true;
			}
		}
		return liveNames;
	}
}
