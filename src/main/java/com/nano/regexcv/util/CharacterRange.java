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
package com.nano.regexcv.util;

public class CharacterRange implements Comparable<CharacterRange> {

  public static final CharacterRange EPSILON = new CharacterRange('ε', 'ε');

  public static final CharacterRange RANGE_a_z = new CharacterRange('a', 'z');
  public static final CharacterRange RANGE_A_Z = new CharacterRange('A', 'Z');
  public static final CharacterRange RANGE_0_9 = new CharacterRange('0', '9');
  public static final CharacterRange RANGE_ANY = new CharacterRange((char) 0, Character.MAX_VALUE);

  public static CharacterRange of(char from, char to) {
    return new CharacterRange(from, to);
  }

  public static CharacterRange of(char ch) {
    return new CharacterRange(ch, ch);
  }

  public static int hashCode(char from, char to) {
    return from * 31 + to;
  }

  public final char from;
  public final char to;

  public CharacterRange(char from, char to) {
    this.from = from;
    this.to = to;
  }

  public char getFrom() {
    return from;
  }

  public char getTo() {
    return to;
  }

  public boolean contains(char ch) {
    return ch >= from && ch <= to;
  }

  @Override
  public int hashCode() {
    return hashCode(from, to);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof CharacterRange) {
      CharacterRange range = (CharacterRange) obj;
      return from == range.from && to == range.to;
    }
    return false;
  }

  public boolean equals(char from, char to) {
    return this.from == from && this.to == to;
  }

  @Override
  public int compareTo(CharacterRange range) {
    if (this.from > range.from) return 1;
    if (this.from < range.from) return -1;
    return 0;
  }

  @Override
  public String toString() {
    String label;
    if (from == to) {
      label = Display.escapingChar(from);
    } else {
      label = String.format("%s-%s", Display.escapingChar(from), Display.escapingChar(to));
    }
    return label;
  }
}
