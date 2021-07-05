package com.nano.regexcv.gen;

import com.nano.regexcv.CharacterClass;
import com.nano.regexcv.Dfa;
import com.nano.regexcv.DfaState;
import com.nano.regexcv.Nfa;
import com.nano.regexcv.NfaState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This converts a NFA into a DFA.
 */
public class Nfa2DfaGenerator implements NfaGenerator<Dfa> {
	
	private static class DState {
		private HashSet<NfaState> nfaSubset;

		public DState(HashSet<NfaState> nfaSubset) {
			this.nfaSubset = nfaSubset;
		}
		
		public boolean isFinalState(Nfa nfa) {
			return this.nfaSubset.contains(nfa.getEnd());
		}

		@Override
		public int hashCode() {
			return nfaSubset.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DState) {
				return nfaSubset.equals(((DState) obj).nfaSubset);
			}
			return false;
		}
	}
	
	@Override
	public Dfa generate(Nfa nfa, CharacterClass charClass) {
		LinkedList<DState> stack = new LinkedList<>();
		HashMap<DState, DfaState> dstates = new HashMap<>();
		final int charSetCount = charClass.getTableSize();
		
		DState start = new DState(closure(nfa.getStart()));
		dstates.put(start, new DfaState(charSetCount, start.isFinalState(nfa)));
		stack.push(start);
		
		while (!stack.isEmpty()) {
			DState dstate = stack.pop();
			DfaState fromDfaState = dstates.get(dstate);
			
			for (int in = 1; in <= charSetCount; in ++) {
				HashSet<NfaState> U = closure(move(dstate.nfaSubset, in));
				if (U.isEmpty()) {
					continue;
				}
				DState newDState = new DState(U);
				DfaState toDfaState = dstates.get(newDState);
				if (toDfaState == null) {
					toDfaState = new DfaState(charSetCount, newDState.isFinalState(nfa));
					dstates.put(newDState, toDfaState);
					stack.push(newDState);
				}
				fromDfaState.addTransition(in, toDfaState);
			}
			
		}
		return new Dfa(dstates.get(start));
	}
	
	private HashSet<NfaState> closure(NfaState s) {
		HashSet<NfaState> T = new HashSet<>();
		T.add(s);
		return closure(T);
	}
	
	private HashSet<NfaState> closure(HashSet<NfaState> T) {
		HashSet<NfaState> set = new HashSet<>(T);
		LinkedList<NfaState> stack = new LinkedList<>();
		set.stream().forEach(stack::push);
		while (!stack.isEmpty()) {
			NfaState s = stack.pop();
			for (NfaState u :  s.getEpsilonTransitions()) {
				if (set.add(u)) {
					stack.push(u);	
				}
			}
		}
		return set;
	}
	
	private HashSet<NfaState> move(HashSet<NfaState> T, int a) {	
		LinkedList<NfaState> stack = new LinkedList<NfaState>();
		HashSet<NfaState> marker = new HashSet<>(T);
		HashSet<NfaState> set = new HashSet<>();
		T.stream().forEach(stack::push);	
		while (!stack.isEmpty()) {
			NfaState s = stack.pop();
			Set<NfaState> emptyTransitions = s.getEpsilonTransitions();
			for (NfaState state : emptyTransitions) {
				if (marker.add(state)) {
					stack.push(state);
				}
			}
			for (NfaState state : s.getStateSet(a)) {
				marker.add(state);
				set.add(state);
			}
		}
		return set;
	}
}
