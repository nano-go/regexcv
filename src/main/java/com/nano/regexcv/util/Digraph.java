package com.nano.regexcv.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.nano.regexcv.util.CharacterRange.*;

public class Digraph {
	
	private static final ReductionRuler[] RULES = {
		newRuler("\\\\w", RANGE_A_Z, RANGE_a_z, RANGE_0_9),
		newRuler("any char", RANGE_ANY),
	};
	
	private static final ReductionRuler newRuler(String reduceTo, CharacterRange... rule) {
		return new ReductionRuler(rule, new SpecialAcception(reduceTo));
	}
	
	private static class ReductionRuler {
		private CharacterRange[] reductionRule;
		private Acception reducedTo;

		public ReductionRuler(CharacterRange[] reductionRule, Acception reducedTo) {
			this.reductionRule = reductionRule;
			this.reducedTo = reducedTo;
		}
	}
	
	public interface Acception {
		public String toString();
	}

	public static class SpecialAcception implements Acception {
		private String label;

		public SpecialAcception(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}

		@Override
		public int hashCode() {
			return label.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof SpecialAcception &&
				((SpecialAcception) obj).label.equals(label);
		}
	}

	public static class Node {
		private HashMap<Acception, HashSet<Node>> edges;
		private boolean isFinalState;
		
		public Node(boolean isFinalState) {
			this.edges = new HashMap<>();
			this.isFinalState = isFinalState;
		}

		public void addEdges(CharacterRange in, Iterable<Node> toNodes) {
			for (Node n : toNodes) {
				addEdge(in, n);
			}
		}

		public void addEdge(Acception in, Node toNode) {
			HashSet<Node> nodes = edges.get(in);
			if (nodes == null) {
				nodes = new HashSet<>();
				edges.put(in, nodes);
			}
			nodes.add(toNode);
		}

		public Set<Node> getNodes(Acception in) {
			return Objects.requireNonNullElse(
				edges.get(in), Collections.<Node>emptySet()
			);
		}

		public Set<Map.Entry<Acception, HashSet<Node>>> getAllEdges() {
			return edges.entrySet();
		}

		public boolean isFinalState() {
			return isFinalState;
		}
	}
	
	private static Set<Node> intersectedSet(Set<Node>... sets) {
		Set<Node> intersection = new HashSet<>();
		if (sets.length == 0) {
			return intersection;
		}
		intersection.addAll(sets[0]);
		for (int i = 1; i < sets.length; i ++) {
			Set<Node> set = sets[i];	
			Iterator<Node> iterator = intersection.iterator();
			while (iterator.hasNext()) {
				if (!set.contains(iterator.next())) {
					iterator.remove();
				}
			}
		}
		return intersection;
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

	public void reduceEdges() {
		LinkedList<Node> stack = new LinkedList<>();
		HashSet<Node> marker = new HashSet<>();
		stack.push(start);
		while (!stack.isEmpty()) {
			Node n = stack.pop();
			marker.add(n);
			reduceEdges(n);
			n.edges.values().stream()
				.flatMap(e -> e.stream())
				.filter(e -> !marker.contains(e))
				.forEach(stack::push);
		}
	}

	private void reduceEdges(Node n) {
		tryToMergeEdges(n);
		for (ReductionRuler rule : RULES) {
			tryToReduce(n, rule);
		}
	}

	/**
	 * s0['a-e'] = s1
	 * s0['f']   = s1
	 * s0['a-z'] = s1
	 *
	 * will be merged: s0['a-z'] = s1
	 */
	private void tryToMergeEdges(Node n) {
		if (n.edges.size() <= 1) {
			return;
		}
		List<CharacterRange> ranges = new ArrayList<>();
		for (Map.Entry<Acception, HashSet<Node>> edges : n.getAllEdges()) {
			if (edges.getKey() instanceof CharacterRange) {
				ranges.add((CharacterRange) edges.getKey());
			}
		}
		Collections.sort(ranges);

		CharacterRange previousCharRange = ranges.get(0);
		Set<Node> previousNodes = n.getNodes(previousCharRange);
		int i = 1;
		outloop: while (i < ranges.size()) {
			CharacterRange range = ranges.get(i);
			char from = range.getFrom(), to = range.getTo();
			Set<Node> nodes = n.getNodes(range);
			if (previousCharRange.getTo() == from-1) {
				Set<Node> intersectedSet = intersectedSet(new Set[]{previousNodes, nodes});
				range = new CharacterRange(previousCharRange.getFrom(), to);
				for (Node node : intersectedSet) {
					previousNodes.remove(node);
					nodes.remove(node);
					n.addEdge(range, node);
				}
				nodes = n.getNodes(range);
			}
			i ++;
			previousCharRange = range;
			previousNodes = nodes;
		}
	}

	private void tryToReduce(Node n, ReductionRuler ruler) {
		Set<Node>[] nodes = new Set[ruler.reductionRule.length];
		
		for (int i = 0; i < ruler.reductionRule.length; i ++) {
			nodes[i] = n.getNodes(ruler.reductionRule[i]);
		}

		Set<Node> intersectedSet = intersectedSet(nodes);
		for (Node commonN : intersectedSet) {
			for (Set<Node> edge : nodes) {
				edge.remove(commonN);
			}
			n.addEdge(ruler.reducedTo, commonN);
		}
	}
	
}
