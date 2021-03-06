package com.nano.regexcv;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharacterRangeSet;
import com.nano.regexcv.util.CharacterSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * We use a number representing a range of characters to describe
 * the transition of DFA/NFA.
 *
 * For a set of character ranges, we find all intersections and 
 * subtractions of those character ranges and number them.
 *
 * For Example:
 * We hava a set of characters:
 * {['0' - '9'], ['a'-'z'], ['f'], ['u'], ['n']}
 * 
 * We can get the following table:
 * ┌───────┬───────┬───┬───────┬───┬───────┐
 * │ 0 - 9 │ a - e │ f │ g - m │ n │ o - z │
 * ├───────┼───────┼───┼───────┼───┼───────┤
 * │ 0     │ 1     │ 2 │ 3     │ 4 │ 5     │
 * └───────┴───────┴───┴───────┴───┴───────┘
 *
 * We can use the {1, 2, 3, 4, 5} to represent ['a' - 'z'],
 * {0} to represent ['0' - '9'], {2} to represent ['f'] ...
 */
public class CharacterClass {
	
	public static final int EPSILON_CHAR_CLASS = 0;
	public static final int INVALID_CHAR_CLASS = -1;
	
	private CharacterRangeSet charRangeSet;
	private CharacterRange[] charClassTable;
	
	protected CharacterClass() {}
	
	public void generateTable(CharacterRangeSet charRangeSet) {
		List<CharacterRange> ranges = charRangeSet.sortedCharRanges();
		ArrayList<Character> overlappingRangeChars = new ArrayList<>(charRangeSet.size());
		ArrayList<CharacterRange> table = new ArrayList<>();
		CharacterSet duplicatedChars = new CharacterSet();
		char maxRight = 0;
		for (CharacterRange range : ranges) {
			char from = range.getFrom(), to = range.getTo();
			if (from > maxRight && !overlappingRangeChars.isEmpty()) {
				table.addAll(getTable(overlappingRangeChars, charRangeSet));
				overlappingRangeChars.clear();
			}
			
			if (!duplicatedChars.containsOrAdd(from))
				overlappingRangeChars.add(from);				
			if (!duplicatedChars.containsOrAdd(to))
				overlappingRangeChars.add(to);
			
			maxRight = (char) Math.max(range.getTo(), maxRight);
		}
		
		if (!overlappingRangeChars.isEmpty()) {
			table.addAll(getTable(overlappingRangeChars, charRangeSet));
		}
		
		this.charClassTable = table.toArray(new CharacterRange[table.size()]);
		this.charRangeSet = charRangeSet;
	}

	private List<CharacterRange> getTable(List<Character> chars, CharacterRangeSet charRangeSet) {
		Collections.sort(chars);
		ArrayList<CharacterRange> table = new ArrayList<>(chars.size());
		final int size = chars.size();
		int i = 0;
		while (i < chars.size()) {
			char left = chars.get(i);
			if (charRangeSet.isRightChar(left)) {
				if (charRangeSet.isLeftChar(left)) {
					table.add(new CharacterRange(left, left));
				}
				left ++;
			}
			if (++ i >= size) {
				break;
			}
			char next = chars.get(i);
			if (charRangeSet.isLeftChar(next)) {	
			    next --;
			}
			if (left <= next) {
				table.add(new CharacterRange(left, next));
			}
		}
		return table;
	}
	
	protected int getClassNumber(char ch) {
		int l = 0, r = charClassTable.length-1;
		while (l <= r) {
			int mid = (r + l) >> 1;
			CharacterRange range = charClassTable[mid];
			if (ch > range.getTo()) {
				l = mid + 1;
			} else if (ch < range.getFrom()) {
				r = mid - 1;
			} else {
				return mid+1;
			}
		}
		throw new Error("The char '" + ch + "' not found.");
	}
	
	public int getInputCharClassNumber(char ch) {
		try {
			return getClassNumber(ch);
		} catch (Error e) {
			return INVALID_CHAR_CLASS;
		}
	}
	
	/**
	 * Finds the class numbers that contains the specified
	 * character range.
	 *
	 * For Example:
	 * We have a char class table:
	 * ┌───────┬───────┬───────┐
	 * │ a - h │ i - m │ o - z │
	 * │───────┼───────┼───────│
	 * │ 1     │ 2     │ 3     │
	 * └───────┴───────┴───────┘ 
	 *
	 * If we want to find the char class numbers corresponding to 
	 * the char range ['a'-'z'], then the class number range [1, 3] 
	 * to represent [1(a-h), 2(i-m), 3(o-z)] will be found.
	 */
	protected int[] getClassNumberRange(char from, char to) {
		int l = 0,r = charClassTable.length - 1;
		while (l <= r) {
			int mid = l + (r - l) / 2;
			CharacterRange s = charClassTable[mid];
			if (from > s.getFrom()) {
				l = mid + 1;
			} else if (from < s.getFrom()) {
				r = mid - 1;
			} else {
				int start = mid;
				while (s.getTo() != to)
					s = charClassTable[++ mid];
				return new int[]{start + 1,mid + 1};
			}
		}
		throw new Error(
			"The char range ['" + from + "', '" + to + "'] not found.");
	}
	
	protected CharacterRange[] getCharClassTable() {
		return this.charClassTable;
	}
	
	public CharacterRange getCharRange(int charClassNumber) {
		return this.charClassTable[charClassNumber-1];
	}
	
	public int getTableSize() {
		return this.charClassTable.length;
	}
}
