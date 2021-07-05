package com.nano.regexcv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DfaMinimizer {
	
	private static class GroupList {
		private ArrayList<Group> groupList;
		private HashMap<DfaState, Group> groupMap;
		
		public GroupList(Group... groups) {
			groupList = new ArrayList<>(groups.length);
			groupMap = new HashMap<>();
			for (Group g : groups) {
				groupList.add(g);
				for (DfaState dstate : g.states) {
					groupMap.put(dstate, g);
				}
			}
		}
		
		public Group paritition(Group group, DfaState s) {
			group.remove(s);
			Group newGroup = new Group();
			newGroup.add(s);
			groupMap.put(s, newGroup);
			groupList.add(newGroup);
			return newGroup;
		}
		
		public Group get(DfaState dstate) {
			if (dstate == null) {
				return null;
			}
			return groupMap.get(dstate);
		}
		
		public Group get(int i) {
			return groupList.get(i);
		}
		
		public int size() { 
			return groupList.size();
		}
	}
    
	private static class Group {
		private HashSet<DfaState> states;
		public Group() {
			states = new HashSet<>();
		}

		public DfaState[] getDfaStates() {
			return states.toArray(new DfaState[0]);
		}

		public void add(DfaState dstate) {
			states.add(dstate);
		}

		public void remove(DfaState dstate) {
			states.remove(dstate);
		}

		public int size() {
			return states.size();
		}
	}
    
    public static void minizeDfa(Dfa dfa) {
		DfaState[] table = dfa.getAllStates();
		GroupList groupList = initGroups(table);
		while (partition(dfa, groupList));
		dfa.start = rebuild(dfa, table, groupList);
	}
	
	private static boolean partition(Dfa dfa, GroupList groupList) {
		final int CHAR_SET_COUNT = dfa.getStart().getAllTransitions().length;
		boolean paritionFlag = false;
		for (int i = 0; i < groupList.size(); i ++) {
			Group group = groupList.get(i);
			if (group.size() <= 1) {
				continue;
			}
			DfaState[] dstates = group.getDfaStates();
			for (int in = 1; in <= CHAR_SET_COUNT; in ++) {
				if(partition(groupList, group, dstates, in)) {
					paritionFlag = true;
					break;
				}
			}
		}
		return paritionFlag;
	}
	
	private static boolean partition(GroupList groupList, Group group, DfaState[] dstates, int in) {
		int size = dstates.length;
		for (int x = 0; x < size; x ++) {
			for (int y = x+1; y < size; y ++) {
				DfaState t = dstates[x];
				DfaState s = dstates[y];
				if (groupList.get(t.getState(in)) != groupList.get(s.getState(in))) {
					groupList.paritition(group, t);
					return true;
				}
			}
		}
		return false;
	}
	
	private static GroupList initGroups(DfaState[] table) {
		Group finalGroup = new Group();
		Group nonFinalGroup = new Group();	
		for (DfaState dstate : table) {
			if (dstate.isFinalState()) {
				finalGroup.add(dstate);
			} else {
				nonFinalGroup.add(dstate);
			}
		}
		return new GroupList(nonFinalGroup, finalGroup);
	}
	
	private static DfaState rebuild(Dfa dfa, DfaState[] table, GroupList groupList) {
		final int CHAR_SET_COUNT = dfa.getStart().getAllTransitions().length;
		HashMap<Group, DfaState> groupMap = new HashMap<>(groupList.size());
		
		for (DfaState dstate : table) {
			Group fromGroup = groupList.get(dstate);
			DfaState fromState = getStateFromMap(groupMap, fromGroup, dstate);
			DfaState[] ts = dstate.getAllTransitions();
			for (int i = 0; i < ts.length; i ++) {
				if (ts[i] == null) continue;
				Group group = groupList.get(ts[i]);
				DfaState toState = groupMap.get(group);
				if (toState == null) {
					toState = new DfaState(CHAR_SET_COUNT, ts[i].isFinalState());
					groupMap.put(group, toState);
				} else if (ts[i].isFinalState()) {
					toState.isFinal = true;
				}
				fromState.addTransition(i+1, toState);
			}
		}
		return groupMap.get(groupList.get(dfa.getStart()));
	}

	private static DfaState getStateFromMap(HashMap<Group, DfaState> groupMap, Group group, DfaState dstate) {
		final int CHAR_SET_COUNT = dstate.getAllTransitions().length;
		DfaState state = groupMap.get(group);
		if (state == null) {
			state = new DfaState(CHAR_SET_COUNT, dstate.isFinalState());
			groupMap.put(group, state);
		} else if (dstate.isFinalState()) {
			state.isFinal = true;
		}
		return state;
	}
}
