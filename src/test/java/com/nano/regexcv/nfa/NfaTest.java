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
package com.nano.regexcv.nfa;

import com.nano.regexcv.RegexTestCase;
import java.io.IOException;
import org.junit.Test;

public class NfaTest {

  @Test
  public void nfaTest() throws IOException {
    var testCaseArr = RegexTestCase.parseFile("regex_test_cases.txt");
    for (var testCase : testCaseArr) {
      runTestCase(testCase);
    }
  }

  private void runTestCase(RegexTestCase tc) {
    for (var pattern : tc.patterns) {
      var nfaPattern = new NfaPattern(pattern);
      nfaPattern.test(true, tc.strsShouldBeMatched);
      nfaPattern.test(false, tc.strsShouldNotBeMatched);
      nfaPattern.removeEpsilonClosure();
      nfaPattern.test(true, tc.strsShouldBeMatched);
      nfaPattern.test(false, tc.strsShouldNotBeMatched);
    }
  }
}
