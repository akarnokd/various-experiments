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

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A bounded MPMC queue implementation based on 
 * https://blogs.oracle.com/dave/entry/ptlqueue_a_scalable_bounded_capacity 
 * <p>
 * Does not support null values.
 */
public final class PTLQueue<E> {
	private final int mask;
	private final int length;
	private final PaddedAtomicLong offerCursor;
	private final PaddedAtomicLong pollCursor;
	private final AtomicLongArray turns;
	private final AtomicReferenceArray<E> values;
	public PTLQueue(int capacity) {
		if ((capacity & (capacity - 1)) != 0) {
			capacity--;
			capacity |= capacity >> 1;
			capacity |= capacity >> 2;
			capacity |= capacity >> 4;
			capacity |= capacity >> 8;
			capacity |= capacity >> 16;
			capacity++;
		}
		mask = capacity - 1;
		length = capacity;
		offerCursor = new PaddedAtomicLong();
		pollCursor = new PaddedAtomicLong();
		turns = new AtomicLongArray(capacity);
		values = new AtomicReferenceArray<>(capacity);
		for  (int i = 0; i < length - 1; i++) {
			turns.lazySet(i, i);
		}
		turns.set(length - 1, length - 1);
	}
	public void put(E value, Runnable ifWait) {
		nullCheck(value);
		long ticket = offerCursor.getAndIncrement();
		int slot = (int)ticket & mask;
		while (turns.get(slot) != ticket) {
			ifWait.run();
		}
		values.set(slot, value);
	}
	public boolean offer(E value) {
		nullCheck(value);
		for (;;) {
			long ticket = offerCursor.get();
			int slot = (int)ticket & mask;
			if (turns.get(slot) != ticket) {
				return false;
			}
			if (offerCursor.compareAndSet(ticket, ticket + 1)) {
				values.set(slot, value);
				return true;
			}
		}
	}
	private void nullCheck(E value) {
		if (value == null) {
			throw new NullPointerException("Null values not allowed here!");
		}
	}
	public E take(Runnable ifWait) {
		long ticket = pollCursor.getAndIncrement();
		int slot = (int)ticket & mask;
		while (turns.get(slot) != ticket) {
			ifWait.run();
		}
		for (;;) {
			E v = values.get(slot);
			if (v != null) {
				values.lazySet(slot, null);
				turns.set(slot, ticket /* + mask + 1*/);
				return v;
			}
			ifWait.run();
		}
	}
	public E poll() {
		for (;;) {
			long ticket = pollCursor.get();
			int slot = (int)ticket & mask;
			if (turns.get(slot) != ticket) {
				return null;
			}
			E v = values.get(slot);
			if (v == null) {
				return null;
			}
			if (pollCursor.compareAndSet(ticket, ticket + 1)) {
				values.lazySet(slot, null);
				turns.set(slot, ticket + length);
				return v;
			}
		}
	}
	public E pollStrong() {
		for (;;) {
			long ticket = pollCursor.get();
			int slot = (int)ticket & mask;
			if (turns.get(slot) != ticket) {
				if (pollCursor.get() != ticket) {
					continue;
				}
				return null;
			}
			E v = values.get(slot);
			if (v == null) {
				if (((pollCursor.get() ^ ticket) | (turns.get(slot) ^ ticket)) != 0) {
					continue;
				}
				return null;
			}
			if (pollCursor.compareAndSet(ticket, ticket + 1)) {
				values.lazySet(slot, null);
				turns.set(slot, ticket + length);
				return v;
			}
		}
	}
}
