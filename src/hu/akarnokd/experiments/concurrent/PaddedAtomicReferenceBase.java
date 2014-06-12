/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package hu.akarnokd.experiments.concurrent;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * The atomic reference base padded at the front.
 * @param <T> the value type
 */
abstract class PaddedAtomicReferenceBase<T> extends FrontPadding {

    private static final long serialVersionUID = 6513142711280243198L;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<PaddedAtomicReferenceBase, Object> updater;

    static {
        updater = AtomicReferenceFieldUpdater.newUpdater(PaddedAtomicReferenceBase.class, Object.class, "value");
    }

    private volatile T value; // 8-byte object field (or 4-byte + padding)

    public final T get() {
        return value;
    }

    public final void set(T newValue) {
        this.value = newValue;
    }

    public final void lazySet(T newValue) {
        updater.lazySet(this, newValue);
    }

    public final boolean compareAndSet(T expect, T update) {
        return updater.compareAndSet(this, expect, update);
    }

    public final boolean weakCompareAndSet(T expect, T update) {
        return updater.weakCompareAndSet(this, expect, update);
    }

    @SuppressWarnings("unchecked")
    public final T getAndSet(T newValue) {
        return (T) updater.getAndSet(this, newValue);
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }
}