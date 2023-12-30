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

import com.nano.regexcv.IPattern;
import com.nano.regexcv.syntax.RegexParser;
import com.nano.regexcv.table.CharacterSetCollector;
import com.nano.regexcv.table.ICharsNumTable;
import java.util.HashSet;

public class NfaPattern implements IPattern {

  protected Nfa nfa;
  protected ICharsNumTable table;
  protected String pattern;
  private boolean removedEpsilonClosure;

  public NfaPattern(String pattern) {
    this.pattern = pattern;
    this.nfa =
        new RegexParser()
            .next(new CharacterSetCollector())
            .next(new RExpTree2NfaPass())
            .accept(pattern);
    this.table = this.nfa.getCharsNumTable();
    this.removedEpsilonClosure = false;
  }

  public void removeEpsilonClosure() {
    this.removedEpsilonClosure = true;
    this.nfa = new RemoveEpsilonClosurePass().accept(this.nfa);
  }

  @Override
  public boolean matches(String text) {
    return this.matches(nfa.getStart(), new HashSet<>(), text, 0);
  }

  private boolean matches(NfaState state, HashSet<NfaState> marked, String text, int i) {
    marked.add(state);
    for (var succ : state.getEpsilonTransitions()) {
      if (!marked.contains(succ) && matches(succ, marked, text, i)) {
        return true;
      }
    }
    marked.remove(state);
    if (i == text.length()) {
      return state.isFinalState();
    }
    var num = this.table.queryNumOfInputChar(text.charAt(i));
    if (num == ICharsNumTable.INVALID_CHAR_NUM) {
      return false;
    }
    var newMarked = marked.isEmpty() ? marked : new HashSet<NfaState>();
    for (var succ : state.getStateSet(num)) {
      if (matches(succ, newMarked, text, i + 1)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public String getInformation() {
    return String.format(
        "<NFA, /%s/, %s>",
        pattern, removedEpsilonClosure ? "removed epsilon closure" : "with epsilon closure");
  }

  @Override
  public ICharsNumTable getTable() {
    return this.table;
  }
}
