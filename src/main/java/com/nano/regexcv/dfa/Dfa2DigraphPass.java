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
import com.nano.regexcv.util.Digraph;
import com.nano.regexcv.util.Digraph.Node;
import java.util.HashMap;
import java.util.LinkedList;

/** This converts a DFA into a digraph. */
public class Dfa2DigraphPass implements Pass<Dfa, Digraph> {

  @Override
  public Digraph accept(Dfa dfa) {
    var table = dfa.getCharsNumTable();
    var nodeMap = new HashMap<DfaState, Node>();
    var stack = new LinkedList<DfaState>();

    var startState = dfa.getStart();
    stack.push(startState);
    nodeMap.put(startState, new Node(startState.isFinalState()));

    while (!stack.isEmpty()) {
      var state = stack.pop();
      var node = nodeMap.get(state);
      var transitions = state.getAllTransitions();
      for (int num = 0; num < transitions.length; num++) {
        var successorState = transitions[num];
        if (successorState == null) continue;
        var successorNode =
            nodeMap.computeIfAbsent(
                successorState,
                key -> {
                  stack.push(key);
                  return new Node(key.isFinalState());
                });
        // Dfa has not epsilon edge numbered 0.
        node.addEdge(table.getCharRangeOfNum(num + 1), successorNode);
      }
    }

    return new Digraph(nodeMap.get(startState), "DFA");
  }
}
