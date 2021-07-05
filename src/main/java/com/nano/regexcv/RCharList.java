package com.nano.regexcv;

public class RCharList extends RegularExpression {

	private char[] chs;

	public RCharList(char... chs) {
		this.chs = chs;
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		for (char ch : chs) {
			start.addTransition(getCharClassNumber(ch), end);
		}
		if (chs.length == 0) {
			start.addEmptyTransition(end);
		}
		return new Nfa(start, end);
	}
	
}
