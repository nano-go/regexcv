package com.nano.regexcv;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class NfaState {
	
	private HashSet<NfaState>[] transitions;
	
	private boolean isFinal;
	
	public NfaState(int charSetCount) {
		this.transitions = new HashSet[charSetCount+1];
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
		Set<NfaState> states = 
			transitions[CharacterClass.EPSILON_CHAR_CLASS];
		return states != null ? states : Collections.emptySet();
	}
	
	public HashSet<NfaState>[] getTransitions() {
		return transitions;
	}
	
	public Set<NfaState> getStateSet(int in) {
		return Objects.requireNonNullElse(
			transitions[in], Collections.<NfaState>emptySet());
	}
	
	public NfaState markFinalState() {
		this.isFinal = true;
		return this;
	}
	
	public boolean isFinalState() {
		return this.isFinal;
	}
}
