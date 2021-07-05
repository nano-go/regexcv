package com.nano.regexcv.gen;
import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.Nfa;
import com.nano.regexcv.NfaState;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.Digraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This converts a DFA into a digraph.
 */
public class Nfa2DigraphGenerator implements NfaGenerator<Digraph> {

	private static CharacterRange getCharRange(CharacterClass table, int classNumber) {
		return classNumber == 0 ? CharacterRange.EPSILON 
			: table.getCharRange(classNumber);
	}
	
	@Override
	public Digraph generate(Nfa nfa, CharacterClass charClass) {
		LinkedList<NfaState> stack = new LinkedList<>();
		HashMap<NfaState, Digraph.Node> nodes = new HashMap<>();

		NfaState start = nfa.getStart();
		stack.push(start);
		nodes.put(start, new Digraph.Node(start.isFinalState()));

		while (!stack.isEmpty()) {
			NfaState nstate = stack.pop();
			Digraph.Node fromNode = nodes.get(nstate);
			HashSet<NfaState>[] transitions = nstate.getTransitions();
			for (int classNum = 0; classNum < transitions.length; classNum ++) {
				if (transitions[classNum] == null) {
					continue;
				}
				for (NfaState state : transitions[classNum]) {
					Digraph.Node toNode = nodes.get(state);
					if (toNode == null) {
						toNode = new Digraph.Node(state.isFinalState());
						nodes.put(state, toNode);
						stack.push(state);
					}
					CharacterRange charRange = getCharRange(charClass, classNum);
					fromNode.addEdge(charRange, toNode);
				}
			}
		}

		return new Digraph(nodes.get(start), "NFA");
	}
    
}
