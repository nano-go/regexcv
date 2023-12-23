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

import java.util.List;
import java.util.Optional;

public interface ICharsNumTable {

  public static final int INVALID_CHAR_NUM = -1;
  public static final int EPSILON_CHAR_NUM = 0;

  public static class NumInterval {
    public int start;
    public int end;

    public NumInterval(int start, int end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof NumInterval interval) {
        return this.start == interval.start && this.end == interval.end;
      }
      return super.equals(obj);
    }
  }

  public int getTableSize();

  public int getNumOfChar(char ch);

  public Optional<NumInterval> getNumsOfCharRange(CharacterRange range);

  public Optional<NumInterval> getNumsOfCharRange(char from, char to);

  public CharacterRange getCharRangeOfNum(int num);

  public List<CharacterRange> getTable();

  public int queryNumOfInputChar(char ch);
}
