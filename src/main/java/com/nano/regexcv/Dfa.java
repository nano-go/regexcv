package com.nano.regexcv;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

public class Dfa {
	
	/**
	 * Visible for DfaMinimizer.
	 */
	protected DfaState start;

	public Dfa(DfaState start) {
		this.start = start;
	}

	public DfaState getStart() {
		return start;
	}
	
	public DfaState[] getAllStates() {
		ArrayList<DfaState> list = new ArrayList<>();
		HashSet<DfaState> marker = new HashSet<>();
		list.add(this.start);
		marker.add(this.start);
		int i = 0;
		while (i < list.size()) {
			DfaState dstate = list.get(i);
			for (DfaState to : dstate.getAllTransitions()) {
				if (to == null) continue;
				if (!marker.contains(to)) {
					list.add(to);
					marker.add(to);
				}
			}
			i ++;
		}
		return list.toArray(new DfaState[list.size()]);
	}
}
