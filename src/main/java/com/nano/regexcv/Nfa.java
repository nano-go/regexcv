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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Nfa {

  protected NfaState start, end;

  public Nfa(NfaState start, NfaState end) {
    this.start = start;
    this.end = end;
  }

  public NfaState getStart() {
    return start;
  }

  public NfaState getEnd() {
    return end;
  }

  public void removeEpsilonClosure() {
    LinkedList<NfaState> stack = new LinkedList<NfaState>();
    HashSet<NfaState> marked = new HashSet<>();
    stack.add(start);
    while (!stack.isEmpty()) {
      NfaState node = stack.pop();
      marked.add(node);
      removeEpsilonClosure(node);
      Arrays.stream(node.getTransitions())
          .filter(e -> e != null)
          .flatMap(e -> e.stream())
          .filter(e -> !marked.contains(e))
          .forEach(stack::push);
    }
    // The end state may not exist after the ε-closure is removed.
    this.end = null;
  }

  /** Remove the set of states which can be reached from the given state with only 'ε'. */
  private void removeEpsilonClosure(NfaState from) {
    boolean foundEndNode = false;
    LinkedList<NfaState> stack = new LinkedList<NfaState>(from.getEpsilonTransitions());
    HashSet<NfaState> marked = new HashSet<>();
    marked.add(from);

    while (!stack.isEmpty()) {
      NfaState node = stack.pop();
      foundEndNode |= node == end;
      marked.add(node);
      for (NfaState nstate : node.getEpsilonTransitions()) {
        if (!marked.contains(nstate)) {
          stack.push(nstate);
        }
      }

      // If A['ε'] = B, B['a'] = C, we can connect the state A and
      // the state C with the char 'a'.
      HashSet<NfaState>[] transitions = node.getTransitions();
      for (int i = 1; i < transitions.length; i++) {
        if (transitions[i] != null) {
          from.addTransitions(i, transitions[i]);
        }
      }
    }
    from.removeEpsilonTransitions();
    if (foundEndNode) from.markFinalState();
  }
}
