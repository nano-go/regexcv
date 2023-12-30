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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@code ICharsNumTable} is a table that maps a character range to numbers.
 *
 * <p>For example, character ranges {@code [a-zA-Z0-9]} will build the following table:
 *
 * <pre>
 * {@code a-z: 1, A-Z: 2, 0-9: 3 }
 * </pre>
 *
 * We can use '1' to represent a-z, '2' to represent 'A-Z' and 'e' to represent '0-9'. A state in
 * NFA or DFA can accept a number instead of a character to transfer another status.
 *
 * <p>Sometimes a range may be split into multiple parts. For example:
 *
 * <pre>
 * {@code [a-z][c-f]e}
 * </pre>
 *
 * In order to ensure that both the {@code e} and the {@code c-f} are queryable in the table, the
 * range 'a-z' and 'c-f' must be split into multiple parts:
 *
 * <pre>
 * {@code a-b, c-d, e, f, g-z}
 * </pre>
 *
 * In the above table, we can use {@code 2(c-d), 3(e), 4(f)} to represent {@code c-f} and use {@code
 * 1,2,3,4,5} to represent {@code a-z}.
 *
 * <p>Another reason to use {@code ICharsNumTable} is to implement nagated character-class group
 * like:
 *
 * <pre>{@code [^b1-8]}</pre>
 *
 * We can the range {@code unicode(0)-'a'} and the range {@code'c'-Character.MAX_VALUE} to represent
 * the negated character {@code [^b]}.
 *
 * <p>Similarly, the range {@code unicode(0)-'0'} and the range {@code '9'-Character.MAX_VALUE} can
 * be used to represent the negated character range {@code [^1-8]}.
 *
 * <p>In NFA, the {@code epsilon} may be used, so the table use the special number '0' to represent
 * it.
 *
 * <p>You can use {@code CharsNumTableBuilder} to build a table object:
 *
 * <pre>{@code
 * var builder = new CharsNumTableBuilder();
 * var table = builder.addCharRange('a', 'z')
 * 		.addCharRange('c', 'f')
 * 		.addChar('e')
 * 		.build();
 * // table.getNumsOfCharRange('a', 'z') equals to [1, 5]
 * }</pre>
 */
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

    @Override
    public String toString() {
      return String.format("%s-%s", start, end);
    }

    @Override
    public int hashCode() {
      return Objects.hash(start, end);
    }
  }

  public int getTableSize();

  /**
   * Returns a number that only can be used to represent the given char or {@link
   * ICharsNumTable#INVALID_CHAR_NUM} if it does not exist.
   *
   * <p>For example:
   *
   * <pre>{@code
   * table: a-d f g h-z
   *
   * Input: 'a' Ouput: -1
   * Input: 'f' Ouput: 2
   * Input: 'g' Ouput: 3
   * }</pre>
   *
   * @param ch Any character.
   */
  public int getNumOfChar(char ch);

  /** This is same as {@link ICharsNumTable#getNumsOfCharRange(char, char)}. */
  public Optional<NumInterval> getNumsOfCharRange(CharacterRange range);

  /**
   * Returns a number interval that can be used to represent the character range {@code from-to}.
   *
   * <p>For example:
   *
   * <pre>{@code
   * table: a-d f g h-z
   *
   * Input: 'a'-'d' Output: 1-1
   * Input: 'f'-'z' Output: 2-4
   * Input: 'g'-'z' Output: 3-4
   * Input: 'g'-'h' Output: Optional.empty()
   * }</pre>
   *
   * @param from The start of the character range.
   * @param to The end of the character range.
   */
  public Optional<NumInterval> getNumsOfCharRange(char from, char to);

  public CharacterRange getCharRangeOfNum(int num);

  public List<CharacterRange> getTable();

  /**
   * Returns the number that maps a character range that contains the given character 'ch' or
   * returns {@link ICharsNumTable#INVALID_CHAR_NUM} if the character does not exist in this table.
   */
  public int queryNumOfInputChar(char ch);
}
