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

import com.nano.regexcv.util.CharacterRange;

public class RCharRangeList extends RegularExpression {

  private CharacterRange[] charRanges;

  public RCharRangeList(CharacterRange[] charRanges) {
    this.charRanges = charRanges;
  }

  @Override
  public Nfa generateNfa() {
    NfaState start = new NfaState(getMaxClassNumber());
    NfaState end = new NfaState(getMaxClassNumber());
    for (CharacterRange range : charRanges) {
      int[] classNums = getCharClass().getClassNumberRange(range.getFrom(), range.getTo());
      for (int i : classNums) start.addTransition(i, end);
    }
    if (charRanges.length == 0) {
      start.addEmptyTransition(end);
    }
    return new Nfa(start, end);
  }
}
