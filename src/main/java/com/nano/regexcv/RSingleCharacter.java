package com.nano.regexcv;

public class RSingleCharacter extends RegularExpression {
	
	private char ch;

	public RSingleCharacter(char ch) {
		this.ch = ch;
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		start.addTransition(getCharClassNumber(ch), end);
		return new Nfa(start, end);
	}
    
}
