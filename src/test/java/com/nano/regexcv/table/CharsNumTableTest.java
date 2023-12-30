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
import com.nano.regexcv.util.CharacterRanges;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CharsNumTableTest {

  static final TestCase[] TEST_CASES =
      new TestCase[] {
        tcase("", "", "GET CH;    a;   -1", "GET RANGE; a-b; -1", "QUERY;     a;   -1"),
        tcase(
            "a",
            "a",
            "GET CH;    a;   1",
            "GET RANGE; a-a; 1",
            "GET RANGE; a-b; -1",
            "QUERY;     b;   -1",
            "QUERY;     a;   1"),
        tcase(
            "1-2,	2, 3-5",
            "1,	2, 3-5",
            "GET CH;    2;   2",
            "GET CH;    4;   -1",
            "GET RANGE; 3-5; 3",
            "GET RANGE; 1-4; -1",
            "GET RANGE; 1-5; 1-3",
            "QUERY;     1;   1"),
        tcase(
            "1-2",
            "1-2",
            "GET CH;    2;   -1",
            "GET CH;    1;   -1",
            "GET RANGE; 1-2; 1",
            "QUERY;     1;   1",
            "QUERY;     3;   -1",
            "QUERY;     2;   1"),
        tcase(
            "6, 2-5, 2, 7-8, 1-3",
            "1, 2, 3, 4-5, 6, 7-8",
            "GET CH;    1;   1",
            "GET CH;    2;   2",
            "GET CH;    3;   3",
            "GET RANGE; 1-3; 1-3",
            "GET RANGE; 4-5; 4",
            "GET RANGE; 2-5; 2-4",
            "GET CH;    6;   5",
            "GET RANGE; 7-8; 6",
            "QUERY;     1;   1",
            "QUERY;     2;   2",
            "QUERY;     3;   3",
            "QUERY;     8;   6"),
        tcase(
            "e-f, a-f, a-d, d, c-h, h, a",
            "a, b, c, d, e-f, g, h",
            "GET CH;    d;   4",
            "GET CH;    h;   7",
            "GET RANGE; a-d; 1-4",
            "GET RANGE; c-h; 3-7",
            "GET RANGE; e-f; 5",
            "GET RANGE; a-f; 1-5",
            "QUERY;     a;   1",
            "QUERY;     e;   5")
      };

  private static TestCase tcase(String input, String expectedTable, String... ops) {
    return new TestCase(input, expectedTable, ops);
  }

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
    List<CharacterRange> expectedTable;
    List<Operation> ops;

    public TestCase(String input, String expectedTable, String[] operations) {
      this.input = input;
      var builder = new CharsNumTableBuilder();
      for (var range : parseRanges(input)) {
        builder.addCharRange(range);
      }
      this.table = builder.build();
      this.expectedTable = parseRanges(expectedTable);
      this.ops = Arrays.stream(operations).map(opStr -> parseOperation(opStr)).toList();
    }
  }

  private static List<CharacterRange> parseRanges(String s) {
    return Arrays.stream(s.split(","))
        .map(item -> item.trim())
        .filter(item -> !item.isEmpty())
        .map(item -> CharacterRanges.parse(item)[0])
        .toList();
  }

  private static Operation parseOperation(String opStr) {
    var args = opStr.split(";");
    int type =
        switch (args[0].trim()) {
          case "GET CH" -> GET_CHAR_NUM;
          case "GET RANGE" -> GET_RANGE_NUM;
          case "QUERY" -> QUARY;
          default -> throw new IllegalArgumentException("Bad input: " + opStr);
        };
    return new Operation(type, args[1].trim(), args[2].trim());
  }

  @Test
  public void testTableOperations() {
    for (var testCase : TEST_CASES) {
      assertTestCase(testCase);
    }
  }

  private static void assertTestCase(TestCase tcase) {
    Assert.assertEquals(tcase.expectedTable, tcase.table.getTable());
    for (var operation : tcase.ops) {
      switch (operation.type) {
        case GET_CHAR_NUM:
          {
            testGetCharNum(tcase, operation);
            break;
          }

        case GET_RANGE_NUM:
          {
            testGetRangeNum(tcase, operation);
            break;
          }

        case QUARY:
          {
            testQuery(tcase, operation);
            break;
          }
      }
    }
  }

  private static void testGetCharNum(TestCase tcase, Operation op) {
    var input = op.input.charAt(0);
    var expected = Integer.valueOf(op.expected).intValue();
    var actual = tcase.table.getNumOfChar(input);
    var errMsg =
        String.format(
            "Input: %s; Op: Getting Char Num; Char Arg: '%s'; Table: %s",
            tcase.input, op.input, tcase.table.getTable());
    Assert.assertEquals(errMsg, expected, actual);
  }

  private static void testGetRangeNum(TestCase tcase, Operation op) {
    var input = op.input.split("-");
    var range = new CharacterRange(input[0].charAt(0), input[1].charAt(0));
    var actual =
        tcase.table.getNumsOfCharRange(range).orElse(new ICharsNumTable.NumInterval(-1, -1));

    var args = op.expected.split("-");
    ICharsNumTable.NumInterval exceptedInterval;
    if (args.length == 1) {
      var num = Integer.valueOf(args[0]);
      exceptedInterval = new ICharsNumTable.NumInterval(num, num);
    } else {
      if (args[0].isEmpty()) {
        exceptedInterval = new ICharsNumTable.NumInterval(-1, -1);
      } else {
        var start = Integer.valueOf(args[0]);
        var end = Integer.valueOf(args[1]);
        exceptedInterval = new ICharsNumTable.NumInterval(start, end);
      }
    }

    var errMsg =
        String.format(
            "Input: %s; Op: Getting Char Range Num; Char Range Arg: '%s'; Table: %s",
            tcase.input, op.input, tcase.table.getTable());
    Assert.assertEquals(errMsg, exceptedInterval, actual);
  }

  private static void testQuery(TestCase tcase, Operation op) {
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
