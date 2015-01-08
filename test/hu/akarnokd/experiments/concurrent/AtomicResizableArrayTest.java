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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.functions.Action1;

public class AtomicResizableArrayTest {

	@Test(timeout = 10000)
	public void testSimpleAdd() {
		Random rnd = new Random();
		
		for (int k = 0; k < 1000; k++) {
			AtomicResizableArray ara = new AtomicResizableArray();
			
			int n = rnd.nextInt(100000);
			
			for (int i = 0; i < n; i++) {
				ara.add(i);
			}
			
			for (int i = 0; i < n; i++) {
				assertEquals(i, ara.get(i));
			}
			
			assertEquals(n, ara.size());
			
			final AtomicInteger cnt = new AtomicInteger();
			
			ara.lazyForEach(new Action1<Object>() {
			    @Override
			    public void call(Object t1) {
			        cnt.incrementAndGet();
			    }
			});
	
			assertEquals(n, cnt.get());
		}
	}

}
