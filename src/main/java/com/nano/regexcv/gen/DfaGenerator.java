package com.nano.regexcv.gen;

import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.Dfa;

public interface DfaGenerator<R> {
	public R generate(Dfa nfa, CharacterClass charClass);
}
