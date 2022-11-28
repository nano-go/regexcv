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

public class CharacterSet {

  private boolean[] charMarked;

  public void add(char ch) {
    if (charMarked == null) {
      charMarked = new boolean[Character.MAX_VALUE + 1];
    }
    charMarked[ch] = true;
  }

  public void remove(char ch) {
    if (charMarked == null) {
      return;
    }
    charMarked[ch] = false;
  }

  public void clear() {
    charMarked = null;
  }

  public boolean contains(char ch) {
    return charMarked != null ? charMarked[ch] : false;
  }

  /** If the given char is not in this set, the char will be added into this set. */
  public boolean containsOrAdd(char ch) {
    if (!contains(ch)) {
      add(ch);
      return false;
    }
    return true;
  }
}
