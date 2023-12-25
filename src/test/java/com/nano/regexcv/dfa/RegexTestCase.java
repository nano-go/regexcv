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
package com.nano.regexcv.dfa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class RegexTestCase {

  static final Pattern REGEX_PARSING_PATTERNS = Pattern.compile("\\s*/([^/]*)/[\\s,]*");
  static final Pattern REGEX_PARSING_DATALINE = Pattern.compile("\\s*\"([^\"]*)\"[\\s,]*");

  // Read resouece file and parse it.
  protected static RegexTestCase[] parseFile(String filename) throws IOException {
    var path = RegexTestCase.class.getClassLoader().getResource(filename).getFile();
    try (var fis = new FileInputStream(new File(path))) {
      return parseContent(new String(fis.readAllBytes()));
    }
  }

  protected static RegexTestCase[] parseContent(String content) {
    // Ignore empty lines and comments.
    var lines =
        Arrays.stream(content.split("\n+"))
            .map(line -> line.trim())
            .filter(line -> line.length() != 0)
            .filter(line -> !line.startsWith("//") && !line.startsWith("#"))
            .toArray(String[]::new);
    var cases = new ArrayList<RegexTestCase>();
    for (int i = 0; i < lines.length; i += 3) {
      var patterns = lines[i].substring("PATTERN:".length());
      var matched = lines[i + 1].substring("MATCHES:".length());
      var unmatched = lines[i + 2].substring("UNMATCH:".length());
      cases.add(parseTestCase(patterns, matched, unmatched));
    }
    return cases.toArray(RegexTestCase[]::new);
  }

  private static RegexTestCase parseTestCase(
      String patternLine, String matchedStrsLine, String unmatchedStrsLine) {
    var patterns = parseByPattern(patternLine, REGEX_PARSING_PATTERNS, false);
    var matchedStrs = parseByPattern(matchedStrsLine, REGEX_PARSING_DATALINE, true);
    var unmatchedStrs = parseByPattern(unmatchedStrsLine, REGEX_PARSING_DATALINE, true);
    return new RegexTestCase(patterns, matchedStrs, unmatchedStrs);
  }

  // Parse line by specified pattern.
  private static String[] parseByPattern(String input, Pattern pattern, boolean escape) {
    var result = new ArrayList<>();
    var matcher = pattern.matcher(input);
    while (matcher.find()) {
      var item = matcher.group(1);
      if (escape) {
        item = parseEscapeChars(item);
      }
      result.add(item);
    }
    return result.toArray(String[]::new);
  }

  private static String parseEscapeChars(String str) {
    var buf = new StringBuilder();
    var chars = str.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      var ch = chars[i];
      if (ch != '\\' || i == chars.length - 1) {
        buf.append(ch);
        continue;
      }
      ch =
          switch (chars[i + 1]) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case '\\' -> '\\';
            default -> chars[i + 1];
          };
      i++;
      buf.append(ch);
    }
    return buf.toString();
  }

  protected DfaPattern[] patterns;
  protected String[] strsShouldBeMatched;
  protected String[] strsShouldNotBeMatched;

  public RegexTestCase(String[] patterns, String[] matchedStrs, String[] unmacthedStrs) {
    this.patterns = Arrays.stream(patterns).map(DfaPattern::new).toArray(DfaPattern[]::new);
    this.strsShouldBeMatched = matchedStrs;
    this.strsShouldNotBeMatched = unmacthedStrs;
  }
}
