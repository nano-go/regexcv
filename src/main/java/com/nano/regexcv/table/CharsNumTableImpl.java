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
package com.nano.regexcv.table;

import com.nano.regexcv.util.CharacterRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CharsNumTableImpl implements ICharsNumTable {

  private ArrayList<CharacterRange> table;

  private HashMap<Character, Integer> leftCharsMap;
  private HashMap<Character, Integer> rightCharsMap;

  protected CharsNumTableImpl(ArrayList<CharacterRange> table) {
    this.table = table;
    this.leftCharsMap = new HashMap<>();
    this.rightCharsMap = new HashMap<>();

    var size = table.size();
    for (int i = 0; i < size; i++) {
      var range = table.get(i);
      leftCharsMap.put(range.from, i + 1);
      rightCharsMap.put(range.to, i + 1);
    }
  }

  @Override
  public int getTableSize() {
    return table.size();
  }

  @Override
  public int getNumOfChar(char ch) {
    var nums = getNumsOfCharRange(ch, ch);
    if (nums.isEmpty()) {
      return ICharsNumTable.INVALID_CHAR_NUM;
    }
    return nums.get().start;
  }

  @Override
  public Optional<NumInterval> getNumsOfCharRange(CharacterRange range) {
    return getNumsOfCharRange(range.from, range.to);
  }

  @Override
  public Optional<NumInterval> getNumsOfCharRange(char from, char to) {
    var leftNum = this.leftCharsMap.get(from);
    var rightNum = this.rightCharsMap.get(to);
    if (leftNum == null || rightNum == null) {
      return Optional.empty();
    }
    return Optional.of(new NumInterval(leftNum, rightNum));
  }

  @Override
  public CharacterRange getCharRangeOfNum(int num) {
    return table.get(num - 1);
  }

  @Override
  public List<CharacterRange> getTable() {
    return new ArrayList<>(this.table);
  }

  @Override
  public int queryNumOfInputChar(char ch) {
    return this.binarySearch(ch);
  }

  private int binarySearch(char ch) {
    var idx = CharacterRange.binarySearch(this.table, ch);
    if (idx == -1) {
      return ICharsNumTable.INVALID_CHAR_NUM;
    }
    // Because the number 0 is always for the epsilon.
    return idx + 1;
  }
}
