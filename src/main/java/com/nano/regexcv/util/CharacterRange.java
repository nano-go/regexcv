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
import java.util.Arrays;
import java.util.LinkedList;

public class CharacterRange implements Comparable<CharacterRange>, Digraph.Acception {

  public static final CharacterRange EPSILON = new CharacterRange('ε', 'ε');

  public static final CharacterRange RANGE_a_z = new CharacterRange('a', 'z');
  public static final CharacterRange RANGE_A_Z = new CharacterRange('A', 'Z');
  public static final CharacterRange RANGE_0_9 = new CharacterRange('0', '9');
  public static final CharacterRange RANGE_ANY = new CharacterRange((char) 0, Character.MAX_VALUE);

  public static int hashCode(char from, char to) {
    return from * 31 + to;
  }

  /**
   * Use binary-search algorithm to find a character range that contains the specified character
   * 'ch'.
   *
   * @param arr The sorted list.
   * @param ch The specified character.
   * @return The index of the character range that contains the given char or -1 if the char is out
   *     of the character range list.
   */
  public static int binarySearch(ArrayList<CharacterRange> arr, char ch) {
    var l = 0;
    var r = arr.size() - 1;

    while (r >= l) {
      var mid = l + ((r - l) >> 1);
      var chRange = arr.get(mid);
      if (chRange.contains(ch)) {
        return mid;
      }

      if (ch > chRange.to) {
        l = mid + 1;
      } else {
        r = mid - 1;
      }
    }

    return -1;
  }

  public static CharacterRange[] mergeOverlappedRanges(CharacterRange... ranges) {
    if (ranges.length == 0) {
      return new CharacterRange[0];
    }
    Arrays.sort(ranges);
    var result = new LinkedList<CharacterRange>();
    var left = ranges[0].from;
    var right = ranges[0].to;
    for (int i = 1; i < ranges.length; i++) {
      var range = ranges[i];
      if (range.from > right) {
        result.add(new CharacterRange(left, right));
        left = range.from;
      }
      right = right > range.to ? right : range.to;
    }
    result.add(new CharacterRange(left, right));
    return result.toArray(CharacterRange[]::new);
  }

  public static CharacterRange[] inversedRanges(CharacterRange... ranges) {
    var result = new ArrayList<>(ranges.length + 1);
    var left = Character.MIN_VALUE;
    for (var range : ranges) {
      if (left < range.from) {
        result.add(new CharacterRange(left, (char) (range.from - 1)));
      }
      if (range.to == Character.MAX_VALUE) {
        return result.toArray(CharacterRange[]::new);
      }
      left = (char) (range.to + 1);
    }
    result.add(new CharacterRange(left, Character.MAX_VALUE));
    return result.toArray(CharacterRange[]::new);
  }

  protected static String charToString(char ch) {
    switch (ch) {
      case '\n':
        return "\\\\n";
      case '\r':
        return "\\\\r";
      case '\f':
        return "\\\\f";
      case '\b':
        return "\\\\b";
      case '\t':
        return "\\\\t";
      case '|':
        return "\\\\|";
      case ' ':
        return "space";
    }
    if (!(ch < 127 && ch >= 0x21) && (ch == 0 || !Character.isUnicodeIdentifierPart(ch))) {
      return "0x" + Integer.toHexString(ch);
    }
    return String.valueOf(ch);
  }

  public final char from;
  public final char to;

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

  public boolean contains(char ch) {
    return ch >= from && ch <= to;
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
      label = String.format("%s-%s", charToString(from), charToString(to));
    }
    return label;
  }
}
