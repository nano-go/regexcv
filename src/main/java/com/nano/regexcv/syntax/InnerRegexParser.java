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

import com.nano.regexcv.syntax.tree.RAlternation;
import com.nano.regexcv.syntax.tree.RCharList;
import com.nano.regexcv.syntax.tree.RCharRange;
import com.nano.regexcv.syntax.tree.RCharRangeList;
import com.nano.regexcv.syntax.tree.RContatenation;
import com.nano.regexcv.syntax.tree.REmpty;
import com.nano.regexcv.syntax.tree.ROneOrMore;
import com.nano.regexcv.syntax.tree.ROptional;
import com.nano.regexcv.syntax.tree.RSingleCharacter;
import com.nano.regexcv.syntax.tree.RZeroOrMore;
import com.nano.regexcv.syntax.tree.RegularExpression;
import com.nano.regexcv.util.CharacterRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InnerRegexParser {

  private static final char EOF = (char) -1;

  private int p;
  private char[] chars;
  private char ch;

  protected InnerRegexParser(CharSequence regex) {
    this.p = 0;
    this.ch = 0;
    this.chars = regex.toString().toCharArray();
    if (this.chars.length == 0 || this.chars[chars.length - 1] != EOF) {
      this.chars = Arrays.copyOf(this.chars, this.chars.length + 1);
      this.chars[chars.length - 1] = EOF;
    }
    advance();
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
    }
    return new RCharRangeList(charRanges);
  }

  private RCharList newCharList(char... chs) {
    return new RCharList(chs);
  }

  private RCharRange newCharRange(char from, char to) {
    return new RCharRange(from, to);
  }

  private RSingleCharacter newCharExpr(char ch) {
    return new RSingleCharacter(ch);
  }

  // Parsing Methods

  protected RegularExpression parse() {
    var regex = parseRegex();
    if (!isEnd()) {
      error("Unexpecting the character '%s'.", ch);
    }
    return regex;
  }

  /**
   * Parse a regular expression.
   *
   * <p>A regular expression is concatenated by multiple sub-expressions.
   */
  protected RegularExpression parseRegex() {
    LinkedList<RegularExpression> concatenation = new LinkedList<>();
    while (!isEnd() && ch != ')') {
      concatenation.add(parseAlternation());
    }
    if (concatenation.size() == 1) {
      return concatenation.get(0);
    }
    return new RContatenation(concatenation);
  }

  /**
   * Attempt to parse multiple alternation-expressions.
   *
   * <pre>{@code
   * Syntax: ( Term ( '|' [ Term ] )* ) | ( '|' [ Term ] )+ )
   * }</pre>
   */
  private RegularExpression parseAlternation() {
    RegularExpression left = parseLeftOfAlternation();
    while (got('|')) {
      var right = parseRightOfAlternation();
      left = new RAlternation(List.of(left, right));
    }
    return left;
  }

  private RegularExpression parseLeftOfAlternation() {
    if (this.ch == '|') {
      return new REmpty();
    }
    return parseTerm();
  }

  private RegularExpression parseRightOfAlternation() {
    if (isEnd() || this.ch == ')' || this.ch == '|') {
      return new REmpty();
    }
    return parseTerm();
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
