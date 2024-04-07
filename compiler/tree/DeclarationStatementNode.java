// Stub generated by FactoryMaker

package tree;

import compiler.*;
import compiler.SymbolTable.SymbolAlreadyDeclaredException;
import parser.*;

public class DeclarationStatementNode extends QNode implements QParserTreeConstants {
	public DeclarationStatementNode() {
		super(JJTDECLARATIONSTATEMENT);
	}

	void initialize() {
		Symbol symbol = new Symbol();

		symbol._isPublic = isToken(0) && getToken(0).kind == PUBLIC;
		if (symbol._isPublic && Engine._instance._symbolTable.getLevel() > 0)
			throw new CompileException("Local declarations cannot be public", this);

		int k = symbol._isPublic ? 1 : 0;
		symbol._name = getToken(k + 1).image;

		symbol._node = this;

		try {
			Engine._instance._symbolTable.add(symbol);
		} catch (SymbolAlreadyDeclaredException e) {
			throw new CompileException("Duplicate symbol definition", getToken(k + 1));
		}

		if ((!isToken(k + 2)) && getNode(k + 2).getId() == JJTFUNCTIONDECLARATIONBODY) {
			BlockNode block = (BlockNode) (getChild(1).getChild(1));

			symbol._type = new QType(QType.FUNCTION_KIND, block._signature);
			symbol._signature = block._signature;
			symbol._signature._returnType = QType.getType((TypeNode) getNode(k));

			boolean isSamplingFunction = block._samplingDepth > 0;

			if (symbol._name.equals("init")) {
				if (symbol._isPublic)
					throw new CompileException("Constructor may not be public", this);
				if (symbol._signature._returnType._kind != VOID)
					throw new CompileException("Constructor may not return a value", this);
			}
			
			if (isSamplingFunction && symbol._signature._returnType._kind != PMF)
				throw new CompileException("Sampling function must return Pmf", getChild(0));

			if (symbol._signature._returnType._kind != VOID)
				if (block._returnValueNodes == null || !block._isTerminal)
					throw new CompileException("Non-void function must return a value on all branches",
							block.jjtGetFirstToken());

			if (!isSamplingFunction) {
				if (symbol._signature._returnType._kind == VOID) {
					// All return statements must not return a value
					if (block._returnValueNodes != null) {
						QNode node = block._returnValueNodes.get(0);
						throw new CompileException("Return statements in void function may not return a value", node);
					}
					return;
				} else {
					// All return statements must return a value of correct type
					for (QNode returnValueNode : block._returnValueNodes) {
						QType returnType;
						if (returnValueNode._type._kind != RETURN)
							returnType = returnValueNode._type;
						else
							returnType = returnValueNode.getChild(0).getChild(0)._type;
						if (!symbol._signature._returnType.isAssignableFrom(returnType))
							throw new CompileException("Expected " + symbol._signature._returnType, returnValueNode);
					}
					return;
				}
			}

			if (symbol._signature._returnType._qualifier._category == Qualifier.Category.SIMPLE) {
				int numberOfSamplesRequired = symbol._signature._returnType._qualifier._simpleRVNames.size();
				for (QNode returnValueNode : block._returnValueNodes) {
					if (returnValueNode.jjtGetNumChildren() != numberOfSamplesRequired)
						throw new CompileException("Expected " + numberOfSamplesRequired + " sample(s)",
								(QNode) returnValueNode.jjtGetParent());
					for (int i = 0; i < numberOfSamplesRequired; i++)
						if (returnValueNode.getChild(i).jjtGetNumChildren() != 1)
							throw new CompileException("Expected " + numberOfSamplesRequired + " sample(s)",
									(QNode) returnValueNode.jjtGetParent());
					for (int i = 0; i < numberOfSamplesRequired; i++)
						if (returnValueNode.getChild(i).getChild(0)._type != QType.INT)
							throw new CompileException("Returned samples must be nonnegative integers",
									(QNode) returnValueNode.jjtGetParent());
				}
				return;
			}

			int[] numbersOfSamplesRequired = new int[symbol._signature._returnType._qualifier._compoundRVNames.size()];
			for (int i = 0; i < numbersOfSamplesRequired.length; i++)
				numbersOfSamplesRequired[i] = symbol._signature._returnType._qualifier._compoundRVNames.get(i).size();

			for (QNode returnValueNode : block._returnValueNodes) {
				if (returnValueNode.jjtGetNumChildren() != numbersOfSamplesRequired.length)
					throw new CompileException("Expected " + numbersOfSamplesRequired.length + " groups of samples",
							(QNode) returnValueNode.jjtGetParent());
				for (int i = 0; i < numbersOfSamplesRequired.length; i++) {
					int numberOfSamplesRequiredInGroup = numbersOfSamplesRequired[i];
					int numberOfSamplesProvidedInGroup = returnValueNode.getChild(i).jjtGetNumChildren();
					if (numberOfSamplesRequiredInGroup != numberOfSamplesProvidedInGroup)
						throw new CompileException("Expected a group of " + numberOfSamplesRequiredInGroup + " samples",
								(QNode) returnValueNode.getChild(i));
					for (int j = 0; j < numberOfSamplesRequiredInGroup; j++)
						if (returnValueNode.getChild(i).getChild(j)._type != QType.INT)
							throw new CompileException("Returned samples must be nonnegative integers",
									(QNode) returnValueNode.jjtGetParent());
				}

			}

			return;
		}

		symbol._type = QType.getType((TypeNode) getNode(k));

		if (getToken(k + 2).kind == SEMICOLON)
			return;

		QNode sourceNode = getNode(k + 3);

		if (Engine._instance._symbolTable.getLevel() == 0)
			throw new CompileException("Global variables may not be assigned initial values", sourceNode);

		if (sourceNode.getId() == JJTCREATEEXPRESSION) {
			if (sourceNode._type._kind != symbol._type._kind)
				throw new CompileException("Type mismatch", sourceNode);
			_type = sourceNode._type = symbol._type;
		} else if (!symbol._type.isAssignableFrom(sourceNode._type))
			throw new CompileException("Type mismatch", sourceNode);
	}
}