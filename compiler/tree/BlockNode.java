// Stub generated by FactoryMaker

package tree;

import compiler.SymbolTable.SymbolAlreadyDeclaredException;
import parser.*;
import compiler.*;

public class BlockNode extends QNode implements QParserTreeConstants {
	Signature _signature;

	public BlockNode() {
		super(JJTBLOCK);

		Engine._instance._symbolTable.enterBlock();
		_signature = Signature.getLastSignatureCreated();

		if (_signature != null) {
			for (int i = 0; i < _signature._parameterNames.size(); i++) {
				Symbol symbol = new Symbol();
				symbol._name = _signature._parameterNames.get(i);
				symbol._type = _signature._parameterTypes.get(i);
				symbol._category = Category.FIXED;

				try {
					Engine._instance._symbolTable.add(symbol);
				} catch (SymbolAlreadyDeclaredException e) {
					throw new CompileException("Duplicate symbol definition", _signature._parameterNameTokens.get(i));
				}
			}
		}
	}

	void initialize() {
		Engine._instance._symbolTable.leaveBlock();

		for (int i = 0; i < jjtGetNumChildren(); i++) {
			if (_isTerminal)
				throw new CompileException("Unreachable statement", getChild(i));

			_samplingDepth = Math.max(_samplingDepth, _samplingCount + getChild(i)._samplingDepth);
			_samplingCount += getChild(i)._samplingCount;
			_samplingDepth = Math.max(_samplingDepth, _samplingCount);
			_isTerminal = getChild(i)._isTerminal;

			if (!_hasSkipBeforeSampling)
				if (_samplingCount==0 && getChild(i)._hasSkipBeforeSampling) 
					_hasSkipBeforeSampling = true;

			if (getChild(i)._returnValueNodes != null) {
				if (_returnValueNodes == null)
					_returnValueNodes = getChild(i)._returnValueNodes;
				else
					_returnValueNodes.addAll(getChild(i)._returnValueNodes);
			}
		}

		if (_samplingDepth > 0 && !_isTerminal) 
			throw new CompileException("Block containing sampling statement must be left through a return or skip statement", this);		
	}
}
