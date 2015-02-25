/*
 * Copyright 2014 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package hu.akarnokd.experiments.collections;

import java.util.*;

public final class SLList<T> implements Iterable<T> {
        static final class N<T> {
            final T value;
            N<T> next;
            public N(T value) {
                this.value = value;
            }
        }
        N<T> head;
        N<T> tail;
        int size;
        public SLList() {
            
        }
        public SLList(Iterable<? extends T> other) {
            for (T t : other) {
                add(t);
            }
        }
        public void add(T value) {
            N<T> n = new N<T>(value);
            if (head == null) {
                head = n;
                tail = n;
            } else {
                tail.next = n;
                tail = n;
            }
            size++;
        }
        public boolean remove(T value) {
            N<T> prev = null;
            N<T> curr = head;
            while (curr != null) {
                if (value == curr.value || (value != null && value.equals(curr.value))) {
                    if (prev != null) {
                        // remove middle
                        prev.next = curr.next;
                    } else {
                        // remove first
                        head = curr.next;
                    }
                    if (tail == curr) {
                        // remove last
                        tail = prev;
                    }
                    size--;
                    return true;
                }
                prev = curr;
                curr = curr.next;
            }
            return false;
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public void addTo(Collection<? super T> other) {
            N<T> curr = head;
            while (curr != null) {
                other.add(curr.value);
                curr = curr.next;
            }
        }
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                N<T> curr = head;
                @Override
                public boolean hasNext() {
                    return curr != null;
                }
                @Override
                public T next() {
                    if (curr != null) {
                        T v = curr.value;
                        curr = curr.next;
                        return v;
                    }
                    throw new NoSuchElementException();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }