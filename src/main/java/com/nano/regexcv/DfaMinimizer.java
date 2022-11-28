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

    public void move(Group src, Group dest, DfaState s) {
      src.remove(s);
      dest.add(s);
      groupMap.put(s, dest);
    }

    public void addGroup(Group newGroup) {
      this.groupList.add(newGroup);
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

    public boolean equals(DfaState s, DfaState t) {
      if (s == t) return true;
      return groupMap.get(s) == groupMap.get(t);
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

  public static Dfa minimizeDfa(Dfa dfa) {
    DfaState[] table = dfa.getAllStates();
    GroupList groupList = initGroups(table);
    while (partition(dfa, groupList))
      ;
    return rebuild(dfa, table, groupList);
  }

  private static boolean partition(Dfa dfa, GroupList groupList) {
    final int CHAR_SET_COUNT = dfa.getStart().getAllTransitions().length;
    boolean paritionFlag = false;
    for (int i = 0; i < groupList.size(); i++) {
      Group group = groupList.get(i);
      if (group.size() <= 1) {
        continue;
      }
      DfaState[] dstates = group.getDfaStates();
      for (int in = 1; in <= CHAR_SET_COUNT; in++) {
        if (partition(groupList, group, dstates, in)) {
          paritionFlag = true;
          break;
        }
      }
    }
    return paritionFlag;
  }

  private static boolean partition(GroupList groupList, Group group, DfaState[] dstates, int in) {
    int size = dstates.length;
    DfaState firstDstate = dstates[0];
    Group newGroup = null;
    for (int i = 1; i < size; i++) {
      DfaState s = dstates[i];
      if (!groupList.equals(firstDstate.getState(in), s.getState(in))) {
        if (newGroup == null) {
          newGroup = new Group();
          groupList.addGroup(newGroup);
        }
        groupList.move(group, newGroup, s);
      }
    }
    return newGroup != null;
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

  private static Dfa rebuild(Dfa dfa, DfaState[] table, GroupList groupList) {
    HashMap<Group, DfaState> groupMap = new HashMap<>(groupList.size());

    for (DfaState dstate : table) {
      Group fromGroup = groupList.get(dstate);
      DfaState fromState = getStateFromMap(groupMap, fromGroup, dstate);
      DfaState[] ts = dstate.getAllTransitions();
      for (int i = 0; i < ts.length; i++) {
        if (ts[i] == null) continue;
        Group group = groupList.get(ts[i]);
        DfaState toState = getStateFromMap(groupMap, group, ts[i]);
        fromState.addTransition(i + 1, toState);
      }
    }

    return new Dfa(groupMap.get(groupList.get(dfa.getStart())));
  }

  private static DfaState getStateFromMap(
      HashMap<Group, DfaState> groupMap, Group group, DfaState dstate) {
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
