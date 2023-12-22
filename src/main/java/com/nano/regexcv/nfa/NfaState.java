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
package com.nano.regexcv.nfa;

import com.nano.regexcv.util.CharacterClass;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NfaState {

  private HashSet<NfaState>[] transitions;

  private boolean isFinal;

  public NfaState(int charSetCount) {
    this.transitions = new HashSet[charSetCount + 1];
  }

  public void addEmptyTransition(NfaState state) {
    addTransition(CharacterClass.EPSILON_CHAR_CLASS, state);
  }

  public void addTransition(int charClass, NfaState state) {
    if (this.transitions[charClass] == null) {
      this.transitions[charClass] = new HashSet<>();
    }
    this.transitions[charClass].add(state);
  }

  public void addTransitions(int charClass, Collection<NfaState> states) {
    if (this.transitions[charClass] == null) {
      this.transitions[charClass] = new HashSet<>();
    }
    this.transitions[charClass].addAll(states);
  }

  public void removeEpsilonTransitions() {
    transitions[CharacterClass.EPSILON_CHAR_CLASS] = null;
  }

  public Set<NfaState> getEpsilonTransitions() {
    Set<NfaState> states = transitions[CharacterClass.EPSILON_CHAR_CLASS];
    return states != null ? states : Collections.emptySet();
  }

  public HashSet<NfaState>[] getTransitions() {
    return transitions;
  }

  public Set<NfaState> getStateSet(int in) {
    return Objects.requireNonNullElse(transitions[in], Collections.<NfaState>emptySet());
  }

  public NfaState markFinalState() {
    this.isFinal = true;
    return this;
  }

  public NfaState unmarkFinalState() {
    this.isFinal = false;
    return this;
  }

  public boolean isFinalState() {
    return this.isFinal;
  }
}
