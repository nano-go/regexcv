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
import java.util.LinkedList;

public class DigraphDotGenerator implements Pass<Digraph, String> {

  private static String generateName(int serialNumber) {
    return String.format("s%d", serialNumber);
  }

  @Override
  public String accept(Digraph digraph) {
    var codeBuilder = new GraphvizDotBuilder("digraph", Display.escapingString(digraph.getName()));

    var queue = new LinkedList<Node>();
    var names = new HashMap<Node, String>();
    int serialNumber = 1;
    names.put(digraph.getStart(), generateName(0));
    queue.offer(digraph.getStart());
    while (!queue.isEmpty()) {
      var node = queue.poll();
      var name = names.get(node);
      codeBuilder.addNodeDeclr(name, node.isFinalState());
      for (var edges : node.getAllEdges()) {
        var label = edges.getKey().toString();
        for (var successor : edges.getValue()) {
          var succName = names.get(successor);
          if (succName == null) {
            succName = generateName(serialNumber++);
            names.put(successor, succName);
            queue.offer(successor);
          }
          codeBuilder.addEdge(name, succName, label);
        }
      }
    }

    return codeBuilder.build();
  }
}
