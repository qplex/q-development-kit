package analysis;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import tree.QNode;

/**
 * One sampling statement within a function: the sample names it declares and
 * the identifiers read by the pmf expression it draws from.
 *
 * Together with {@link FunctionSamplingAnalysis}, the sites of a function form
 * a dependency graph: a site depends on the sites that declared the samples
 * its pmf expression reads.
 */
public class SamplingSite {
	public final QNode _statementNode;
	public final ArrayList<String> _sampleNames = new ArrayList<String>();
	public final LinkedHashSet<String> _identifiersReadByPmfExpression = new LinkedHashSet<String>();

	SamplingSite(QNode statementNode) {
		_statementNode = statementNode;
	}

	public int lineNumber() {
		return _statementNode.jjtGetFirstToken().beginLine;
	}
}
