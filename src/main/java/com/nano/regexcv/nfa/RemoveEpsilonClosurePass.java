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

import com.nano.regexcv.Pass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class RemoveEpsilonClosurePass implements Pass<Nfa, Nfa> {

  @Override
  public Nfa accept(Nfa nfa) {
    var stack = new LinkedList<NfaState>();
    var marked = new HashSet<>();
    stack.add(nfa.start);
    marked.add(nfa.start);
    while (!stack.isEmpty()) {
      var node = stack.pop();
      removeEpsilonClosure(node);
      Arrays.stream(node.getTransitions())
          .filter(e -> e != null)
          .flatMap(e -> e.stream())
          .filter(marked::add)
          .forEach(stack::push);
    }
    // The end state may not exist after the ε-closure is removed.
    nfa.end = null;
    return nfa;
  }

  private void removeEpsilonClosure(NfaState from) {
    boolean isFinalState = false;
    var stack = new LinkedList<NfaState>(from.getEpsilonTransitions());
    var marked = new HashSet<>(from.getEpsilonTransitions());
    marked.add(from);

    while (!stack.isEmpty()) {
      var node = stack.pop();
      isFinalState |= node.isFinalState();
      var transitions = node.getTransitions();
      for (int i = 1; i < transitions.length; i++) {
        if (transitions[i] != null) {
          from.addTransitions(i, transitions[i]);
        }
      }
      node.getEpsilonTransitions().stream().filter(marked::add).forEach(stack::push);
    }

    if (isFinalState) {
      from.markFinalState();
    }
    from.removeEpsilonTransitions();
  }
}
