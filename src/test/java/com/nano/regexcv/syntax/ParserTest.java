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
package com.nano.regexcv.syntax;

import static org.junit.Assert.*;

import com.nano.regexcv.syntax.tree.RAlternation;
import com.nano.regexcv.syntax.tree.RContatenation;
import com.nano.regexcv.syntax.tree.RegularExpression;
import org.junit.Test;

public class ParserTest {

  public static final String[] ERROR_REGEX = {
    "[a-", "[z-a]", "[a-b", "[", "[w-\\q]", "(ab", "((ab)", "(ab))", "\\Poab", "a**", "*", "c++",
    "+", "a??", "?", "a|*", "a|+", "a|?", ")"
  };

  @Test(timeout = 2000)
  public void expectedSyntaxError() {
    RegexParser parser = new RegexParser();
    for (String pattern : ERROR_REGEX) {
      try {
        parser.accept(pattern);
      } catch (RegexSyntaxErrorException e) {
        System.out.printf("\"%s\": %s\n", pattern, e.getMessage());
        continue;
      }
      fail("Expected syntax error: " + pattern);
    }
  }

  @Test(timeout = 2000)
  public void parseAlternationContainingConcatenations() {
    RegexParser parser = new RegexParser();

    RegularExpression regex = parser.accept("ab|cd");
    assertTrue(regex instanceof RAlternation);

    RAlternation alternation = (RAlternation) regex;
    assertEquals(2, alternation.getRegexList().size());
    assertTrue(alternation.getRegexList().get(0) instanceof RContatenation);
    assertTrue(alternation.getRegexList().get(1) instanceof RContatenation);

    RContatenation left = (RContatenation) alternation.getRegexList().get(0);
    RContatenation right = (RContatenation) alternation.getRegexList().get(1);
    assertEquals(2, left.getRegexList().size());
    assertEquals(2, right.getRegexList().size());
  }
}
