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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/** This is a useful pass that can merge edges of a digraph. */
public class MergingDigraphEdges implements Pass<Digraph, Digraph> {

  public static Tag[] COMMON_TAGS =
      new Tag[] {
        new Tag("any char", CharacterRange.RANGE_ANY),
        new Tag("\\w", CharacterRange.parse("a-zA-Z0-9_")),
        new Tag("\\d", CharacterRange.parse("0-9")),
        new Tag("\\s", CharacterRange.parse(" \n\f\t\b\r")),
      };

  /**
   * This can merge specified ranges into a tag.
   *
   * <p>For Example:
   *
   * <pre>{@code
   * [a-zA-Z0-9_] can be merged as a '\w' tag.
   * }</pre>
   */
  public static class Tag {
    protected String name;
    protected HashSet<CharacterRange> matchedRanges;

    public Tag(String name, CharacterRange... matchedRanges) {
      this.name = name;
      this.matchedRanges = new HashSet<>(Arrays.asList(matchedRanges));
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
    var marker = new HashSet<>();
    stack.push(digraph.getStart());
    while (!stack.isEmpty()) {
      var node = stack.pop();
      marker.add(node);
      mergeContinuousEdges(node);
      mergeEdgesIntoTags(node);
      node.edges.values().stream()
          .flatMap(e -> e.stream())
          .filter(marker::add)
          .forEach(stack::push);
    }
    return digraph;
  }

  /**
   * Merge continuous edges.
   *
   * <p>If a state can accept {@code [a-d], [c-g], h, [i-z]} to another state, then merge theme into
   * an interval {@code [a-z]}.
   */
  private void mergeContinuousEdges(Node node) {
    var map = mapNodeToRanges(node);
    node.edges.clear();
    for (var entry : map.entrySet()) {
      var toNode = entry.getKey();
      var rangesIter = sortRanges(entry.getValue());
      while (rangesIter.hasNext()) {
        var range = mergeContinuousRanges(rangesIter);
        node.addEdge(new Label(range), toNode);
      }
    }
  }

  private PeekingIterator<CharacterRange> sortRanges(HashSet<CharacterRange> value) {
    return new PeekingIterator<>(value.stream().sorted().iterator());
  }

  private HashMap<Node, HashSet<CharacterRange>> mapNodeToRanges(Node node) {
    var map = new HashMap<Node, HashSet<CharacterRange>>();
    for (var entry : node.getAllEdges()) {
      var label = entry.getKey();
      var toNodes = entry.getValue();
      for (var toNode : toNodes) {
        var set = map.get(toNode);
        if (set == null) {
          set = new HashSet<>();
          map.put(toNode, set);
        }
        Arrays.stream(label.ranges).forEach(set::add);
      }
    }
    return map;
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

  private void mergeEdgesIntoTags(Node node) {
    var map = mapNodeToRanges(node);
    node.edges.clear();
    for (var entry : map.entrySet()) {
      var toNode = entry.getKey();
      var ranges = entry.getValue();
      var tags = matchesTags(ranges);
      for (var tag : tags) {
        var label = new Label(tag.name, tag.matchedRanges.toArray(CharacterRange[]::new));
        node.addEdge(label, node);
      }
      if (!ranges.isEmpty()) {
        var label = new Label(ranges.toArray(CharacterRange[]::new));
        node.addEdge(label, toNode);
      }
    }
  }

  private Tag[] matchesTags(HashSet<CharacterRange> ranges) {
    var tags = new ArrayList<>();
    for (var tag : this.tags) {
      if (ranges.containsAll(tag.matchedRanges)) {
        tags.add(tag);
        ranges.removeAll(tag.matchedRanges);
      }
    }
    return tags.toArray(Tag[]::new);
  }
}
