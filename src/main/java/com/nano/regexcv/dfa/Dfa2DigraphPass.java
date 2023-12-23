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
import com.nano.regexcv.util.ICharsNumTable;
import java.util.HashMap;
import java.util.LinkedList;

/** This converts a DFA into a digraph. */
public class Dfa2DigraphPass implements Pass<Dfa, Digraph> {

  @Override
  public Digraph accept(Dfa dfa) {
    ICharsNumTable charClass = dfa.getCharsNumTable();
    LinkedList<DfaState> stack = new LinkedList<>();

    HashMap<DfaState, Digraph.Node> nodes = new HashMap<>();

    DfaState start = dfa.getStart();
    stack.push(start);
    nodes.put(start, new Digraph.Node(start.isFinalState()));

    while (!stack.isEmpty()) {
      DfaState dstate = stack.pop();
      Digraph.Node node = nodes.get(dstate);
      DfaState[] transitions = dstate.getAllTransitions();
      for (int i = 0; i < transitions.length; i++) {
        DfaState toDstate = transitions[i];
        if (transitions[i] == null) continue;
        Digraph.Node toNode = nodes.get(toDstate);
        if (toNode == null) {
          toNode = new Digraph.Node(toDstate.isFinalState());
          nodes.put(toDstate, toNode);
          stack.push(toDstate);
        }
        node.addEdge(charClass.getCharRangeOfNum(i + 1), toNode);
      }
    }

    return new Digraph(nodes.get(start), "DFA");
  }
}
