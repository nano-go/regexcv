package com.nano.regexcv.util;

public class CharacterRange implements Comparable<CharacterRange>, Digraph.Acception {
	
	public static final CharacterRange EPSILON = new CharacterRange('ε', 'ε');
	
	public static final CharacterRange RANGE_a_z = new CharacterRange('a', 'z');
	public static final CharacterRange RANGE_A_Z = new CharacterRange('A', 'Z');
	public static final CharacterRange RANGE_0_9 = new CharacterRange('0', '9');
	public static final CharacterRange RANGE_ANY = new CharacterRange((char) 0, Character.MAX_VALUE);
	
	public static int hashCode(char from, char to) {
		return from*31 + to;
	}
	
	protected static String charToString(char ch) {
		switch (ch) {
			case '\n': return "\\\\n";
			case '\r': return "\\\\r";
			case '\f': return "\\\\f";
			case '\b': return "\\\\b";
			case '\t': return "\\\\t";
			case '|': return "\\\\|";
			case ' ': return "space";
		}
		if (!(ch < 127 && ch >= 0x21) && 
			(ch == 0 || !Character.isUnicodeIdentifierPart(ch))) {
			return "0x" + Integer.toHexString(ch);
		}
		return String.valueOf(ch);
	}
	
	protected final char from;
	protected final char to;

	public CharacterRange(char from, char to) {
		this.from = from;
		this.to = to;
	}
	
	public char getFrom() {
		return from;
	}

	public char getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		return hashCode(from, to);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof CharacterRange) {
			CharacterRange range = (CharacterRange) obj;
			return from == range.from && to == range.to;
		}
		return false;
	}
	
	public boolean equals(char from, char to) {
		return this.from == from && this.to == to;
	}

	@Override
	public int compareTo(CharacterRange range) {
		if (this.from > range.from) return 1;
		if (this.from < range.from) return -1;
		return 0;
	}

	@Override
	public String toString() {
		String label;
		if (from == to) {
			label = charToString(from);
		} else {
			label = String.format(
				"%s-%s", charToString(from), charToString(to)
			);
		}
		return label;
	}
}
