package compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Dynamic table of declared symbols in Q code. Tracks block level and removes
 * symbols when they go out of scope.
 */
public class SymbolTable {
	public static class SymbolAlreadyDeclaredException extends Exception {
	}

	private final ArrayList<String> _publicGlobalNamesInSequenceOfDeclaration = new ArrayList<String>();
	private final ArrayList<String> _globalNamesInSequenceOfDeclaration = new ArrayList<String>();
	private HashMap<String, Symbol> _mapOfSymbolsByName = new HashMap<String, Symbol>();
	private int _level;

	public int getLevel() {
		return _level;
	}

	public void enterBlock() {
		_level++;
	}

	public void leaveBlock() {
		_mapOfSymbolsByName.values().removeIf(val -> val._level >= _level);
		_level--;
	}

	public void add(Symbol symbol) throws SymbolAlreadyDeclaredException {
		if (_mapOfSymbolsByName.containsKey(symbol._name))
			throw new SymbolAlreadyDeclaredException();

		symbol._level = _level;
		if (symbol._category == null)
			symbol._category = _level == 0 ? Category.GLOBAL : Category.LOCAL;

		_mapOfSymbolsByName.put(symbol._name, symbol);
		if (_level == 0)
			_globalNamesInSequenceOfDeclaration.add(symbol._name);
		if (_level == 0 && symbol._isPublic)
			_publicGlobalNamesInSequenceOfDeclaration.add(symbol._name);
	}

	public Symbol get(String name) {
		return _mapOfSymbolsByName.get(name);
	}

	public Collection<String> publicGlobalNames() {
		return Collections.unmodifiableList(_publicGlobalNamesInSequenceOfDeclaration);
	}

	public Collection<String> globalNames() {
		return Collections.unmodifiableList(_globalNamesInSequenceOfDeclaration);
	}
}
