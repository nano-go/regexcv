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
package com.nano.regexcv;

import com.nano.regexcv.dfa.Dfa;
import com.nano.regexcv.dfa.DfaMinimizer;
import com.nano.regexcv.dfa.DfaState;
import com.nano.regexcv.dfa.SubsetConstructionPass;
import com.nano.regexcv.nfa.RExpTree2NfaPass;
import com.nano.regexcv.syntax.RegexParser;
import com.nano.regexcv.util.ICharsNumTable;
import org.junit.Assert;
import org.junit.Test;

public class DfaTest {

  private static class MPattern {
    private Dfa dfa;
    private ICharsNumTable table;

    private String pattern;

    private boolean isMinimized;

    public MPattern(String pattern) {
      this.dfa =
          new RegexParser()
              .next(new RExpTree2NfaPass())
              .next(new SubsetConstructionPass())
              .accept(pattern);
      this.table = dfa.getCharsNumTable();
      this.pattern = pattern;
    }

    public void minimizePattern() {
      this.dfa = new DfaMinimizer().accept(this.dfa);
      this.isMinimized = true;
    }

    public boolean match(String text) {
      DfaState s = dfa.getStart();
      char[] chs = text.toCharArray();
      for (int i = 0; i < chs.length; i++) {
        char ch = chs[i];
        int classNumber = table.queryNumOfInputChar(ch);
        if (classNumber == ICharsNumTable.INVALID_CHAR_NUM) {
          return false;
        }
        s = s.getState(classNumber);
        if (s == null) {
          return false;
        }
      }
      return s.isFinalState();
    }

    @Override
    public String toString() {
      return String.format("Minimized DFA Enable: %s,  Pattern: <%s>", isMinimized, pattern);
    }
  }

  public static final String[][][] TEST_CASES = {
    {
      // Patterns Macthed strings Unmacthed stringss.
      {"", "[]", "()"}, {""}, {"abc", "a", "b"}
    },
    {{"ab"}, {"ab"}, {"a", "b", "ac", "acb"}},
    {{"aaaa"}, {"aaaa"}, {"aaa", "aaaaa", ""}},
    {{"a"}, {"a"}, {"ab", "b", "cde"}},
    {{"a*"}, {"", "a", "aaa"}, {"ab", "ac", "d", "db"}},
    {{"aa*"}, {"a", "aa", "aaa"}, {"", "ab", "aaab"}},
    {{"a*abb"}, {"abb", "aaaaaabb", "aaabb"}, {"ab", "bb", "aaab"}},
    {
      {"(a|b)*abb", "[ab]*abb"},
      {"ababaabb", "ababaababb", "abb", "aaaaaaaabb", "bbbbbbbbabb"},
      {"ababaa", "ab", "abbb", "ybb", ""}
    },
    {
      {"(a|b)*abb*"},
      {"bbbbbbbbabbbbbbbbbbbbbbbbbbbbab", "ababababbbbbb", "aaaab", "ab", "bab"},
      {"", "a", "b", "ba", "bba", "bbbba"}
    },
    {{"ab*c"}, {"ac", "abc", "abbbbbc"}, {"ad", "abe", "acf", "abbbbbbbe"}},
    {{"ab+c"}, {"abc", "abbc"}, {"ac", "abbb", "abbbd", "ed"}},
    {{"gr(e|a)y", "gr[ea]y"}, {"gray", "grey"}, {"graay", "gery", "ygar"}},
    {{"colou?r"}, {"color", "colour"}, {"coloor", "ccolor", "rcolou"}},
    {{"(aa)|(bb)"}, {"aa", "bb"}, {"a", "b", "ab", "aabb", "a"}},
    {{"(a|b|c|d)", "[abcd]"}, {"a", "b", "c", "d"}, {"", "e"}},
    {
      {"[ab]*([e-fk]+|[e-g]+)?[0-9]+(123)?"},
      {"aaaabbbbbab999123", "fkfkfef123123", "ababbegeg010520"},
      {"", "abbabbbgggk010123", "123a", "abbbkkkk0112a"}
    },
    {{"(aaaaa*)|(bbbbb*)"}, {"aaaaaaaa", "aaaa", "bbbbbb", "bbbb"}, {"", "a", "b", "aaa", "bbb"}},
    {
      {"[ \n\f\t\r]*", "\\s*", "\\s*|\\s*"},
      {"", " \n\f\t\r", "    \n", " \n\f\t\r \n\f\t\r", "    "},
      {"      a", " \n\f\t\rb", "b \n\f\t\r", "    \r*"}
    },
    {
      {"\\w+", "[a-zA-Z0-9]+"},
      {
        "abcdefghijklmnopqrstuvwsyz",
        "abcdefghijklmnopqrstuvwsyz".toUpperCase(),
        "0123456789abcdefg",
        "1",
        "a",
        "b"
      },
      {"a*", "#$%", "", "abcdefg012345-"}
    },
    {{"\\d*", "[0-9]*"}, {"", "0123456789"}, {"0001a", "a", "00a", "45%"}},
    {{"\\n\\f\\r\\t\\b", "\n\f\r\t\b"}, {"\n\f\r\t\b"}, {"", "\n\f\r\t\ba", "a\n\f\r\t\b"}},
    {
      {"\\+\\*\\?\\[\\]\\(\\)\\\\"},
      {"+*?[]()\\"},
      {"", "+*?[]()\\b", "b+*?[]()\\", "\\\\\\\\", "++++", "****", "???", "[[[", "]]]"}
    },
    {{"[a-zA-Z]*"}, {"", "ABCdedg", "aaaa", "zzzzZZ"}, {"a0", "z1", "j("}},
    {{"[+*?\\()]*"}, {"+*?\\()", ""}, {"[]", "{}"}},
    {
      {"[a-zA-Z_$](\\w|[_$])*"},
      {"_getItem", "setItem", "$Class007", "FooBar007", "a"},
      {"", "0abcdefg", "007"},
    },
    {
      {"<\\w+>.*</\\w+>"},
      {
        "<p>It's been a long day without my friend.</p>",
        "<abc></abc>",
        "<p>咕噜咕噜</p>",
        "<p></p>",
        "<a><<<<</a>"
      },
      {"<p>abc<p>", "<p>abc</>", "<>abc</>", "<a></>", "<a></////>"}
    },
    {{"(哀民生之多艰)*"}, {"", "哀民生之多艰", "哀民生之多艰哀民生之多艰"}, {"A", "哀民生之多", "哀哀哀哀哀"}}
  };

  @Test
  public void dfaMatchTest() {
    for (String[][] testCase : TEST_CASES) {
      String[] patterns = testCase[0];
      String[] matched = testCase[1];
      String[] unmatched = testCase[2];
      for (String pattern : patterns) {
        matchTest(pattern, matched, unmatched);
      }
    }
  }

  private void matchTest(String pattern, String[] matched, String[] unmatched) {
    MPattern mp = new MPattern(pattern);
    matches(mp, matched, unmatched);

    // Minimized DFA test.
    mp.minimizePattern();
    // Match again
    matches(mp, matched, unmatched);
  }

  private void matches(MPattern pattern, String[] matched, String[] unmatched) {
    for (String text : matched) {
      Assert.assertTrue(
          String.format(
              "Table: %s; Expect the text '%s' is matched by the pattern '%s'.",
              pattern.table.getTable(), text, pattern),
          pattern.match(text));
    }

    for (String text : unmatched) {
      Assert.assertFalse(
          String.format(
              "Table: %s; Don't expect the text '%s' is matched by the pattern '%s'.",
              pattern.table.getTable(), text, pattern),
          pattern.match(text));
    }
  }
}
