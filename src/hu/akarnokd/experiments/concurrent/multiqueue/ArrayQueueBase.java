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

package hu.akarnokd.experiments.concurrent.multiqueue;

import static hu.akarnokd.experiments.concurrent.UnsafeAccess.UNSAFE;
import rx.internal.util.unsafe.Pow2;
/**
 * 
 */
public abstract class ArrayQueueBase {
    protected final Object[] array;
    protected final int mask;
    
    static final long ARRAY_BASE;
    static final long ARRAY_SHIFT;
    static {
        ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class);
        int s = UNSAFE.arrayIndexScale(Object[].class);
        int sh = 2;
        if (s == 8) {
            sh = 3;
        }
        ARRAY_SHIFT = sh;
    }
    public ArrayQueueBase(int capacity) {
        int c = Pow2.roundToPowerOfTwo(capacity);
        this.mask = c - 1;
        this.array = new Object[c];
    }
    
    protected static long calcOffset(int index) {
        return ARRAY_BASE + (((long)index) << ARRAY_SHIFT);
    }
    
    protected static long calcWrappedOffset(long index, int mask) {
        return ARRAY_BASE + ((index & mask) << ARRAY_SHIFT);
    }
    
    @SuppressWarnings("unchecked")
    protected <E> E lpElement(long offset) {
        return (E)UNSAFE.getObject(array, offset);
    }
    @SuppressWarnings("unchecked")
    protected <E> E lvElement(long offset) {
        return (E)UNSAFE.getObjectVolatile(array, offset);
    }
    protected <E> void spElement(long offset, E value) {
        UNSAFE.putObject(array, offset, value);
    }
    protected <E> void soElement(long offset, E value) {
        UNSAFE.putOrderedObject(array, offset, value);
    }
    protected <E> void svElement(long offset, E value) {
        UNSAFE.putObjectVolatile(array, offset, value);
    }
    protected <E> boolean casElement(long offset, E expected, E value) {
        return UNSAFE.compareAndSwapObject(array, offset, expected, value);
    }
}
