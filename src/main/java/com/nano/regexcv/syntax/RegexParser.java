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
package com.nano.regexcv.syntax;

import com.nano.regexcv.Pass;
import com.nano.regexcv.syntax.tree.RCharList;
import com.nano.regexcv.syntax.tree.RCharRange;
import com.nano.regexcv.syntax.tree.RCharRangeList;
import com.nano.regexcv.syntax.tree.RChoice;
import com.nano.regexcv.syntax.tree.RContatenation;
import com.nano.regexcv.syntax.tree.ROneOrMore;
import com.nano.regexcv.syntax.tree.ROptional;
import com.nano.regexcv.syntax.tree.RSingleCharacter;
import com.nano.regexcv.syntax.tree.RZeroOrMore;
import com.nano.regexcv.syntax.tree.RegularExpression;
import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharsNumTableBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RegexParser implements Pass<String, RTreeWithTable> {

  private static final char EOF = (char) -1;

  private int p;
  private char[] chars;
  private char ch;

  private CharsNumTableBuilder tableBuilder;

  @Override
  public RTreeWithTable accept(String regex) {
    this.p = 0;
    this.ch = 0;
    this.tableBuilder = new CharsNumTableBuilder();
    this.chars = regex.toCharArray();
    if (this.chars.length == 0 || this.chars[chars.length - 1] != EOF) {
      this.chars = Arrays.copyOf(this.chars, this.chars.length + 1);
      this.chars[chars.length - 1] = EOF;
    }
    advance();
    RegularExpression tree = parseRegex();
    if (!isEnd()) {
      error("Unexpecting the character '%s'.", ch);
    }
    var table = this.tableBuilder.build();
    this.tableBuilder = null;
    this.chars = null;
    return new RTreeWithTable(tree, table);
  }

  private void error(String format, Object... args) {
    throw new ParserException("SyntaxError: " + String.format(format, args));
  }

  private void advance() {
    if (isEnd()) {
      return;
    }
    ch = chars[p++];
  }

  private boolean isEnd() {
    return ch == EOF;
  }

  private void match(char expectedChar, String errfmt, Object... args) {
    if (ch == expectedChar) {
      advance();
      return;
    }
    error(errfmt, args);
  }

  private boolean got(char ch) {
    if (this.ch == ch) {
      advance();
      return true;
    }
    return false;
  }

  // Factory Methods

  private RCharRangeList newCharRangeList(char... chs) {
    CharacterRange[] charRanges = new CharacterRange[chs.length / 2];
    for (int i = 0; i < chs.length; i += 2) {
      charRanges[i / 2] = new CharacterRange(chs[i], chs[i + 1]);
      this.tableBuilder.addCharRange(chs[i], chs[i + 1]);
    }
    return new RCharRangeList(charRanges);
  }

  private RCharList newCharList(char... chs) {
    for (char ch : chs) {
      this.tableBuilder.addChar(ch);
    }
    return new RCharList(chs);
  }

  private RCharRange newCharRange(char from, char to) {
    this.tableBuilder.addCharRange(from, to);
    return new RCharRange(from, to);
  }

  private RSingleCharacter newCharExpr(char ch) {
    this.tableBuilder.addChar(ch);
    return new RSingleCharacter(ch);
  }

  // Parsing Methods

  /**
   * Parse a regular expression.
   *
   * <p>A regular expression is concatenated by multiple sub-expressions.
   */
  private RegularExpression parseRegex() {
    LinkedList<RegularExpression> concatenation = new LinkedList<>();
    while (!isEnd() && ch != ')') {
      concatenation.add(parseChoice());
    }
    if (concatenation.size() == 1) {
      return concatenation.get(0);
    }
    return new RContatenation(concatenation);
  }

  /**
   * Attempt to parse multiple choice-expressions.
   *
   * <pre>{@code
   * Syntax: Term ( '|' Term )*
   * }</pre>
   */
  private RegularExpression parseChoice() {
    RegularExpression regex = parseTerm();
    if (ch != '|') {
      return regex;
    }
    advance();
    List<RegularExpression> regexList = new ArrayList<>();
    regexList.add(regex);
    do {
      regex = parseTerm();
      regexList.add(regex);
    } while (got('|'));
    return new RChoice(regexList);
  }

  /**
   * Parse Term.
   *
   * <pre>{@code
   * Syntax:
   *  (  '(' Regex ')'              # Parenthesis expression
   *   | Character class            # Like [abc-f]
   *   | '.'                        # Any character
   *   | Character or escape mate   # \w \n... or single character like 'a'
   *  ) Quantifier
   * }</pre>
   */
  private RegularExpression parseTerm() {
    RegularExpression regex = null;
    switch (ch) {
      case '(':
        {
          advance();
          regex = parseRegex();
          match(')', "Parenthesis expression missing closing ')'.");
          break;
        }

      case '[':
        {
          advance();
          regex = parseCharClass();
          break;
        }

      case '.':
        {
          advance();
          regex = newCharRange((char) 0, Character.MAX_VALUE);
          break;
        }

      case ')':
        {
          error("Unmacthed parenthesis.");
          break;
        }

      case '*', '+', '?':
        {
          error("Invalid target for quantifier '%s'.", ch);
          break;
        }

      case '|':
        {
          error("Unexpecting the character '|'.");
          break;
        }

      case EOF:
        {
          error("Unexpecting EOF.", ch);
          break;
        }

      default:
        {
          regex = parseCharacterOrMetaEscape();
        }
    }
    return parseQuantifier(regex);
  }

  /**
   * Parse Character Class.
   *
   * <pre>{@code
   * Syntax: '[' CharRange* ']'
   * }</pre>
   */
  private RCharRangeList parseCharClass() {
    List<CharacterRange> ranges = new ArrayList<>();
    while (this.ch != ']' && this.ch != EOF) {
      var termExpr = parseCharRange();
      ranges.addAll(termExpr.toCharRangeList());
    }
    match(']', "Character class missing closing bracket.");
    return new RCharRangeList(ranges);
  }

  /** Attempt to parse a charcater range like {@code 'a-z', '0-9'}. */
  private RegularExpression.TermExpr parseCharRange() {
    var left = parseCharacterOrMetaEscape();
    // Support syntax '[\w-a]'
    if (!checkIsChar(left) || !got('-')) {
      return left;
    }
    char start = ((RSingleCharacter) left).getChar();
    // Support syntax '[a-]'
    if (this.ch == ']') {
      return newCharList(start, '-');
    }
    var right = parseCharacterOrMetaEscape();
    // Support syntax '[a-\w]'
    if (!checkIsChar(right)) {
      return new RCharRangeList(left, newCharExpr('-'), right);
    }
    char end = ((RSingleCharacter) right).getChar();
    if (end < start) {
      error("Range out of order in character class.");
    }
    return newCharRange(start, end);
  }

  private boolean checkIsChar(RegularExpression expr) {
    return expr instanceof RSingleCharacter;
  }

  /** Parse a character or a meta escape character. */
  private RegularExpression.TermExpr parseCharacterOrMetaEscape() {
    if (got('\\')) {
      return parseMetaEscape();
    }
    char ch = this.ch;
    advance();
    return newCharExpr(ch);
  }

  /** Parse meta escape. */
  private RegularExpression.TermExpr parseMetaEscape() {
    RegularExpression.TermExpr regex;
    switch (this.ch) {
      case 'w':
        regex = newCharRangeList('a', 'z', 'A', 'Z', '0', '9', '_', '_');
        break;
      case 's':
        regex = newCharList(' ', '\t', '\r', '\n', '\f');
        break;
      case 'd':
        regex = newCharRange('0', '9');
        break;
      default:
        return newCharExpr(parseMetaEscapeExceptSet());
    }
    advance(); // Consume 'w' or 's' or 'd'
    return regex;
  }

  /** Parse meta escape except set like '\w', '\d'... */
  private char parseMetaEscapeExceptSet() {
    var escapeCh =
        switch (this.ch) {
          case 'n' -> '\n';
          case 'r' -> '\r';
          case 'f' -> '\f';
          case 't' -> '\t';
          case 'b' -> '\b';
          case '\\', '.', '(', ')', '[', ']', '*', '+', '?', '|' -> ch;
          default -> EOF;
        };

    if (escapeCh == EOF) {
      error("Illegal escape character '" + ch + "'.");
    } else {
      advance();
    }
    return escapeCh;
  }

  /** Parse Quantifier. */
  private RegularExpression parseQuantifier(RegularExpression regex) {
    switch (this.ch) {
      case '*':
        advance();
        return new RZeroOrMore(regex);
      case '+':
        advance();
        return new ROneOrMore(regex);
      case '?':
        advance();
        return new ROptional(regex);
    }
    return regex;
  }
}
