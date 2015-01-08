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

import static org.junit.Assert.*;

import java.util.concurrent.*;

import org.junit.Test;

import rx.Observable;

/**
 * 
 */
public class IETLQueueTest {

    @Test
    public void test() {
        IETLQueue<Integer> queue = new IETLQueue<>(32);
        queue.offer(1);

        assertEquals((Integer)1, queue.poll());
    }
    @Test
    public void testMany() {
        IETLQueue<Integer> queue = new IETLQueue<>(32);
        Observable.range(0, 32).forEach(e -> queue.offer(e));

        assertFalse(queue.offer(32));

        assertEquals((Integer)0, queue.poll());

        assertTrue(queue.offer(32));
    }
    private static void await(CyclicBarrier b) {
        try {
            b.await();
        } catch (InterruptedException | BrokenBarrierException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Test/*(timeout = 1000)*/
    public void testEmpty() {
        IETLQueue<Integer> queue = new IETLQueue<>(32);
        for (int i = 0; i < 1000; i++) {
            queue.offer(i);
            assertEquals((Integer)i, queue.poll());
            assertEquals(null, queue.poll());
        }
    }
    @Test
    public void test2Producer1BecomesConsumer() throws InterruptedException, ExecutionException {
        IETLQueue<Integer> queue = new IETLQueue<>(32);
        final CyclicBarrier b = new CyclicBarrier(2);
        ExecutorService exec = Executors.newFixedThreadPool(1);
        final Integer[] slots = new Integer[128];
        try {
            for (int i = 0; i < 1000000; i++) {
                slots[0] = null;
                slots[64] = null;
                final int j = i;
                Future<?> f = exec.submit(() -> {
                    await(b);
                    queue.offer(-j);
                    slots[64] = queue.poll();
                });
                await(b);
                queue.offer(j);
                slots[0] = queue.poll();
                f.get();
                assertTrue(slots[0] != null);
                assertTrue(slots[64] != null);
            }
        } finally {
            exec.shutdown();
        }
    }
}
