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

import java.util.ArrayList;
import java.util.List;

public class CharsNumTableBuilder {

  private CharacterRangeSet charRangesSet;

  public CharsNumTableBuilder() {
    this.charRangesSet = new CharacterRangeSet();
  }

  public CharsNumTableBuilder addChar(char ch) {
    return this.addCharRange(new CharacterRange(ch, ch));
  }

  public CharsNumTableBuilder addCharRange(CharacterRange range) {
    return this.addCharRange(range.from, range.to);
  }
  ;

  public CharsNumTableBuilder addCharRange(char from, char to) {
    this.charRangesSet.addRange(from, to);
    return this;
  }
  ;

  public ICharsNumTable build() {
    var result = new ArrayList<CharacterRange>();

    var table = this.charRangesSet.sortedCharRanges();
    var tableSize = table.size();
    var i = 0;
    while (i < tableSize) {
      var overlappedRanges = getOverlappedRangesFrom(table, i);
      result.addAll(splitOverlappedRanges(overlappedRanges));
      i += overlappedRanges.size();
    }

    return new CharsNumTableImpl(result);
  }

  protected ArrayList<CharacterRange> getOverlappedRangesFrom(List<CharacterRange> table, int i) {
    var result = new ArrayList<CharacterRange>();
    var prev = table.get(i++);
    result.add(prev);
    while (i < table.size()) {
      var range = table.get(i++);
      if (range.from <= prev.to) {
        result.add(range);
      }
    }
    return result;
  }

  protected ArrayList<CharacterRange> splitOverlappedRanges(List<CharacterRange> ranges) {
    var result = new ArrayList<CharacterRange>();
    var chars = CharsNumTableBuilder.getSortedCharArray(ranges);
    if (chars.size() == 1) {
      var ch = chars.get(0);
      result.add(new CharacterRange(ch, ch));
      return result;
    }
    var prevCh = chars.get(0);
    for (int i = 1; i < chars.size(); i++) {
      var ch = chars.get(i);
      var isInLeftSet = this.charRangesSet.isLeftChar(ch);
      var isInRightSet = this.charRangesSet.isRightChar(ch);
      if (isInLeftSet && isInRightSet) {
        if (prevCh != ch) {
          result.add(new CharacterRange(prevCh, (char) (ch - 1)));
        }
        result.add(new CharacterRange(ch, ch));
        prevCh = (char) (ch + 1);
      } else if (isInLeftSet) {
        result.add(new CharacterRange(prevCh, (char) (ch - 1)));
        prevCh = ch;
      } else if (isInRightSet) {
        result.add(new CharacterRange(prevCh, ch));
        prevCh = (char) (ch + 1);
      }
    }
    return result;
  }

  // Merge and sort and deduplicate chatacter ranges.
  //
  // Input: 1-4 2-6 3-4
  // Output: 1, 2, 3, 4, 6
  protected static ArrayList<Character> getSortedCharArray(List<CharacterRange> ranges) {
    var result = new ArrayList<Character>(ranges.size() * 2);
    var rightChars = ranges.stream().map((r) -> r.to).sorted().toList();
    int lPtr = 0, rPtr = 0, size = ranges.size();
    var prev = ranges.get(0).from - 1;
    while (lPtr < size) {
      char lCh = ranges.get(lPtr).from;
      char rCh = rightChars.get(rPtr);
      char ch;
      if (lCh < rCh) {
        lPtr++;
        ch = lCh;
      } else if (rCh < lCh) {
        rPtr++;
        ch = rCh;
      } else {
        lPtr++;
        rPtr++;
        ch = lCh;
      }
      if (ch != prev) {
        result.add(ch);
        prev = ch;
      }
    }
    while (rPtr < size) {
      char ch = rightChars.get(rPtr++);
      if (ch != prev) {
        result.add(ch);
        prev = ch;
      }
    }
    return result;
  }
}
