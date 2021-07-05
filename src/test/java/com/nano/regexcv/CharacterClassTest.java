package com.nano.regexcv;

import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.CharacterClassTest;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharacterRangeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterClassTest {
	
	private static final String[][] TEST_CASES = {
		{"a-z, A-Z, 0-9", "a-z", "A-Z", "0-9"},
		{"a-z, d-f", "a-c", "d-f", "g-z"},
		{"a, b", "a", "b"},
		{"a-e, d-h, g-l, o-r, q-u, v-z", "a-c", "d-e", "f", "g-h", "i-l", "o-p", "q-r", "s-u", "v-z"},
	};
	
	@Test public void test() {
		for (String[] testCase : TEST_CASES) {
			CharacterClass charClass = parseCharacterClass(testCase[0]);
			ArrayList<CharacterRange> expectedRanges = new ArrayList<>();
			for (int i = 1; i < testCase.length; i ++) {
				expectedRanges.add(parseRange(testCase[i]));
			}
			Collections.sort(expectedRanges);
			assertArrayEquals(
				expectedRanges.toArray(new CharacterRange[0]),
				charClass.getCharClassTable()
			); 
			
			int i = 0;
			for (CharacterRange range : charClass.getCharClassTable()) {
				for (char ch = range.getFrom(); ch <= range.getTo(); ch ++) {
					assertEquals("Char: " + ch,
						i, charClass.getClassNumber(ch) - 1
					);
				}
				i ++;
			}
		}
	}
	
	private static CharacterClass parseCharacterClass(String charRangesText) {
		List<CharacterRange> parsedRanges = Arrays.stream(charRangesText.split(","))
			.map(CharacterClassTest::parseRange)
			.collect(Collectors.toList());

		CharacterRangeSet rangeSet = new CharacterRangeSet();
		for (CharacterRange range : parsedRanges) {
			rangeSet.addRange(range.getFrom(), range.getTo());
		}
		CharacterClass charClass = new CharacterClass();
		charClass.generateTable(rangeSet);
		return charClass;
	}

	private static CharacterRange parseRange(String range) {
		range = range.trim();
		if (range.length() == 1) {
			char ch = range.charAt(0);
			return new CharacterRange(ch, ch);
		}
		if (range.length() == 3 && range.charAt(1) == '-') {
			char from = range.charAt(0), to = range.charAt(2);
			return new CharacterRange(from, to); 
		}
		throw new Error("Error range text: " + range);
	}
}
