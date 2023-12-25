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
package com.nano.regexcv.dfa;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class DfaTest {

  @Test
  public void dfaTest() throws IOException {
    var testCaseArr = RegexTestCase.parseFile("regex_test_cases.txt");
    for (var testCase : testCaseArr) {
      runTestCase(testCase);
    }
  }

  private void runTestCase(RegexTestCase tc) {
    for (var pattern : tc.patterns) {
      testPattern(pattern, true, tc.strsShouldBeMatched);
      testPattern(pattern, false, tc.strsShouldNotBeMatched);
      // Minimized DFA should be ok.
      pattern.minimizeDFA();
      testPattern(pattern, true, tc.strsShouldBeMatched);
      testPattern(pattern, false, tc.strsShouldNotBeMatched);
    }
  }

  private void testPattern(DfaPattern pattern, boolean matched, String[] input) {

    for (String text : input) {
      var actual = pattern.match(text);
      if (actual != matched) {
        var tag = matched ? "SHOULD MATCH" : "SHOULD NOT MATCH";
        var table = pattern.table.getTable();
        var msg =
            String.format(
                "%s; pattern: \"%s\"; text: \"%s\"; minimized: %s; table: %s",
                tag, pattern.pattern, text, pattern.isMinimized, table);
        Assert.fail(msg);
      }
    }
  }
}
