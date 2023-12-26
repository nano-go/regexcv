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

import com.nano.regexcv.Pass;
import com.nano.regexcv.syntax.tree.RAlternation;
import com.nano.regexcv.syntax.tree.RCharList;
import com.nano.regexcv.syntax.tree.RCharRange;
import com.nano.regexcv.syntax.tree.RCharRangeList;
import com.nano.regexcv.syntax.tree.RContatenation;
import com.nano.regexcv.syntax.tree.REmpty;
import com.nano.regexcv.syntax.tree.ROneOrMore;
import com.nano.regexcv.syntax.tree.ROptional;
import com.nano.regexcv.syntax.tree.RSingleCharacter;
import com.nano.regexcv.syntax.tree.RTreeVisitor;
import com.nano.regexcv.syntax.tree.RZeroOrMore;
import com.nano.regexcv.syntax.tree.RegularExpression;

public class CharacterSetCollector
    implements Pass<RegularExpression, RTreeWithTable>, RTreeVisitor<Void> {

  CharsNumTableBuilder builder;

  @Override
  public RTreeWithTable accept(RegularExpression input) {
    builder = new CharsNumTableBuilder();
    input.accept(this);
    var result = new RTreeWithTable(input, builder.build());
    builder = null;
    return result;
  }

  @Override
  public Void visit(RSingleCharacter node) {
    builder.addChar(node.getChar());
    return null;
  }

  @Override
  public Void visit(RCharList node) {
    for (char ch : node.getChars()) {
      builder.addChar(ch);
    }
    return null;
  }

  @Override
  public Void visit(RCharRange node) {
    builder.addCharRange(node.getFromChar(), node.getToChar());
    return null;
  }

  @Override
  public Void visit(RCharRangeList node) {
    for (var range : node.getCharacterRanges()) {
      builder.addCharRange(range.from, range.to);
    }
    return null;
  }

  @Override
  public Void visit(RAlternation node) {
    for (var expr : node.getRegexList()) {
      expr.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(RContatenation node) {
    for (var expr : node.getRegexList()) {
      expr.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(REmpty node) {
    return null;
  }

  @Override
  public Void visit(ROneOrMore node) {
    node.getQuiantifiedNode().accept(this);
    return null;
  }

  @Override
  public Void visit(ROptional node) {
    node.getQuiantifiedNode().accept(this);
    return null;
  }

  @Override
  public Void visit(RZeroOrMore node) {
    node.getQuiantifiedNode().accept(this);
    return null;
  }
}
