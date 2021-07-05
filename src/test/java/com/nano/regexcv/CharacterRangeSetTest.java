package com.nano.regexcv;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharacterRangeSet;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterRangeSetTest {
	
	/**
	 * Converts a character range set into a hash set.
	 */
	public static HashSet<CharacterRange> toHashSet(CharacterRangeSet set) {
		HashSet<CharacterRange> hashSet = new HashSet<>();
		for (CharacterRange range : set) hashSet.add(range);
		return hashSet;
	}
	
	@Test public void simpleOperationTest() {
		CharacterRangeSet set = new CharacterRangeSet();
		set.addChar('a');
		set.addChar('a');
		set.addChar('b');
		set.addChar('b');
		set.addRange('c', 'e');
		assertEquals(3, set.size());
		set.addRange('c', 'e');
		assertEquals(3, set.size());
		assertTrue(set.isLeftChar('c'));
		assertTrue(set.isLeftChar('a'));
		assertTrue(set.isLeftChar('b'));
		assertFalse(set.isLeftChar('e'));	
		assertTrue(set.isRightChar('a'));
		assertTrue(set.isRightChar('b'));
		assertTrue(set.isRightChar('e'));
		assertFalse(set.isRightChar('c'));
		
		HashSet<CharacterRange> expectedRange = new HashSet<>();
		expectedRange.add(new CharacterRange('a', 'a'));
		expectedRange.add(new CharacterRange('b', 'b'));
		expectedRange.add(new CharacterRange('c', 'e'));
		assertEquals(expectedRange, toHashSet(set));
	}
	
	@Test public void operationOnTheEmptySet() {
		CharacterRangeSet set = new CharacterRangeSet();
		assertFalse(set.isLeftChar('a'));
		assertFalse(set.isRightChar('b'));
		assertEquals(0, set.size());
	}
	
	@Test(timeout=2000) public void lotsOfDataTest() {
		CharacterRangeSet set = new CharacterRangeSet();
		HashSet<CharacterRange> expected = new HashSet<>();
		for (char ch = 0; ch < 10000; ch ++) {
			set.addChar(ch);
			expected.add(new CharacterRange(ch, ch));
		}
		assertEquals(10000, set.size());
		assertEquals(expected, toHashSet(set));
	}
}
