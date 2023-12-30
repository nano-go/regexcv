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

import com.nano.regexcv.table.ICharsNumTable;
import org.junit.Assert;

public interface IPattern {
  boolean matches(String text);

  String getPattern();

  String getInformation();

  ICharsNumTable getTable();

  default void test(boolean matched, String... input) {
    var table = this.getTable();
    for (String text : input) {
      var actual = this.matches(text);
      if (actual != matched) {
        var tag = matched ? "SHOULD MATCH" : "SHOULD NOT MATCH";
        var list = table.getTable();
        var msg =
            String.format(
                "%s; pattern: %s; text: \"%s\"; table: %s", tag, this.getInformation(), text, list);
        Assert.fail(msg);
      }
    }
  }
}
