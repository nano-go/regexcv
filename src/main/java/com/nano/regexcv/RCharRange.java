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

public class RCharRange extends RegularExpression {

  private char from, to;

  public RCharRange(char from, char to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public Nfa generateNfa() {
    NfaState start = new NfaState(getMaxClassNumber());
    NfaState end = new NfaState(getMaxClassNumber());
    int[] charClassRange = getCharClass().getClassNumberRange(from, to);
    for (int i = charClassRange[0]; i <= charClassRange[1]; i++) {
      start.addTransition(i, end);
    }
    return new Nfa(start, end);
  }
}
