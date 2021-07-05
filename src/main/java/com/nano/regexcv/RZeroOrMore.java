package com.nano.regexcv;

public class RZeroOrMore extends RegularExpression {

	private RegularExpression regex;

	public RZeroOrMore(RegularExpression regex) {
		this.regex = regex;
	}

	@Override
	public Nfa generateNfa() {
		Nfa nfa = regex.generateNfa();
		nfa.start.addEmptyTransition(nfa.end);
		nfa.end.addEmptyTransition(nfa.start);
		return nfa;
	}
}
