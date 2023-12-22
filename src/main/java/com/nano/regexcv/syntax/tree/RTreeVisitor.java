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

public interface RTreeVisitor<Out> {

  public Out visit(RSingleCharacter node);

  public Out visit(RCharList node);

  public Out visit(RCharRange node);

  public Out visit(RCharRangeList node);

  public Out visit(RChoice node);

  public Out visit(RContatenation node);

  public Out visit(ROneOrMore node);

  public Out visit(ROptional node);

  public Out visit(RZeroOrMore node);
}
