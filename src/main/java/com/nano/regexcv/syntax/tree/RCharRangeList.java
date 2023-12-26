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
import java.util.Arrays;
import java.util.List;

public class RCharRangeList extends RegularExpression.TermExpr {

  private CharacterRange[] charRanges;

  public RCharRangeList(RegularExpression.TermExpr... termExprs) {
    this(
        Arrays.stream(termExprs)
            .flatMap(term -> term.toCharRangeList().stream())
            .toArray(CharacterRange[]::new));
  }

  public RCharRangeList(List<CharacterRange> list) {
    this(list.toArray(CharacterRange[]::new));
  }

  public RCharRangeList(CharacterRange[] charRanges) {
    this.charRanges = CharacterRange.mergeOverlappedRanges(charRanges);
  }

  public CharacterRange[] getCharacterRanges() {
    return this.charRanges;
  }

  @Override
  public <Out> Out accept(RTreeVisitor<Out> visitor) {
    return visitor.visit(this);
  }

  @Override
  public List<CharacterRange> toCharRangeList() {
    return List.of(charRanges);
  }
}
