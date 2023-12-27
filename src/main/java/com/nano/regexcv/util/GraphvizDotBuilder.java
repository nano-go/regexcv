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

import java.util.ArrayList;
import java.util.List;

public class GraphvizDotBuilder {

  private StringBuilder code;

  private List<String> nodeDeclrs;
  private List<String> edges;

  public GraphvizDotBuilder(String type, String name) {
    code = new StringBuilder();
    nodeDeclrs = new ArrayList<>();
    edges = new ArrayList<>();
    code.append(type)
        .append(" ")
        .append(escapingName(name))
        .append("{\n")
        .append("\trankdir=LR\n\n");
  }

  public GraphvizDotBuilder addNodeDeclr(String name) {
    return addNodeDeclr(name, "circle");
  }

  public GraphvizDotBuilder addNodeDeclr(String name, boolean isFinalState) {
    return addNodeDeclr(name, isFinalState ? "doublecircle" : "circle");
  }

  public GraphvizDotBuilder addNodeDeclr(String name, String shape) {
    nodeDeclrs.add(String.format("%s [shape=%s]", escapingName(name), shape));
    return this;
  }

  public GraphvizDotBuilder addEdge(String fromNode, String toNode, CharacterRange charRange) {
    return addEdge(fromNode, toNode, charRange.toString());
  }

  public GraphvizDotBuilder addEdge(String fromNode, String toNode, String label) {
    edges.add(
        String.format(
            "%s -> %s [label=\"%s\"]",
            escapingName(fromNode), escapingName(toNode), escapingName(label)));
    return this;
  }

  public String build() {
    for (String node : nodeDeclrs) {
      code.append("\t").append(node).append("\n");
    }
    code.append("\n");
    for (String edge : edges) {
      code.append("\t").append(edge).append("\n");
    }
    return code.append("}").toString();
  }

  private String escapingName(String name) {
    var buf = new StringBuilder();
    for (char ch : name.toCharArray()) {
      switch (ch) {
        case '\n' -> buf.append("\\n");
        case '\r' -> buf.append("\\r");
        case '\f' -> buf.append("\\f");
        case '\b' -> buf.append("\\b");
        case '\t' -> buf.append("\\t");
        case '\"' -> buf.append("\\\"");
        case '\\' -> buf.append("\\\\");
        default -> buf.append(ch);
      }
    }
    return buf.toString();
  }
}
