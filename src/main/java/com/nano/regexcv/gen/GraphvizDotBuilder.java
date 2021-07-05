package com.nano.regexcv.gen;
import com.nano.regexcv.util.CharacterRange;
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
		code.append(type).append(" ").append(name).append("{\n")
			.append("\trankdir=LR\n\n");
	}
	
	public GraphvizDotBuilder addNodeDeclr(String name) {
		return addNodeDeclr(name, "circle");
	}
	
	public GraphvizDotBuilder addNodeDeclr(String name, boolean isFinalState) {
		return addNodeDeclr(name, isFinalState ? "doublecircle" : "circle");
	}
	
	public GraphvizDotBuilder addNodeDeclr(String name, String shape) {
		nodeDeclrs.add(String.format("%s [shape=%s]", name, shape)); 
		return this;
	}
	
	public GraphvizDotBuilder addEdge(String fromNode, String toNode, CharacterRange charRange) {	
		return addEdge(fromNode, toNode, charRange.toString());
	}
	
	public GraphvizDotBuilder addEdge(String fromNode, String toNode, String label) {
		edges.add(String.format("%s -> %s [label=\"%s\"]", 
			fromNode, toNode, label
		));
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
}
