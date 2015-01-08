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

package hu.akarnokd.experiments.concurrent;

import java.util.concurrent.atomic.*;

/**
 * An ingress/egress ticketed queue.
 */
public class IETLQueue<E> {
    private final int mask;
    private final PaddedAtomicLong offerCursor;
    private final PaddedAtomicLong pollCursor;
    private final AtomicReferenceArray<E> values;
    private final AtomicLongArray ingress;
    private final AtomicLongArray egress;
    public IETLQueue(int capacity) {
        capacity = Pow2.pow2(capacity);
        this.mask = capacity - 1;
        this.offerCursor = new PaddedAtomicLong();
        this.pollCursor = new PaddedAtomicLong();
        this.values = new AtomicReferenceArray<>(capacity);
        this.ingress = new AtomicLongArray(capacity);
        this.egress = new AtomicLongArray(capacity);
        for  (int i = 0; i < capacity - 1; i++) {
            ingress.lazySet(i, i);
            egress.lazySet(i, i - capacity);
        }
        egress.lazySet(capacity - 1, -1);
        ingress.set(capacity - 1, capacity - 1);
    }

    public boolean offer(E value) {
        int m = mask;
        PaddedAtomicLong oc = offerCursor;
        AtomicLongArray in = ingress;
        AtomicLongArray eg = egress;
        AtomicReferenceArray<E> vs = values;
        for (;;) {
            long ticket = oc.get();
            int slot = (int)ticket & m;
            if (in.get(slot) != ticket) {
                if ((oc.get() ^ ticket) != 0) { // slot taken and consumed 
                    continue;
                }
                return false;
            }
            if (oc.compareAndSet(ticket, ticket + 1)) {
                vs.lazySet(slot, value);
                eg.set(slot, ticket);
                return true;
            }
        }
    }
    public E poll() {
        int m = mask;
        PaddedAtomicLong pc = pollCursor;
        AtomicLongArray in = ingress;
        AtomicLongArray eg = egress;
        AtomicReferenceArray<E> vs = values;
        for (;;) {
            long ticket = pc.get();
            int slot = (int)ticket & m;
            if (in.get(slot) != ticket) {
                if (pc.get() != ticket) { // somebody else took this slot
                    continue;
                }
                return null;
            }
            if (eg.get(slot) != ticket) { // offer is not finished yet
                continue;
            }
            E v = vs.get(slot);
            if (v == null) {
                if (((pc.get() ^ ticket) | (in.get(slot) ^ ticket)) != 0) { // somebody else took this slot
                    continue;
                }
                return null;
            }
            if (pc.compareAndSet(ticket, ticket + 1)) {
                vs.lazySet(slot, null);
                in.set(slot, ticket + m + 1);
                return v;
            }
        }
    }
}
