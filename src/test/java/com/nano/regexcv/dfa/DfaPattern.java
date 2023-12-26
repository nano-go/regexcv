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

import com.nano.regexcv.nfa.RExpTree2NfaPass;
import com.nano.regexcv.syntax.RegexParser;
import com.nano.regexcv.table.CharacterSetCollector;
import com.nano.regexcv.table.ICharsNumTable;

public class DfaPattern {
  protected Dfa dfa;
  protected ICharsNumTable table;
  protected String pattern;
  protected boolean isMinimized;

  public DfaPattern(String pattern) {
    this.dfa =
        new RegexParser()
            .next(new CharacterSetCollector())
            .next(new RExpTree2NfaPass())
            .next(new SubsetConstructionPass())
            .accept(pattern);
    this.table = dfa.getCharsNumTable();
    this.pattern = pattern;
  }

  public void minimizeDFA() {
    this.dfa = new DfaMinimizer().accept(this.dfa);
    this.isMinimized = true;
  }

  public boolean match(String text) {
    DfaState state = dfa.getStart();
    for (var ch : text.toCharArray()) {
      int classNumber = table.queryNumOfInputChar(ch);
      if (classNumber == ICharsNumTable.INVALID_CHAR_NUM) {
        return false;
      }
      state = state.getState(classNumber);
      if (state == null) {
        return false;
      }
    }
    return state.isFinalState();
  }
}
