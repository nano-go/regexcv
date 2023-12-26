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
package com.nano.regexcv.table;

import com.nano.regexcv.util.CharacterRange;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CharsNumTableBuilderTest {

  private static List<CharacterRange> parse(String s) {
    return Arrays.stream(s.split(","))
        .map((rangeStr) -> rangeStr.split("-"))
        .map((chs) -> new CharacterRange(chs[0].charAt(0), chs[1].charAt(0)))
        .toList();
  }

  private static CharsNumTableBuilder newBuilder(List<CharacterRange> ranges) {
    var builder = new CharsNumTableBuilder();
    for (var range : ranges) {
      builder.addCharRange(range);
    }
    return builder;
  }

  @Test
  public void testingGetSortedCharArray() {
    final var TEST_CASES =
        new String[][] {
          {"1-2", "1,2"},
          {"1-2,1-2", "1,2"},
          {"1-4,2-6,3-4", "1,2,3,4,6"},
          {"1-1,2-2,3-3", "1,2,3"},
          {"1-5,2-9,6-8", "1,2,5,6,8,9"},
          {"1-5,2-9,6-8,7-7", "1,2,5,6,7,8,9"},
          {"1-8,2-4,3-4,3-5,5-8", "1,2,3,4,5,8"},
          {"1-9,2-5,3-5,4-5,5-5,6-8,7-8", "1,2,3,4,5,6,7,8,9"},
        };

    for (var testCase : TEST_CASES) {
      var ranges = parse(testCase[0]);
      var expected = Arrays.stream(testCase[1].split(",")).map(str -> str.charAt(0)).toList();
      Assert.assertEquals(expected, CharsNumTableBuilder.getSortedCharArray(ranges));
    }
  }

  @Test
  public void testingSplitOverlappedRanges() {
    final var TEST_CASES =
        new String[][] {
          {"1-2", "1-2"},
          {"1-1", "1-1"},
          {"1-1,2-2,3-3", "1-1,2-2,3-3"},
          {"1-2,2-3", "1-1,2-2,3-3"},
          {"1-5,2-3", "1-1,2-3,4-5"},
          {"1-3,2-2,3-4", "1-1,2-2,3-3,4-4"},
          {"1-4,2-6,4-5,6-9", "1-1,2-3,4-4,5-5,6-6,7-9"},
          {"a-g,d-k,e-g", "a-c,d-d,e-g,h-k"},
          {"a-f,e-k,g-l", "a-d,e-f,g-k,l-l"},
          {"a-b,a-a,a-e", "a-a,b-b,c-e"}
        };

    for (var testCase : TEST_CASES) {
      var arg = parse(testCase[0]);
      var builder = newBuilder(arg);
      var actual = builder.splitOverlappedRanges(arg);
      var expected = parse(testCase[1]);
      Assert.assertEquals(expected, actual);
    }
  }
}
