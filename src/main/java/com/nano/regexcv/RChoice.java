package com.nano.regexcv;
import java.util.List;
import java.util.Objects;
import java.util.Collections;

public class RChoice extends RegularExpression {

	private List<RegularExpression> regexList;

	public RChoice(List<RegularExpression> regexList) {
		this.regexList = regexList != null ? regexList : Collections.emptyList();
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		if (!regexList.isEmpty()) {
			for (RegularExpression regex : regexList) {
				Nfa nfa = regex.generateNfa();
				start.addEmptyTransition(nfa.start);
				nfa.end.addEmptyTransition(end);
			}
		} else {
			start.addEmptyTransition(end);
		}
		return new Nfa(start, end);
	}
	
}
