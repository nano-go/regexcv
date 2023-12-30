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
import com.nano.regexcv.util.CharacterRanges;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InnerRegexParser {

  private static final CharacterRange[] WORD_RANGES = CharacterRanges.parse("a-zA-Z0-9_");
  private static final CharacterRange[] NON_WORD_RANGES = CharacterRanges.parse("^a-zA-Z0-9_");
  private static final CharacterRange[] DIGIT_RANGES = CharacterRanges.parse("0-9");
  private static final CharacterRange[] NON_DIGIT_RANGES = CharacterRanges.parse("^0-9");
  private static final CharacterRange[] SPACE_RANGES = CharacterRanges.parse(" \n\r\f\t");
  private static final CharacterRange[] NON_SPACE_RANGES = CharacterRanges.parse("^ \n\r\f\t");

  private static final char EOF = (char) -1;

  private int p;
  private char[] chars;
  private char ch;

  protected InnerRegexParser(CharSequence regex) {
    this.p = 0;
    this.ch = 0;
    this.chars = regex.toString().toCharArray();
    advance();
  }

  private void error(String format, Object... args) {
    throw new RegexSyntaxErrorException("SyntaxError: " + String.format(format, args));
  }

  private void advance() {
    if (this.p == this.chars.length) {
      ch = EOF;
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

  private RCharRangeList newCharRangeList(CharacterRange... ranges) {
    return new RCharRangeList(false, ranges);
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
    var list = new ArrayList<RegularExpression>();
    list.add(parseLeftOfAlternation());
    while (got('|')) {
      list.add(parseRightOfAlternation());
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    return new RAlternation(list);
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
   *   | Character Class            # Like [abc-f]
   *   | '.'                        # Any character
   *   | Character                  # Escape char or literal char.
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
          regex = newCharRange(Character.MIN_VALUE, Character.MAX_VALUE);
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

      case EOF:
        {
          error("Unexpecting EOF.", ch);
          break;
        }

      default:
        {
          regex = parseChar();
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
    var negated = got('^');
    while (this.ch != ']' && this.ch != EOF) {
      var termExpr = parseCharRange();
      ranges.addAll(termExpr.toCharRangeList());
    }
    match(']', "Character class missing closing bracket.");
    return new RCharRangeList(negated, ranges);
  }

  /** Attempt to parse a charcater range like {@code 'a-z', '0-9'}. */
  private RegularExpression.TermExpr parseCharRange() {
    var left = parseChar();
    // Support syntax '[\w-a]'
    if (!checkIsChar(left) || !got('-')) {
      return left;
    }
    char startCh = ((RSingleCharacter) left).getChar();
    // Support syntax '[a-]'
    if (this.ch == ']') {
      return new RCharRangeList(false, startCh, '-');
    }
    var right = parseChar();
    // Support syntax '[a-\w]'
    if (!checkIsChar(right)) {
      return new RCharRangeList(false, left, newCharExpr('-'), right);
    }
    char endCh = ((RSingleCharacter) right).getChar();
    if (endCh < startCh) {
      error("Range out of order in character class.");
    }
    return newCharRange(startCh, endCh);
  }

  private boolean checkIsChar(RegularExpression expr) {
    return expr instanceof RSingleCharacter;
  }

  /** Parse a escape char or a literal char. */
  private RegularExpression.TermExpr parseChar() {
    if (got('\\')) {
      return parseEscapeCharacter();
    }
    char ch = this.ch;
    advance();
    return newCharExpr(ch);
  }

  /** Parse escape character. */
  private RegularExpression.TermExpr parseEscapeCharacter() {
    var regex =
        switch (this.ch) {
          case 'w' -> newCharRangeList(WORD_RANGES);
          case 'W' -> newCharRangeList(NON_WORD_RANGES);
          case 's' -> newCharRangeList(SPACE_RANGES);
          case 'S' -> newCharRangeList(NON_SPACE_RANGES);
          case 'd' -> newCharRangeList(DIGIT_RANGES);
          case 'D' -> newCharRangeList(NON_DIGIT_RANGES);
          case 'n' -> newCharExpr('\n');
          case 'r' -> newCharExpr('\r');
          case 'f' -> newCharExpr('\f');
          case 't' -> newCharExpr('\t');
          case 'b' -> newCharExpr('\b');
          case '\\', '.', '(', ')', '[', ']', '*', '+', '?', '|' -> newCharExpr(ch);
          default -> {
            error("Illegal escape character '" + ch + "'.");
            yield null;
          }
        };
    advance();
    return regex;
  }

  /** Attempt to parse a quantifier. */
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
