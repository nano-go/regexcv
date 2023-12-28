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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** This class contains various useful methods for multiple character ranges. */
public class CharacterRanges {

  /**
   * Parses a string like character class in regex to an array of character range.
   *
   * <p>For Example:
   *
   * <pre>{@code
   * Input: "a-z0-9", Output: [(a, z), (0, 9)]
   * Input: "a-z52", Output: [(a, z), (5, 5), (2, 2)]
   * Input: "a\-b", Output: [(a, a), (-, -), (b, b)]
   * Input: "a-", Output: [(a, a), (-, -)]
   * }</pre>
   *
   * The start character of a range must be greater than or equal to the end character.
   */
  public static CharacterRange[] parse(String ranges) {
    var result = new ArrayList<CharacterRange>();
    var chars = ranges.toCharArray();
    var len = ranges.length();
    var i = 0;
    while (i < len) {
      char start = chars[i];
      if (start == '\\') {
        if (i == len - 1) {
          throw new IllegalArgumentException();
        }
        var next = chars[++i];
        if (next != '\\' || next != '-') {
          throw new IllegalArgumentException();
        }
        start = next;
      }

      if (i + 2 >= len || chars[i + 1] != '-') {
        i++;
        result.add(new CharacterRange(start, start));
        continue;
      }

      char end = chars[i + 2];
      if (start > end) {
        throw new IllegalArgumentException();
      }
      result.add(new CharacterRange(start, end));
      i += 3;
    }
    return result.toArray(CharacterRange[]::new);
  }

  /**
   * Use binary-search algorithm to search a character range that contains the specified character
   * 'ch'.
   *
   * @param arr The array must be in ascending order.
   * @param ch The specified character.
   * @return The index of the character range that contains the given char or -1 if it does not
   *     exist in the list.
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

  /**
   * Use binary-search algorithm to search a character range that contains the specified character
   * 'ch'.
   *
   * @param arr The array must be in ascending order.
   * @param ch The specified character.
   * @return The index of the character range that contains the given char or -1 if it does not
   *     exist in the array.
   */
  public static int binarySearch(CharacterRange[] arr, char ch) {
    var l = 0;
    var r = arr.length - 1;

    while (r >= l) {
      var mid = l + ((r - l) >> 1);
      var chRange = arr[mid];
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

  /**
   * Merge overlapping character ranges.
   *
   * @param ranges The range array is in any order.
   * @return The array with only mutually exclusive ranges.
   */
  public static CharacterRange[] mergeOverlappingRanges(CharacterRange... ranges) {
    return mergeOverlappingRanges(false, ranges);
  }

  /**
   * Merge overlapping character ranges.
   *
   * @param inAscOrder Is the range array in ascending order?
   * @return The array with only mutually exclusive ranges.
   */
  public static CharacterRange[] mergeOverlappingRanges(
      boolean inAscOrder, CharacterRange... ranges) {
    if (ranges.length == 0) {
      return new CharacterRange[0];
    }
    if (!inAscOrder) {
      Arrays.sort(ranges);
    }
    var result = new ArrayList<CharacterRange>(ranges.length);
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

  /**
   * Inverse the specified ranges.
   *
   * <p>For example, given an array {@code [a-z0-9]}, and outputs an array that contains any
   * character except {@code [a-z0-9]}
   *
   * @param rangesInAscOrder The ranges must be in ascending order.
   * @return The array list with inversed ranges.
   */
  public static ArrayList<CharacterRange> inverseRanges(List<CharacterRange> rangesInAscOrder) {
    var result = new ArrayList<CharacterRange>(rangesInAscOrder.size() + 1);
    var left = Character.MIN_VALUE;
    for (var range : rangesInAscOrder) {
      if (left < range.from) {
        result.add(new CharacterRange(left, (char) (range.from - 1)));
      }
      if (range.to == Character.MAX_VALUE) {
        return result;
      }
      left = (char) (range.to + 1);
    }
    result.add(new CharacterRange(left, Character.MAX_VALUE));
    return result;
  }

  /**
   * Inverse the specified ranges.
   *
   * <p>For example, given an array {@code [a-z0-9]}, and outputs an array that contains any
   * character except {@code [a-z0-9]}
   *
   * @param rangesInAscOrder The ranges must be in ascending order.
   * @return The array with inversed ranges.
   */
  public static CharacterRange[] inverseRanges(CharacterRange... rangesInAscOrder) {
    var result = new ArrayList<>(rangesInAscOrder.length + 1);
    var left = Character.MIN_VALUE;
    for (var range : rangesInAscOrder) {
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

  /**
   * Inverse the specified ranges.
   *
   * <p>For example, given an array {@code [a-z0-9]}, and outputs an array that contains any
   * character except {@code [a-z0-9]}
   *
   * @param ranges The ranges to be inversed.
   * @return The hash set with inversed ranges.
   */
  public static HashSet<CharacterRange> inverseRanges(Set<CharacterRange> ranges) {
    var rangesInAscOrder = ranges.stream().sorted().iterator();
    var result = new HashSet<CharacterRange>();
    var left = Character.MIN_VALUE;
    while (rangesInAscOrder.hasNext()) {
      var range = rangesInAscOrder.next();
      if (left < range.from) {
        result.add(new CharacterRange(left, (char) (range.from - 1)));
      }
      if (range.to == Character.MAX_VALUE) {
        return result;
      }
      left = (char) (range.to + 1);
    }
    result.add(new CharacterRange(left, Character.MAX_VALUE));
    return result;
  }
}
