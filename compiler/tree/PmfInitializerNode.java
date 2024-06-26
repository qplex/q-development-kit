// Stub generated by FactoryMaker

package tree;

import compiler.QType;
import compiler.Qualifier;
import parser.*;

public class PmfInitializerNode extends QNode implements QParserTreeConstants {
	public PmfInitializerNode() {
		super(JJTPMFINITIALIZER);
	}

	int _dimension;

	void initialize() {
		int n = jjtGetNumChildren();
		int[] a = new int[n];
		for (int i = 0; i < n; i++)
			a[i] = ((PmfInitializerGroupNode) getChild(i))._dimension;
		_type = new QType(PMF, new Qualifier(a));
	}
}
