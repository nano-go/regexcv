package com.nano.regexcv;
import java.util.HashMap;

public class DfaState {
    
	private DfaState[] transitions;
    protected boolean isFinal;

	public DfaState(int charSetCount, boolean isFinal) {
		this.transitions = new DfaState[charSetCount];
		this.isFinal = isFinal;
	}
	
    public void addTransition(int charClass, DfaState state) {
		transitions[charClass-1] = state;
	}
	
	public DfaState getState(int charClass) {
		return transitions[charClass-1];
	}
	
	public DfaState[] getAllTransitions() {
		return transitions;
	}
	
	public boolean isFinalState() {
		return this.isFinal;
	}
}
