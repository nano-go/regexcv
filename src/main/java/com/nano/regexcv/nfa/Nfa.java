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

import com.nano.regexcv.util.ICharsNumTable;

public class Nfa {

  protected ICharsNumTable table;
  protected NfaState start, end;

  public Nfa(ICharsNumTable table) {
    this.table = table;
    this.start = new NfaState(table.getTableSize());
    this.end = new NfaState(table.getTableSize());
    this.end.markFinalState();
  }

  public ICharsNumTable getCharTable() {
    return table;
  }

  public NfaState getStart() {
    return start;
  }

  public NfaState getEnd() {
    return end;
  }
}
