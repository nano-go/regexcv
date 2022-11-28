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

public class DfaState {

  private DfaState[] transitions;
  protected boolean isFinal;

  public DfaState(int charSetCount, boolean isFinal) {
    this.transitions = new DfaState[charSetCount];
    this.isFinal = isFinal;
  }

  public void addTransition(int charClass, DfaState state) {
    transitions[charClass - 1] = state;
  }

  public DfaState getState(int charClass) {
    return transitions[charClass - 1];
  }

  public DfaState[] getAllTransitions() {
    return transitions;
  }

  public boolean isFinalState() {
    return this.isFinal;
  }
}
