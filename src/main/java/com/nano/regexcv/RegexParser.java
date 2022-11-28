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
package com.nano.regexcv;

import com.nano.regexcv.util.CharacterRange;
import com.nano.regexcv.util.CharacterRangeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RegexParser {

  private static final char EOF = (char) -1;

  private static boolean isMetaChar(char ch) {
    switch (ch) {
      case '(':
      case ')':
      case '|':
      case '*':
      case '+':
      case '?':
      case '[':
      case ']':
        return true;
    }
    return false;
  }

  private int p;
  private char[] chars;
  private char ch;

  private CharacterRangeSet charRangeSet;
  private CharacterClass charClass;

  public RegularExpression parse(String regex) throws ParserException {
    this.p = 0;
    this.ch = 0;
    this.chars = regex.toCharArray();
    this.charRangeSet = new CharacterRangeSet();
    this.charClass = new CharacterClass();
    if (this.chars.length == 0 || this.chars[chars.length - 1] != EOF) {
      this.chars = Arrays.copyOf(this.chars, this.chars.length + 1);
      this.chars[chars.length - 1] = EOF;
    }
    advance();
    RegularExpression r = parse();
    if (!isEnd()) {
      error("Unexpecting the character '%s'.", ch);
    }
    charClass.generateTable(charRangeSet);
    charClass = null;
    charRangeSet = null;
    chars = null;
    return r;
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

  private void match(char expectedChar, String format, Object... args) {
    if (ch == expectedChar) {
      advance();
      return;
    }
    error(format, args);
  }

  private boolean got(char ch) {
    if (this.ch == ch) {
      advance();
      return true;
    }
    return false;
  }

  private <T extends RegularExpression> T newRegex(T regex) {
    regex.setCharacterClass(this.charClass);
    return regex;
  }

  private RegularExpression newCharRangeList(char... chs) {
    CharacterRange[] charRanges = new CharacterRange[chs.length / 2];
    for (int i = 0; i < chs.length; i += 2) {
      charRanges[i / 2] = new CharacterRange(chs[i], chs[i + 1]);
      charRangeSet.addRange(chs[i], chs[i + 1]);
    }
    return newRegex(new RCharRangeList(charRanges));
  }

  private RegularExpression newCharList(char... chs) {
    for (char ch : chs) {
      charRangeSet.addChar(ch);
    }
    return newRegex(new RCharList(chs));
  }

  private RegularExpression newCharRange(char from, char to) {
    charRangeSet.addRange(from, to);
    return newRegex(new RCharRange(from, to));
  }

  private RegularExpression parse() {
    LinkedList<RegularExpression> concatenation = new LinkedList<>();
    while (!isEnd() && ch != ')') {
      concatenation.add(parseChoice());
    }
    if (concatenation.size() == 1) {
      return concatenation.get(0);
    }
    return newRegex(new RContatenation(concatenation));
  }

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
    return newRegex(new RChoice(regexList));
  }

  private RegularExpression parseTerm() {
    RegularExpression regex = null;
    switch (ch) {
      case '(':
        {
          advance();
          regex = parse();
          match(')', "Missing ')'.");
          break;
        }

      case '[':
        {
          advance();
          regex = parseCharClass();
          match(']', "Missing ']'.");
          break;
        }

      case '.':
        {
          advance();
          regex = newCharRange((char) 0, Character.MAX_VALUE);
          break;
        }

      case '\\':
        {
          advance();
          regex = parseEscape(); // \w, \s, \d...
          break;
        }

      case EOF:
        {
          error("Unexpecting EOF.", ch);
          break;
        }

      default:
        {
          regex = parseSingleChar();
        }
    }
    return parseSuffix(regex);
  }

  private RegularExpression parseEscape() {
    char esch = EOF;
    RegularExpression regex = null;
    switch (ch) {
      case 'w':
        regex = newCharRangeList('a', 'z', 'A', 'Z', '0', '9');
        break;
      case 's':
        regex = newCharList(' ', '\t', '\r', '\n', '\f');
        break;
      case 'd':
        regex = newCharRange('0', '9');
        break;

      case 'n':
        esch = '\n';
        break;
      case 'r':
        esch = '\r';
        break;
      case 'f':
        esch = '\f';
        break;
      case 't':
        esch = '\t';
        break;
      case 'b':
        esch = '\b';
        break;

      case '\\':
      case '.':
      case '(':
      case ')':
      case '[':
      case ']':
      case '*':
      case '+':
      case '?':
        esch = ch;
        break;

      default:
        error("Illegal escape character '" + ch + "'.");
    }
    if (esch != EOF) {
      charRangeSet.addChar(esch);
      regex = newRegex(new RSingleCharacter(esch));
    }
    advance();
    return regex;
  }

  private RegularExpression parseCharClass() {
    List<RegularExpression> choiceList = new ArrayList<>();
    while (this.ch != ']' && this.ch != EOF) {
      char from = this.ch;
      advance();
      if (this.ch != '-') {
        charRangeSet.addChar(from);
        choiceList.add(newRegex(new RSingleCharacter(from)));
        continue;
      }
      advance();
      if (this.ch == ']' || this.ch == EOF) {
        error("Illegal char class.");
      }
      choiceList.add(newCharRange(from, this.ch));
      advance();
    }
    return newRegex(new RChoice(choiceList));
  }

  private RegularExpression parseSingleChar() {
    if (isMetaChar(ch)) {
      error("Unexpecting the meta character '%s'.", ch);
    }
    char ch = this.ch;
    charRangeSet.addChar(ch);
    advance();
    return newRegex(new RSingleCharacter(ch));
  }

  private RegularExpression parseSuffix(RegularExpression regex) {
    switch (ch) {
      case '*':
        advance();
        return newRegex(new RZeroOrMore(regex));
      case '+':
        advance();
        return newRegex(new ROneOrMore(regex));
      case '?':
        advance();
        return newRegex(new ROptional(regex));
    }
    return regex;
  }
}
