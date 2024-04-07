package compiler;

import java.util.*;

/** Collection of tokens declared in the Q source. */
public class TokenTable {
	public static class TokenAlreadyDeclaredException extends Exception {}
	
	private ArrayList<String> tokenStrings = new ArrayList<String>();
	private ArrayList<String> publicTokenStrings = new ArrayList<String>();
	private HashMap<String, Integer> tokenValues = new HashMap<String, Integer>();
	
	public void add(boolean isPublic, String qtoken, int value) throws TokenAlreadyDeclaredException {
		if (tokenValues.keySet().contains(qtoken))
			throw new TokenAlreadyDeclaredException();
		tokenStrings.add(qtoken);
		if (isPublic)
			publicTokenStrings.add(qtoken);
		tokenValues.put(qtoken, value);
	}
	
	public List<String> getTokenStrings() {
		return (List<String>) Collections.unmodifiableList(tokenStrings);
	}
	
	public List<String> getPublicTokenStrings() {
		return (List<String>) Collections.unmodifiableList(publicTokenStrings);
	}

	public int getValueOfToken(String s) {
		Integer value = tokenValues.get(s);
		if (value == null)
			return -1;
		return value;
	}
}
