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

import rx.functions.*;

/**
 * 
 */
public final class SpscArrayQueue2<T1, T2> extends SpscArrayQueueBase implements Queue2<T1, T2> {
    public SpscArrayQueue2(int capacity) {
        super(capacity * 2);
    }
    @Override
    public boolean offer(T1 t1, T2 t2) {
        long index = lpProducerIndex();
        int m = mask;
        long offset = calcWrappedOffset(index, m);
        if (lvElement(offset) != null) {
            return false;
        }
        long offset2 = calcWrappedOffset(index + 1, m);
        spElement(offset2, t2);
        soElement(offset, t1);
        soProducerIndex(index + 2);
        return true;
    }
    @Override
    public boolean peek(Action2<? super T1, ? super T2> out) {
        long index = lpConsumerIndex();
        int m = mask;
        long offset = calcWrappedOffset(index, m);
        T1 t1 = lvElement(offset);
        if (t1 == null) {
            return false;
        }
        long offset2 = calcWrappedOffset(index + 1, m);
        T2 t2 = lpElement(offset2);
        out.call(t1, t2);
        return true;
    }
    @Override
    public boolean poll(Action2<? super T1, ? super T2> out) {
        long index = lpConsumerIndex();
        int m = mask;
        long offset = calcWrappedOffset(index, m);
        T1 t1 = lvElement(offset);
        if (t1 == null) {
            return false;
        }
        long offset2 = calcWrappedOffset(index + 1, m);
        T2 t2 = lpElement(offset2);
        spElement(offset2, null);
        soElement(offset, null);
        soConsumerIndex(index + 2);
        out.call(t1, t2);
        return true;
    }
    @Override
    public void clear() {
        Action2<Object, Object> emptyAction = Actions.empty();
        while (poll(emptyAction) || !isEmpty())
            ;
    }
}
