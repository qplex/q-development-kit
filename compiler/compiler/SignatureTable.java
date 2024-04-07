package compiler;

import java.util.*;

/** A collection of function and interface signatures.  Determines which functions implement each interface. */
public class SignatureTable {
	private final ArrayList<Signature> _signatureList = new ArrayList<Signature>();
	private final HashMap<Signature, Integer> _signatureMap = new HashMap<Signature, Integer>();

	public void add(Signature signature) {
		if (_signatureMap.keySet().contains(signature))
			return;
		_signatureList.add(signature);
		_signatureMap.put(signature, _signatureMap.size());
	}
	
	public int getIndex(Signature signature) {
		Integer k = _signatureMap.get(signature);
		if (k == null)
			return -1;
		else
			return k;
	}
	
	public List<Signature> getSignatures() {
		return (List<Signature>) Collections.unmodifiableList(_signatureList);
	}
}


