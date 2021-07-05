package com.nano.regexcv.gen;
import com.nano.regexcv.util.Digraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static com.nano.regexcv.util.Digraph.*;

public class DigraphDotGenerator implements DigraphGenerator<String> {
	
	private static String generateName(int serialNumber) {
		return String.format("s%d", serialNumber);
	}
	
	private GraphvizDotBuilder code;
	
	@Override
	public String generate(Digraph digraph) {
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
						toName = generateName(serialNumber ++);
						marker.put(n, toName);
						queue.offer(n);
					}
					code.addEdge(name, toName, label);
				}
			}
		}
		
	}
}
