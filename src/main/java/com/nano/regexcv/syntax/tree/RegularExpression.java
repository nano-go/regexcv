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
package com.nano.regexcv.syntax.tree;

import com.nano.regexcv.util.CharacterRange;
import java.util.List;

public abstract class RegularExpression {

  public abstract <Out> Out accept(RTreeVisitor<Out> visitor);

  /**
   * A class that extends {@code TermExpr} means the class is a leaf node in a regular expression
   * tree.
   */
  public abstract static class TermExpr extends RegularExpression {

    /**
     * A term expr can be treated as one or more chatacter ranges.
     *
     * <p>For Example:
     *
     * <ul>
     *   <li>A single character 's' will be treated as a range {@code s-s}
     *   <li>A character list 'a,h,d' will be treated as a range list {@code [a-a, h-h, d-d]}.
     * </ul>
     *
     * <p>This method is useful for parsing chatacter classes like '[\w\da-z]'.
     */
    public abstract List<CharacterRange> toCharRangeList();
  }
}
