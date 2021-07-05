package com.nano.regexcv.gen;

import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.Dfa;
import com.nano.regexcv.DfaState;
import com.nano.regexcv.util.Digraph;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This converts a DFA into a digraph.
 */
public class Dfa2DigraphGenerator implements DfaGenerator<Digraph> {

	@Override
	public Digraph generate(Dfa dfa, CharacterClass charClass) {
		LinkedList<DfaState> stack = new LinkedList<>();
		
		HashMap<DfaState, Digraph.Node> nodes = new HashMap<>();
		
		DfaState start = dfa.getStart();
		stack.push(start);
		nodes.put(start, new Digraph.Node(start.isFinalState()));
		
		while (!stack.isEmpty()) {
			DfaState dstate = stack.pop();
			Digraph.Node node = nodes.get(dstate);
			DfaState[] transitions = dstate.getAllTransitions();
			for (int i = 0; i < transitions.length; i ++) {
				DfaState toDstate = transitions[i];
				if (transitions[i] == null) continue;	
				Digraph.Node toNode = nodes.get(toDstate);
				if (toNode == null) {
					toNode = new Digraph.Node(toDstate.isFinalState());
					nodes.put(toDstate, toNode);
					stack.push(toDstate);
				}
				node.addEdge(charClass.getCharRange(i+1), toNode);
			}
		}
		
		return new Digraph(nodes.get(start), "DFA");
	}
    
}
