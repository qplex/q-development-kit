package generator;

import java.util.Stack;

/** Manages indentation in generated C++ code. */
class IndentationManager {

	private enum Token {
		SAMPLE, BLOCK
	};

	private Stack<Token> _stack = new Stack<Token>();

	private int _sampleCount;
	
	void pushBlock() {
		_stack.push(Token.BLOCK);
	}

	void pushSample() {
		_stack.push(Token.SAMPLE);
		_sampleCount++;
	}

	void pop() {
		if (_stack.pop() == Token.SAMPLE)
			_sampleCount--;
	}

	boolean peekIsBlock() {
		return _stack.peek() == Token.BLOCK;
	}

	boolean peekIsSample() {
		return _stack.peek() == Token.SAMPLE;
	}

	void writeIndent() {
		for (int i = 0; i < _stack.size(); i++)
			Generator._cSourceWriter.print("    ");
	}
	
	int getSampleCount() {
		return _sampleCount;
	}
}
