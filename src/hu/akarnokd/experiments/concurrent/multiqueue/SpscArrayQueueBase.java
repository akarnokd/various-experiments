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

import static hu.akarnokd.experiments.concurrent.UnsafeAccess.*;

/**
 * 
 */
public abstract class SpscArrayQueueBase extends ArrayQueueBase implements QueueBase {
    private long producerIndex;
    private static final long PRODUCER_INDEX = addressOf(SpscArrayQueue2.class, "producerIndex");
    private long consumerIndex;
    private static final long CONSUMER_INDEX = addressOf(SpscArrayQueue2.class, "consumerIndex");
    public SpscArrayQueueBase(int capacity) {
        super(capacity);
    }
    protected long lpProducerIndex() {
        return producerIndex;
    }
    protected long lvProducerIndex() {
        return UNSAFE.getLongVolatile(this, PRODUCER_INDEX);
    }
    protected void soProducerIndex(long value) {
        UNSAFE.putOrderedLong(this, PRODUCER_INDEX, value);
    }
    protected long lpConsumerIndex() {
        return consumerIndex;
    }
    protected long lvConsumerIndex() {
        return UNSAFE.getLongVolatile(this, CONSUMER_INDEX);
    }
    protected void soConsumerIndex(long value) {
        UNSAFE.putOrderedLong(this, CONSUMER_INDEX, value);
    }
    @Override
    public int size() {
        long after = lvConsumerIndex();
        while (true) {
            final long before = after;
            final long currentProducerIndex = lvProducerIndex();
            after = lvConsumerIndex();
            if (before == after) {
                return (int) (currentProducerIndex - after);
            }
        }
    }
    @Override
    public boolean isEmpty() {
        return lvProducerIndex() == lvConsumerIndex();
    }
}
