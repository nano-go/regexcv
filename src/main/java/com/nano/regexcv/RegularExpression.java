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

public abstract class RegularExpression {

  private CharacterClass characterClass;

  protected void setCharacterClass(CharacterClass characterClass) {
    this.characterClass = characterClass;
  }

  protected CharacterClass getCharClass() {
    return characterClass;
  }

  protected int getCharClassNumber(char ch) {
    return characterClass.getClassNumber(ch);
  }

  protected int getMaxClassNumber() {
    return characterClass.getTableSize();
  }

  public abstract Nfa generateNfa();
}
