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
package com.nano.regexcv.nfa;

import com.nano.regexcv.Pass;
import com.nano.regexcv.syntax.tree.RAlternation;
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
import com.nano.regexcv.table.ICharsNumTable;
import com.nano.regexcv.table.RTreeWithTable;

public class RExpTree2NfaPass implements RTreeVisitor<Nfa>, Pass<RTreeWithTable, Nfa> {

  private ICharsNumTable table;

  @Override
  public Nfa accept(RTreeWithTable input) {
    this.table = input.table;
    var nfa = input.tree.accept(this);
    this.table = null;
    return nfa;
  }

  @Override
  public Nfa visit(RSingleCharacter node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();
    start.addTransition(table.getNumOfChar(node.getChar()), end);
    return nfa;
  }

  @Override
  public Nfa visit(RCharRange node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();

    var interval = table.getNumsOfCharRange(node.getFromChar(), node.getToChar()).get();
    for (int i = interval.start; i <= interval.end; i++) {
      start.addTransition(i, end);
    }

    return nfa;
  }

  @Override
  public Nfa visit(RCharRangeList node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();

    var charRanges = node.getCharacterRanges();
    for (var range : charRanges) {
      var interval = table.getNumsOfCharRange(range.getFrom(), range.getTo()).get();
      for (int i = interval.start; i <= interval.end; i++) {
        start.addTransition(i, end);
      }
    }
    if (charRanges.length == 0) {
      start.addEmptyTransition(end);
    }

    return nfa;
  }

  @Override
  public Nfa visit(RAlternation node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();

    var regexList = node.getRegexList();
    if (!regexList.isEmpty()) {
      for (RegularExpression regex : regexList) {
        var subnfa = regex.accept(this);
        subnfa.getEnd().unmarkFinalState();
        start.addEmptyTransition(subnfa.getStart());
        subnfa.getEnd().addEmptyTransition(end);
      }
    } else {
      start.addEmptyTransition(end);
    }

    return nfa;
  }

  @Override
  public Nfa visit(RContatenation node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();

    var last = start;
    for (RegularExpression regex : node.getRegexList()) {
      Nfa subnfa = regex.accept(this);
      subnfa.getEnd().unmarkFinalState();
      last.addEmptyTransition(subnfa.getStart());
      last = subnfa.getEnd();
    }
    last.addEmptyTransition(end);

    return nfa;
  }

  @Override
  public Nfa visit(REmpty node) {
    var nfa = new Nfa(table);
    var start = nfa.getStart();
    var end = nfa.getEnd();
    start.addEmptyTransition(end);
    return nfa;
  }

  @Override
  public Nfa visit(ROneOrMore node) {
    var nfa = node.getQuiantifiedNode().accept(this);
    nfa.getEnd().addEmptyTransition(nfa.getStart());
    return nfa;
  }

  @Override
  public Nfa visit(ROptional node) {
    var nfa = node.getQuiantifiedNode().accept(this);
    nfa.getStart().addEmptyTransition(nfa.getEnd());
    return nfa;
  }

  @Override
  public Nfa visit(RZeroOrMore node) {
    var nfa = node.getQuiantifiedNode().accept(this);
    var start = nfa.getStart();
    var end = nfa.getEnd();
    start.addEmptyTransition(end);
    end.addEmptyTransition(start);
    return nfa;
  }
}
