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

public class CharsNumTableTest {

  static final int GET_CHAR_NUM = 0;
  static final int GET_RANGE_NUM = 1;
  static final int QUARY = 2;

  private static class Operation {
    int type;
    String input;
    String expected;

    public Operation(int type, String input, String expected) {
      this.type = type;
      this.input = input;
      this.expected = expected;
    }
  }

  private static class TestCase {
    String input;
    ICharsNumTable table;
    List<Operation> ops;

    public TestCase(String input, List<Operation> ops) {
      this.input = input;
      this.table = createTable(parseInput(input));
      this.ops = ops;
    }
  }

  private static ICharsNumTable createTable(List<CharacterRange> input) {
    var builder = new CharsNumTableBuilder();
    for (var range : input) {
      Assert.assertTrue("Bad input: " + input, range.from <= range.to);
      builder.addCharRange(range);
    }
    return builder.build();
  }

  private static List<CharacterRange> parseInput(String s) {
    return Arrays.stream(s.split(","))
        .map((rangeStr) -> rangeStr.split("-"))
        .map(
            (chs) -> {
              var left = chs[0].charAt(0);
              var right = chs.length == 1 ? left : chs[1].charAt(0);
              return new CharacterRange(left, right);
            })
        .toList();
  }

  private static Operation parseOp(String opStr) {
    var args = opStr.split(";");
    int type =
        switch (args[0]) {
          case "C" -> GET_CHAR_NUM;
          case "R" -> GET_RANGE_NUM;
          case "Q" -> QUARY;
          default -> throw new IllegalArgumentException("Bad input: " + opStr);
        };
    return new Operation(type, args[1], args[2]);
  }

  private static TestCase tcase(String input, String... ops) {
    return new TestCase(input, Arrays.stream(ops).map(CharsNumTableTest::parseOp).toList());
  }

  @Test
  public void testing() {
    final var TEST_CASES =
        new TestCase[] {
          tcase("1-2,2,3-5", "C;2;2", "C;4;-1", "R;3-5;3", "R;1-4;-1", "R;1-5;1,3", "Q;1;1")
        };

    for (var testCase : TEST_CASES) {
      assertTestCase(testCase);
    }
  }

  private static void assertTestCase(TestCase tcase) {
    for (var op : tcase.ops) {
      switch (op.type) {
        case GET_CHAR_NUM:
          {
            testingGetCharNum(tcase, op);
            break;
          }

        case GET_RANGE_NUM:
          {
            testingGetRangeNum(tcase, op);
            break;
          }

        case QUARY:
          {
            testingQuery(tcase, op);
            break;
          }
      }
    }
  }

  private static void testingGetCharNum(TestCase tcase, Operation op) {
    var input = op.input.charAt(0);
    var expected = Integer.valueOf(op.expected).intValue();
    var actual = tcase.table.getNumOfChar(input);
    var errMsg =
        String.format(
            "Input: %s; Op: Getting Char Num; Char Arg: '%s'; Table: %s",
            tcase.input, op.input, tcase.table.getTable());
    Assert.assertEquals(errMsg, expected, actual);
  }

  private static void testingGetRangeNum(TestCase tcase, Operation op) {
    var input = op.input.split("-");
    var range = new CharacterRange(input[0].charAt(0), input[1].charAt(0));
    var actual =
        tcase.table.getNumsOfCharRange(range).orElse(new ICharsNumTable.NumInterval(-1, -1));

    var args = op.expected.split(",");
    ICharsNumTable.NumInterval exceptedInterval;
    if (args.length == 1) {
      var num = Integer.valueOf(args[0]);
      exceptedInterval = new ICharsNumTable.NumInterval(num, num);
    } else {
      var start = Integer.valueOf(args[0]);
      var end = Integer.valueOf(args[1]);
      exceptedInterval = new ICharsNumTable.NumInterval(start, end);
    }

    var errMsg =
        String.format(
            "Input: %s; Op: Getting Char Range Num; Char Range Arg: '%s'; Table: %s",
            tcase.input, op.input, tcase.table.getTable());
    Assert.assertEquals(errMsg, exceptedInterval, actual);
  }

  private static void testingQuery(TestCase tcase, Operation op) {
    var input = op.input.charAt(0);
    var expected = Integer.valueOf(op.expected).intValue();
    var actual = tcase.table.queryNumOfInputChar(input);
    var errMsg =
        String.format(
            "Input: %s; Op: Query Char Num; Char Arg: '%s'; Table: %s",
            tcase.input, op.input, tcase.table.getTable());
    Assert.assertEquals(errMsg, expected, actual);
  }
}
