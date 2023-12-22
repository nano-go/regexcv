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
package com.nano.regexcv.util;

import static com.nano.regexcv.util.Digraph.*;

import com.nano.regexcv.Pass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class DigraphDotGenerator implements Pass<Digraph, String> {

  private static String generateName(int serialNumber) {
    return String.format("s%d", serialNumber);
  }

  private GraphvizDotBuilder code;

  @Override
  public String accept(Digraph digraph) {
    code = new GraphvizDotBuilder("digraph", digraph.getName());
    generateCode(digraph.getStart());
    return code.build();
  }

  private void generateCode(Node node) {
    LinkedList<Node> queue = new LinkedList<>();
    HashMap<Node, String> marker = new HashMap<>();
    int serialNumber = 1;
    marker.put(node, generateName(0));
    queue.push(node);
    while (!queue.isEmpty()) {
      Node fromNode = queue.poll();
      String name = marker.get(fromNode);
      code.addNodeDeclr(name, fromNode.isFinalState());
      for (Map.Entry<Acception, HashSet<Node>> edges : fromNode.getAllEdges()) {
        String label = edges.getKey().toString();
        for (Node n : edges.getValue()) {
          String toName = marker.get(n);
          if (toName == null) {
            toName = generateName(serialNumber++);
            marker.put(n, toName);
            queue.offer(n);
          }
          code.addEdge(name, toName, label);
        }
      }
    }
  }
}
