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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PeekingIterator<T> implements Iterator<T> {

  private Iterator<T> iter;
  private T peek;
  private boolean hasNext;

  public PeekingIterator(Iterator<T> iter) {
    this.iter = iter;
    this.hasNext = this.iter.hasNext();
    if (this.hasNext) {
      this.peek = this.iter.next();
    }
  }

  public T peek() {
    if (!hasNext) {
      throw new NoSuchElementException();
    }
    return peek;
  }

  @Override
  public boolean hasNext() {
    return this.hasNext;
  }

  @Override
  public T next() {
    if (!this.hasNext) {
      throw new NoSuchElementException();
    }
    var next = this.peek;
    this.hasNext = this.iter.hasNext();
    if (this.hasNext) {
      this.peek = this.iter.next();
    } else {
      this.peek = null;
    }
    return next;
  }
}
