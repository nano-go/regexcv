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
import com.nano.regexcv.util.CharacterClass;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.Digraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/** This converts a DFA into a digraph. */
public class Nfa2DigraphPass implements Pass<Nfa, Digraph> {

  private static CharacterRange getCharRange(CharacterClass table, int classNumber) {
    return classNumber == 0 ? CharacterRange.EPSILON : table.getCharRange(classNumber);
  }

  @Override
  public Digraph accept(Nfa nfa) {
    CharacterClass charTable = nfa.getCharTable();
    LinkedList<NfaState> stack = new LinkedList<>();
    HashMap<NfaState, Digraph.Node> nodes = new HashMap<>();

    NfaState start = nfa.getStart();
    stack.push(start);
    nodes.put(start, new Digraph.Node(start.isFinalState()));

    while (!stack.isEmpty()) {
      NfaState nstate = stack.pop();
      Digraph.Node fromNode = nodes.get(nstate);
      HashSet<NfaState>[] transitions = nstate.getTransitions();
      for (int classNum = 0; classNum < transitions.length; classNum++) {
        if (transitions[classNum] == null) {
          continue;
        }
        for (NfaState state : transitions[classNum]) {
          Digraph.Node toNode = nodes.get(state);
          if (toNode == null) {
            toNode = new Digraph.Node(state.isFinalState());
            nodes.put(state, toNode);
            stack.push(state);
          }
          CharacterRange charRange = getCharRange(charTable, classNum);
          fromNode.addEdge(charRange, toNode);
        }
      }
    }

    return new Digraph(nodes.get(start), "NFA");
  }
}
