package com.nano.regexcv;
import java.util.List;

public class RContatenation extends RegularExpression {

	private List<RegularExpression> regexList;

	public RContatenation(List<RegularExpression> regexList) {
		this.regexList = regexList;
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		NfaState last = start;
		for (RegularExpression regex : regexList) {
			Nfa nfa = regex.generateNfa();
			last.addEmptyTransition(nfa.start);
			last = nfa.end;
		}
		last.addEmptyTransition(end);
		return new Nfa(start, end);
	}
}
