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
}
