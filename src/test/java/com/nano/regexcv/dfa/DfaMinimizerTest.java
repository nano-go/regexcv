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

import static org.junit.Assert.*;

import com.nano.regexcv.nfa.RExpTree2NfaPass;
import com.nano.regexcv.syntax.RegexParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class DfaMinimizerTest {

  public static final String[] DFA_MIN_TEST_CASES = {
    // Format: Number: Regex...
    // The number of nodes of minimized DFA.
    "4: (abc)*abb+",
    "5: (from)|(frog)",
    "3: (a|b)*abb*",
    "1: ((a|b)*)|([abc]+)|(abb*)",
    "5: abcd",
    "1: "
  };

  private static final Pattern TC_PATTERN = Pattern.compile("([0-9]+): (.*)");

  @Test
  public void dfaMinTest() {
    for (String tc : DFA_MIN_TEST_CASES) {
      Matcher matcher = TC_PATTERN.matcher(tc);
      if (!matcher.find()) {
        fail(tc + ": No match found.");
      }
      int numNodes = Integer.valueOf(matcher.group(1));
      String regex = matcher.group(2).trim();
      dfaMinTest(numNodes, regex);
    }
  }

  private void dfaMinTest(int expectedMinNodes, String regex) {
    Dfa dfa = getMinimizedDfa(regex);
    assertEquals("<" + regex + ">: ", expectedMinNodes, dfa.getAllStates().length);
  }

  private Dfa getMinimizedDfa(String regex) {
    return new RegexParser()
        .next(new RExpTree2NfaPass())
        .next(new SubsetConstructionPass())
        .next(new DfaMinimizer())
        .accept(regex);
  }
}
