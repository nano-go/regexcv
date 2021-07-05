package com.nano.regexcv;
import com.nano.regexcv.util.CharacterRange;

public class RCharRangeList extends RegularExpression {

	private CharacterRange[] charRanges;

	public RCharRangeList(CharacterRange[] charRanges) {
		this.charRanges = charRanges;
	}
	
	@Override
	public Nfa generateNfa() {
		NfaState start = new NfaState(getMaxClassNumber());
		NfaState end = new NfaState(getMaxClassNumber());
		for (CharacterRange range : charRanges) {
			int[] classNums = getCharClass().getClassNumberRange(
				range.getFrom(), range.getTo()
			);
			for (int i : classNums) start.addTransition(i, end);
		}
		if (charRanges.length == 0) {
			start.addEmptyTransition(end);
		}
		return new Nfa(start, end);
	}
}
