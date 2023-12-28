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

import com.nano.regexcv.Pass;
import com.nano.regexcv.util.Digraph.Label;
import com.nano.regexcv.util.Digraph.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** This is a useful pass that can merge edges of a digraph. */
public class MergingDigraphEdges implements Pass<Digraph, Digraph> {

  public static Tag[] COMMON_TAGS =
      new Tag[] {
        new Tag("any char", CharacterRange.RANGE_ANY),
        new Tag("\\w", CharacterRanges.parse("a-zA-Z0-9_")),
        new Tag("\\d", CharacterRanges.parse("0-9")),
        new Tag("\\s", CharacterRanges.parse(" \t-\n\f-\r")),
      };

  /**
   * Some specified ranges can be represented by the tag.
   *
   * <p>For Example:
   *
   * <pre>{@code
   * [a-zA-Z0-9_] can be represented by the '\w' tag.
   * }</pre>
   */
  public static class Tag {
    protected String name;
    protected HashSet<CharacterRange> matchedRanges;

    public Tag(String name, CharacterRange... matchedRanges) {
      this.name = name;
      this.matchedRanges = new HashSet<>(Arrays.asList(matchedRanges));
    }

    public String getName() {
      return name;
    }

    public HashSet<CharacterRange> getMatchedRanges() {
      return matchedRanges;
    }

    public Label toLabel() {
      return new Label(name, matchedRanges);
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.name, this.matchedRanges);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Tag other = (Tag) obj;
      if (name == null) {
        if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      if (matchedRanges == null) {
        if (other.matchedRanges != null) return false;
      } else if (!matchedRanges.equals(other.matchedRanges)) return false;
      return true;
    }
  }

  private final Tag[] tags;

  public MergingDigraphEdges() {
    this(COMMON_TAGS);
  }

  public MergingDigraphEdges(Tag... tags) {
    this.tags = tags;
  }

  @Override
  public Digraph accept(Digraph digraph) {
    var stack = new LinkedList<Node>();
    var marked = new HashSet<>();
    stack.push(digraph.getStart());
    marked.add(digraph.getStart());
    while (!stack.isEmpty()) {
      var node = stack.pop();
      mergeEdges(node);
      node.edges.values().stream()
          .flatMap(e -> e.stream())
          .filter(marked::add)
          .forEach(stack::push);
    }
    return digraph;
  }

  /**
   * Merges continuous edges and tries to combine specific edges to labels with corresponding tag
   * name.
   */
  private void mergeEdges(Node node) {
    var map = mapSuccessorToRanges(node);
    node.edges.clear(); // rebuild edges
    for (var entry : map.entrySet()) {
      var successor = entry.getKey();
      var distinctRanges = getDistinctRanges(entry.getValue());
      var labels = combineRangesToLabels(distinctRanges);
      for (var label : labels) {
        node.addEdge(label, successor);
      }
    }
  }

  /**
   * If a node can accept the ranges 'R' to be transferred to a successor node, then maps the
   * successor node to the ranges 'R'.
   */
  private HashMap<Node, HashSet<CharacterRange>> mapSuccessorToRanges(Node node) {
    var map = new HashMap<Node, HashSet<CharacterRange>>();
    for (var entry : node.getAllEdges()) {
      var label = entry.getKey();
      var successors = entry.getValue();
      for (var successorNode : successors) {
        var set = map.computeIfAbsent(successorNode, key -> new HashSet<>());
        for (var range : label.ranges) {
          set.add(range);
        }
      }
    }
    return map;
  }

  private ArrayList<CharacterRange> getDistinctRanges(Collection<CharacterRange> ranges) {
    var distinctRanges = new ArrayList<CharacterRange>();
    var iter = new PeekingIterator<>(ranges.stream().sorted().iterator());
    while (iter.hasNext()) {
      var range = mergeContinuousRanges(iter);
      distinctRanges.add(range);
    }
    return distinctRanges;
  }

  private CharacterRange mergeContinuousRanges(PeekingIterator<CharacterRange> iter) {
    var first = iter.next();
    var right = first.to;
    while (iter.hasNext()) {
      var range = iter.peek();
      if (range.from != 0 && (char) (range.from - 1) > right) {
        break;
      }
      iter.next();
      right = range.to > right ? range.to : right;
    }
    return new CharacterRange(first.from, right);
  }

  private List<Label> combineRangesToLabels(List<CharacterRange> ranges) {
    var labels = new ArrayList<Label>();

    // Get matched tags.
    var tags = matchesTags(ranges);
    // Try to combine inversion of the rest ranges to a label like '[^a-z]'
    var label = tryCombineInversionOfRanges(ranges);
    if (label.isPresent()) {
      labels.add(label.get());
      // Clear list because the ranges has been merged into a label.
      ranges.clear();
    }

    if (!tags.isEmpty() || !ranges.isEmpty()) {
      labels.add(getLabelOfTagsAndRanges(tags, ranges));
    }
    return labels;
  }

  private List<Tag> matchesTags(List<CharacterRange> ranges) {
    var tags = new ArrayList<Tag>();
    for (var tag : this.tags) {
      if (ranges.containsAll(tag.matchedRanges)) {
        tags.add(tag);
        ranges.removeAll(tag.matchedRanges);
      }
    }
    return tags;
  }

  /**
   * If the length of the inversion of the ranges is less than the length of itself, then returns
   * the label {@code '^' + inversion} like {@code [^a-z], [^\w]}
   */
  private Optional<Label> tryCombineInversionOfRanges(List<CharacterRange> ranges) {
    var inversedRanges = CharacterRanges.inverseRanges(ranges);
    var inversedLength = sumLength(inversedRanges);
    var length = sumLength(ranges);
    if (length < inversedLength) {
      return Optional.empty();
    }
    var labelName = getNameOfInversedRanges(inversedRanges);
    return Optional.of(new Label(labelName, ranges));
  }

  private int sumLength(Collection<CharacterRange> ranges) {
    return ranges.stream().mapToInt(range -> range.to - range.from).sum();
  }

  private String getNameOfInversedRanges(List<CharacterRange> inversedRanges) {
    var name = new StringBuilder();
    var tags = matchesTags(inversedRanges);
    if (!tags.isEmpty()) {
      name.append("[^");
      tags.forEach(name::append);
    }
    if (!inversedRanges.isEmpty()) {
      if (name.length() == 0) {
        name.append("[^");
      }
      inversedRanges.forEach(name::append);
    }
    name.append("]");
    return name.toString();
  }

  /** Returns a label that represents the specified tags and ranges. */
  private Label getLabelOfTagsAndRanges(List<Tag> tags, List<CharacterRange> ranges) {
    var name = new StringBuilder();
    var trim = ranges.size() + tags.size() == 1;
    name.append(trim ? "" : "[");
    tags.forEach(name::append);
    ranges.forEach(name::append);
    name.append(trim ? "" : "]");
    for (var tag : tags) {
      ranges.addAll(tag.matchedRanges);
    }
    return new Label(name.toString(), ranges);
  }
}
