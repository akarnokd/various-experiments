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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import rx.Observable;

/**
 * 
 */
public class PTLQueueTest {

	@Test
	public void test() {
		PTLQueue<Integer> queue = new PTLQueue<>(32);
		queue.offer(1);
		
		assertEquals((Integer)1, queue.poll());
	}
	@Test
	public void testMany() {
		PTLQueue<Integer> queue = new PTLQueue<>(32);
		Observable.range(0, 32).forEach(e -> queue.offer(e));

		assertFalse(queue.offer(32));
		
		assertEquals((Integer)0, queue.poll());

		assertTrue(queue.offer(32));
	}

}
