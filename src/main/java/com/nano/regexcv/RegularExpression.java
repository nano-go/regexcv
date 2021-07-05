package com.nano.regexcv;

public abstract class RegularExpression {
	
	private CharacterClass characterClass;
	
	protected void setCharacterClass(CharacterClass characterClass) {
		this.characterClass = characterClass;
	}
	
	protected CharacterClass getCharClass() {
		return characterClass;
	}
	
	protected int getCharClassNumber(char ch) {
		return characterClass.getClassNumber(ch);
	}
	
	protected int getMaxClassNumber() {
		return characterClass.getTableSize();
	}
	
	public abstract Nfa generateNfa();
}
