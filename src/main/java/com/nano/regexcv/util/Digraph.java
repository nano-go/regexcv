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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Digraph {

  public static final class Label {
    protected String label;
    protected CharacterRange[] ranges;

    public Label(Collection<CharacterRange> ranges) {
      this(ranges.toArray(CharacterRange[]::new));
    }

    public Label(String name, Collection<CharacterRange> ranges) {
      this(name, ranges.toArray(CharacterRange[]::new));
    }

    public Label(CharacterRange... ranges) {
      this.ranges = ranges;
      if (this.ranges.length == 0) {
        label = "ε";
      } else {
        label = Arrays.toString(ranges);
        // Trim '[' and ']'
        label = label.substring(1, label.length() - 1);
      }
    }

    public Label(String label, CharacterRange... ranges) {
      this.label = label;
      this.ranges = ranges;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((label == null) ? 0 : label.hashCode());
      result = prime * result + Arrays.hashCode(ranges);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Label other = (Label) obj;
      if (label == null) {
        if (other.label != null) return false;
      } else if (!label.equals(other.label)) return false;
      if (!Arrays.equals(ranges, other.ranges)) return false;
      return true;
    }
  }

  public static final class Node {
    protected HashMap<Label, HashSet<Node>> edges;
    protected boolean isFinalState;

    public Node(boolean isFinalState) {
      this.edges = new HashMap<>();
      this.isFinalState = isFinalState;
    }

    public void addEdge(CharacterRange charRange, Iterable<Node> toNodes) {
      for (Node n : toNodes) {
        addEdge(new Label(charRange), n);
      }
    }

    public void addEdge(CharacterRange charRange, Node toNode) {
      addEdge(new Label(charRange), toNode);
    }

    public void addEdge(Label label, Node toNode) {
      HashSet<Node> nodes = edges.get(label);
      if (nodes == null) {
        nodes = new HashSet<>();
        edges.put(label, nodes);
      }
      nodes.add(toNode);
    }

    public void addEpsilonEdge(Node successor) {
      this.addEdge(new Label(), successor);
    }

    public Set<Node> getNodes(Label label) {
      return Objects.requireNonNullElse(edges.get(label), Collections.<Node>emptySet());
    }

    public Set<Map.Entry<Label, HashSet<Node>>> getAllEdges() {
      return edges.entrySet();
    }

    public boolean isFinalState() {
      return isFinalState;
    }
  }

  private Node start;
  private String name;

  public Digraph(Node start, String name) {
    this.start = start;
    this.name = name;
  }

  public Node getStart() {
    return start;
  }

  public String getName() {
    return name;
  }
}
