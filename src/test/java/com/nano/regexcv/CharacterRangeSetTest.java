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
package com.nano.regexcv;

import static org.junit.Assert.*;

import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharacterRangeSet;
import java.util.HashSet;
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
}
