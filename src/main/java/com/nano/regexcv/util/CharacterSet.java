package com.nano.regexcv.util;

public class CharacterSet {
	
	private boolean[] charMarked;
	
	public void add(char ch) {
		if (charMarked == null) {
			charMarked = new boolean[Character.MAX_VALUE+1];
		}
		charMarked[ch] = true;
	}
	
	public void remove(char ch) {
		if (charMarked == null) {
			return;
		}
		charMarked[ch] = false;
	}
	
	public void clear() {
		charMarked = null;
	}
	
	public boolean contains(char ch) {
		return charMarked != null ? charMarked[ch] : false;
	}
	
	/**
	 * If the given char is not in this set, the char will be 
	 * added into this set.
	 */
	public boolean containsOrAdd(char ch) {
		if (!contains(ch)) {
			add(ch);
			return false;
		}
		return true;
	}
}
