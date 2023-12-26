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
import java.util.ArrayList;
import java.util.List;

public class RCharList extends RegularExpression.TermExpr {

  private char[] chs;

  public RCharList(char... chs) {
    this.chs = chs;
  }

  public char[] getChars() {
    return this.chs;
  }

  @Override
  public <Out> Out accept(RTreeVisitor<Out> visitor) {
    return visitor.visit(this);
  }

  @Override
  public List<CharacterRange> toCharRangeList() {
    var list = new ArrayList<CharacterRange>(chs.length);
    for (char ch : this.chs) {
      list.add(new CharacterRange(ch, ch));
    }
    return list;
  }
}
