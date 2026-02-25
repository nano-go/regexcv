/*
 * Copyright 2021 nano1
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nano.regexcv.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;

public class CharacterRangeSetTest {

  /** Converts a character range set into a hash set. */
  public static HashSet<CharacterRange> toHashSet(CharacterRangeSet set) {
    HashSet<CharacterRange> hashSet = new HashSet<>();
    for (CharacterRange range : set) hashSet.add(range);
    return hashSet;
  }

  @Test
  public void simpleOperationTest() {
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

  @Test
  public void operationOnTheEmptySet() {
    CharacterRangeSet set = new CharacterRangeSet();
    assertFalse(set.isLeftChar('a'));
    assertFalse(set.isRightChar('b'));
    assertEquals(0, set.size());
  }

  @Test(timeout = 2000)
  public void lotsOfDataTest() {
    CharacterRangeSet set = new CharacterRangeSet();
    HashSet<CharacterRange> expected = new HashSet<>();
    for (char ch = 0; ch < 10000; ch++) {
      set.addChar(ch);
      expected.add(new CharacterRange(ch, ch));
    }
    assertEquals(10000, set.size());
    assertEquals(expected, toHashSet(set));
  }

  @Test
  public void sortedCharRangesShouldReturnOrderedValues() {
    CharacterRangeSet set = new CharacterRangeSet();
    set.addRange('f', 'z');
    set.addChar('a');
    set.addRange('b', 'd');

    List<CharacterRange> ranges = set.sortedCharRanges();
    assertEquals(3, ranges.size());
    assertEquals(new CharacterRange('a', 'a'), ranges.get(0));
    assertEquals(new CharacterRange('b', 'd'), ranges.get(1));
    assertEquals(new CharacterRange('f', 'z'), ranges.get(2));
  }

  @Test
  public void getCharsShouldIncludeRangeBoundariesOnly() {
    CharacterRangeSet set = new CharacterRangeSet();
    set.addRange('a', 'c');
    set.addRange('f', 'j');
    set.addChar('x');

    List<Character> chars = set.getChars();
    assertEquals(6, chars.size());
    assertTrue(chars.containsAll(Arrays.asList('a', 'c', 'f', 'j', 'x')));
  }

  @Test
  public void shouldRejectIllegalInitialCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new CharacterRangeSet(0));
    assertThrows(IllegalArgumentException.class, () -> new CharacterRangeSet(-5));
  }
}
