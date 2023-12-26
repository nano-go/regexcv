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

import com.nano.regexcv.table.ICharsNumTable;
import java.util.ArrayList;
import java.util.HashSet;

public class Dfa {

  /** Visible for DfaMinimizer. */
  protected DfaState start;

  protected ICharsNumTable table;

  public Dfa(DfaState start, ICharsNumTable table) {
    this.start = start;
    this.table = table;
  }

  public ICharsNumTable getCharsNumTable() {
    return this.table;
  }

  public DfaState getStart() {
    return start;
  }

  public DfaState[] getAllStates() {
    ArrayList<DfaState> list = new ArrayList<>();
    HashSet<DfaState> marker = new HashSet<>();
    list.add(this.start);
    marker.add(this.start);
    int i = 0;
    while (i < list.size()) {
      DfaState dstate = list.get(i);
      for (DfaState to : dstate.getAllTransitions()) {
        if (to == null) continue;
        if (!marker.contains(to)) {
          list.add(to);
          marker.add(to);
        }
      }
      i++;
    }
    return list.toArray(new DfaState[list.size()]);
  }
}
