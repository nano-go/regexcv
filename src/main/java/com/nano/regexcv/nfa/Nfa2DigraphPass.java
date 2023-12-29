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
import com.nano.regexcv.util.Digraph;
import com.nano.regexcv.util.Digraph.Node;
import java.util.HashMap;
import java.util.LinkedList;

/** This converts a DFA into a digraph. */
public class Nfa2DigraphPass implements Pass<Nfa, Digraph> {

  @Override
  public Digraph accept(Nfa nfa) {
    var table = nfa.getCharsNumTable();
    var nodeMap = new HashMap<NfaState, Node>();
    var stack = new LinkedList<NfaState>();

    var startState = nfa.getStart();
    stack.push(startState);
    nodeMap.put(startState, new Node(startState.isFinalState()));

    while (!stack.isEmpty()) {
      var state = stack.pop();
      var node = nodeMap.get(state);
      var transitions = state.getTransitions();
      for (int num = 0; num < transitions.length; num++) {
        var successorStateSet = transitions[num];
        if (successorStateSet == null) continue;
        for (var successorState : successorStateSet) {
          var successorNode =
              nodeMap.computeIfAbsent(
                  successorState,
                  key -> {
                    stack.push(key);
                    return new Node(key.isFinalState());
                  });
          if (num == 0) {
            node.addEpsilonEdge(successorNode);
          } else {
            node.addEdge(table.getCharRangeOfNum(num), successorNode);
          }
        }
      }
    }

    return new Digraph(nodeMap.get(startState), "NFA");
  }
}
