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
package com.nano.regexcv.util;

/** This class represents a string that can be displayed. */
public final class Display {

  public static String escapingString(String str) {
    var buf = new StringBuilder();
    for (char ch : str.toCharArray()) {
      buf.append(escapingChar(ch));
    }
    return buf.toString();
  }

  public static String escapingChar(char ch) {
    return switch (ch) {
      case '\n' -> "\\n";
      case '\r' -> "\\r";
      case '\f' -> "\\f";
      case '\b' -> "\\b";
      case '\t' -> "\\t";
      default -> {
        if (!(ch < 127 && ch >= 0x21) && (ch == 0 || !Character.isUnicodeIdentifierPart(ch))) {
          yield "0x" + Integer.toHexString(ch);
        } else {
          yield String.valueOf(ch);
        }
      }
    };
  }

  private String str;

  public Display(Object any) {
    this.str = escapingString(any.toString());
  }

  public String getString() {
    return str;
  }

  @Override
  public String toString() {
    return str;
  }

  @Override
  public int hashCode() {
    return str.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Display other = (Display) obj;
    if (str == null) {
      if (other.str != null) return false;
    } else if (!str.equals(other.str)) return false;
    return true;
  }
}
