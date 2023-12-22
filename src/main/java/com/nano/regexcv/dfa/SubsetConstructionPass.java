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

import com.nano.regexcv.Pass;
import com.nano.regexcv.nfa.Nfa;
import com.nano.regexcv.nfa.NfaState;
import com.nano.regexcv.util.CharacterClass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/** This converts a NFA into a DFA. */
public class SubsetConstructionPass implements Pass<Nfa, Dfa> {

  private static class DState {
    protected HashSet<NfaState> nfaSubset;

    public DState(HashSet<NfaState> nfaSubset) {
      this.nfaSubset = nfaSubset;
    }

    public boolean isFinalState() {
      return this.nfaSubset.stream().anyMatch((s) -> s.isFinalState());
    }

    @Override
    public int hashCode() {
      return nfaSubset.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof DState) {
        return nfaSubset.equals(((DState) obj).nfaSubset);
      }
      return false;
    }
  }

  @Override
  public Dfa accept(Nfa nfa) {
    CharacterClass table = nfa.getCharTable();
    LinkedList<DState> stack = new LinkedList<>();
    HashMap<DState, DfaState> dstates = new HashMap<>();
    final int charSetCount = table.getTableSize();

    DState start = new DState(closure(nfa.getStart()));
    dstates.put(start, new DfaState(charSetCount, start.isFinalState()));
    stack.push(start);

    while (!stack.isEmpty()) {
      DState dstate = stack.pop();
      DfaState fromDfaState = dstates.get(dstate);

      for (int in = 1; in <= charSetCount; in++) {
        HashSet<NfaState> U = closure(move(dstate.nfaSubset, in));
        if (U.isEmpty()) {
          continue;
        }
        DState newDState = new DState(U);
        DfaState toDfaState = dstates.get(newDState);
        if (toDfaState == null) {
          toDfaState = new DfaState(charSetCount, newDState.isFinalState());
          dstates.put(newDState, toDfaState);
          stack.push(newDState);
        }
        fromDfaState.addTransition(in, toDfaState);
      }
    }
    return new Dfa(dstates.get(start), table);
  }

  private HashSet<NfaState> closure(NfaState s) {
    HashSet<NfaState> T = new HashSet<>();
    T.add(s);
    return closure(T);
  }

  private HashSet<NfaState> closure(HashSet<NfaState> T) {
    HashSet<NfaState> set = new HashSet<>(T);
    LinkedList<NfaState> stack = new LinkedList<>();
    set.stream().forEach(stack::push);
    while (!stack.isEmpty()) {
      NfaState s = stack.pop();
      for (NfaState u : s.getEpsilonTransitions()) {
        if (set.add(u)) {
          stack.push(u);
        }
      }
    }
    return set;
  }

  private HashSet<NfaState> move(HashSet<NfaState> T, int in) {
    HashSet<NfaState> set = new HashSet<>();
    HashSet<NfaState> marker = new HashSet<>(T);
    LinkedList<NfaState> stack = new LinkedList<NfaState>();
    T.stream().forEach(stack::push);

    while (!stack.isEmpty()) {
      NfaState s = stack.pop();
      Set<NfaState> emptyTransitions = s.getEpsilonTransitions();
      for (NfaState state : emptyTransitions) {
        if (marker.add(state)) {
          stack.push(state);
        }
      }
      for (NfaState state : s.getStateSet(in)) {
        marker.add(state);
        set.add(state);
      }
    }

    return set;
  }
}
