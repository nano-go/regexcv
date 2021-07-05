package com.nano.regexcv;

public class ROneOrMore extends RegularExpression {
	
	private RegularExpression regex;

	public ROneOrMore(RegularExpression regex) {
		this.regex = regex;
	}
	
	@Override
	public Nfa generateNfa() {
		Nfa nfa = regex.generateNfa();
		nfa.end.addEmptyTransition(nfa.start);
		return nfa;
	}
}
