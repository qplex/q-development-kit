package tree;

import java.io.IOException;
import java.util.ArrayList;

import compiler.*;
import compiler.CompileException;
import parser.*;

/** Base class for node classes in package <code>tree</code>. */
public abstract class QNode extends SimpleNode implements QParserTreeConstants, QParserConstants {

	QNode(int id) {
		super(id);
	}

	public int getId() {
		return id;
	}
	
	/** Computes the type of this node and does error-checking. */
	abstract void initialize() throws CompileException;
	
	public QType _type;
	
	public int _samplingCount;
	public int _samplingDepth;
	public boolean _hasSkipBeforeSampling;

	/**
	 * First token of a branchProbability() usage in this subtree that is not (yet)
	 * preceded by a sampling statement in an enclosing block; null if there is no
	 * such usage. Set in PrimaryExpressionNode, cleared in BlockNode when a
	 * preceding sampling statement satisfies the usage, and reported as an error
	 * by Engine if still pending at the root.
	 */
	public Token _pendingBranchProbabilityToken;
	public boolean _isTerminal;
	ArrayList<QNode> _returnValueNodes;
	
	// QNode maintains a list of child tokens and nodes
	private ArrayList<Object> _tokenAndNodeList;

	/** Returns whether the i-th child of this QNode is a Token. */
	public boolean isToken(int i) {
		return _tokenAndNodeList.get(i) instanceof Token;
	}

	/** Returns the i-th child of this QNode as a Token. */
	public Token getToken(int i) {
		return (Token) _tokenAndNodeList.get(i);
	}

	/** Returns the i-th child of this QNode as a QNode */
	public QNode getNode(int i) {
		return (QNode) _tokenAndNodeList.get(i);
	}

	// The number of tokens and nodes below this node
	public int getTokenAndNodeCount() {
		return _tokenAndNodeList.size();
	}

	public QNode getChild(int i) {
		return (QNode) children[i];
	}

	public QNode getParent() {
		return (QNode) jjtGetParent();
	}
	
	/*
	 * Performs node initialization. Unexpectedly, the parser calls jjtClose BEFORE
	 * jjtSetLastToken, so we need to initialize inside the latter.
	 */
	public void jjtSetLastToken(Token lastToken) {
		super.jjtSetLastToken(lastToken);

		// Create the token and node list
		_tokenAndNodeList = new ArrayList<Object>();
		Token currentToken = firstToken;
		int currentChildIndex = 0;
		int numChildren = children != null ? children.length : 0;
		QNode currentNode = numChildren > 0 ? (QNode) children[0] : null;

		while (true) {
			if (currentToken != null && currentNode != null) {
				if (currentToken != currentNode.firstToken) {
					_tokenAndNodeList.add(currentToken);
					currentToken = currentToken.next;
				} else {
					_tokenAndNodeList.add(currentNode);
					currentToken = currentNode.lastToken;
					currentToken = currentToken == lastToken ? null : currentToken.next;
					currentChildIndex++;
					currentNode = currentChildIndex < numChildren ? (QNode) children[currentChildIndex] : null;
				}
			} else if (currentToken != null) {
				_tokenAndNodeList.add(currentToken);
				currentToken = currentToken != lastToken ? currentToken.next : null;
			} else if (currentNode != null) {
				_tokenAndNodeList.add(currentNode);
				currentToken = currentNode.lastToken;
				currentToken = currentToken == lastToken ? null : currentToken.next;
				currentChildIndex++;
				currentNode = currentChildIndex < numChildren ? (QNode) children[currentChildIndex] : null;
			} else {
				break;
			}
		}

		// Propagate any pending branchProbability() usage upward. BlockNode.initialize re-derives this field for
		// blocks, clearing usages preceded by a sampling statement.
		int childNodeCount = children != null ? children.length : 0;
		for (int childIndex = 0; childIndex < childNodeCount; childIndex++) {
			QNode childNode = (QNode) children[childIndex];
			if (childNode._pendingBranchProbabilityToken != null) {
				_pendingBranchProbabilityToken = childNode._pendingBranchProbabilityToken;
				break;
			}
		}

		// jjtSetLastToken (this method) may have been called from a finally clause 
		// in the parser after a parse exception was thrown.
		// If initialize() throws a compile exception, pass it along.
		// If anything else (for example, index out of bounds, stick with the parser exception
		try {
			initialize();
		} catch(CompileException x) {
			throw x;
		}
	}
}