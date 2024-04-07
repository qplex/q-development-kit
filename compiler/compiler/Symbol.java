package compiler;

import tree.QNode;

/** A symbol declared in the Q source. */
public class Symbol {
	public boolean _isPublic;
	public String _name;
	public QType _type;
	public Signature _signature;
	public Category _category;
	public int _level;
	public QNode _node;
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (_isPublic)
			b.append("public ");
		b.append(_category);
		b.append(" ");
		b.append(_type);
		b.append(" ");
		b.append(_name);
		b.append(" ");
		if (_signature != null) {
			b.append(_signature);
		}
		
		return b.toString();
	}
}
