package com.nano.regexcv.gen;
import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.Nfa;

public interface NfaGenerator<R> {
	public R generate(Nfa nfa, CharacterClass charClass);
}
