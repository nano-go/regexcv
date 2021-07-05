package com.nano.regexcv;


public class RCharRange extends RegularExpression {

	private char from, to;

	public RCharRange(char from, char to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		int[] charClassRange = getCharClass().getClassNumberRange(from, to);
		for (int i = charClassRange[0]; i <= charClassRange[1]; i ++) {
			start.addTransition(i, end);
		}
		return new Nfa(start, end);
	}
}
