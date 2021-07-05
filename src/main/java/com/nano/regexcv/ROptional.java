package com.nano.regexcv;

public class ROptional extends RegularExpression {

	private RegularExpression regex;

	public ROptional(RegularExpression regex) {
		this.regex = regex;
	}

	@Override
	public Nfa generateNfa() {
		Nfa nfa = regex.generateNfa();
		nfa.start.addEmptyTransition(nfa.end);
		return nfa;
	}
}
