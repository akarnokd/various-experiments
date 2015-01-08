package hu.akarnokd.experiments.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.functions.Action1;

public class AtomicResizableArrayUnsafeTest {

	@Test(timeout = 10000)
	public void testSimpleAdd() {
		Random rnd = new Random();
		
		for (int k = 0; k < 1000; k++) {
			AtomicResizableArrayUnsafe ara = new AtomicResizableArrayUnsafe();
			
			int n = rnd.nextInt(100000);
			
			for (int i = 0; i < n; i++) {
				ara.insert(i, i);
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
