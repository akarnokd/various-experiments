package hu.akarnokd.experiments.concurrent;

import java.util.concurrent.atomic.*;

import rx.functions.*;

/**
 * A lock-free append-only resizable array with optimized first bank of 16 elements.
 */
public final class AtomicResizableArray {
    final AtomicReferenceArray<AtomicReferenceArray<Object>> arrays;
    final AtomicInteger count;
    public AtomicResizableArray() {
        arrays = new AtomicReferenceArray<>(27);
        count = new AtomicInteger();
    }
    public AtomicResizableArray(int initialCapacity) {
        this();
        // TODO
        count.set(0); // StoreLoad barrier
    }
    public void add(Object value) {
        if (value == null) {
            throw new NullPointerException("value != null required");
        }
        int index = count.getAndIncrement();

        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        AtomicReferenceArray<Object> array;
        if (index < 16) {
            array = as.get(0);
            if (array == null) {
                array = new AtomicReferenceArray<>(16);
                if (!as.compareAndSet(0, null, array)) {
                    array = as.get(0);
                }
            }
            array.lazySet(index, value);
        } else {
            int bankRaw = 32 - Integer.numberOfLeadingZeros(index);
            int bankCorr = bankRaw - 4;
            int offset = (index & ((1 << bankRaw) - 1) >> 1);
            array = as.get(bankCorr);
            if (array == null) {
                int bankSize = 1 << (bankRaw - 1);
                try {
                    array = new AtomicReferenceArray<>(bankSize);
                    if (!as.compareAndSet(bankCorr, null, array)) {
                        array = as.get(bankCorr);
                    }
                } catch (OutOfMemoryError ex) {
                    int retries = 128;
                    while ((array = as.get(bankCorr)) == null && retries-- > 0);
                    if (array == null) {
                        throw ex;
                    }
                }
            }            
            array.lazySet(offset, value);
        }
    }
    public int size() {
        return count.get();
    }
    public int getAndClear() {
        int c = count.get();
        count.lazySet(0);
        return c;
    }
    /**
     * Returns the value at the specified index and spins
     * until the value becomes visible.
     * @param index
     * @return
     */
    public Object get(int index) {
        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        AtomicReferenceArray<Object> array;
        if (index < 16) {
            array = as.get(0);
            if (array != null) {
                Object o = array.get(index);
                if (o != null) {
                    return o;
                }
            }
        } else {
            int bankRaw = 32 - Integer.numberOfLeadingZeros(index);
            int bankCorr = bankRaw - 4;
            int offset = (index & ((1 << bankRaw) - 1) >> 1);
            array = as.get(bankCorr);
            if (array != null) {
                Object o = array.get(offset);
                if (o != null) {
                    return o;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }
    /**
     * Clears the counter to zero and the contents of the arrays to null.
     * Should not run concurrently with any add.
     */
    public void lazyClear() {
        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        int n = as.length();
        for (int i = 0; i < n; i++) {
            AtomicReferenceArray<Object> ara = as.get(i);
            if (ara != null) {
                int m = ara.length();
                if (m < 128) {
                    for (int j = 0; j < m; j += 4) {
                        ara.lazySet(j, null);
                    }
                } else {
                    for (int j = 0; j < m; j += 4) {
                        if (ara.get(j) == null) {
                            break;
                        }
                        ara.lazySet(j, null);
                        ara.lazySet(j + 1, null);
                        ara.lazySet(j + 2, null);
                        ara.lazySet(j + 3, null);
                    }
                }
            }
        }
        count.lazySet(0);
    }
    /**
     * Deallocates all arrays and resets the counter to zero.
     * Should not run concurrently with any add.
     */
    public void lazyReset() {
        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        int n = as.length();
        for (int i = 0; i < n; i++) {
            as.lazySet(i, null);
        }		
        count.lazySet(0);
    }
    public void lazyForEach(Action1<Object> action) {
        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        int n = as.length();
        for (int i = 0; i < n; i++) {
            AtomicReferenceArray<Object> ara = as.get(i);
            if (ara != null) {
                int m = ara.length();
                for (int j = 0; j < m; j++) {
                    Object t1 = ara.get(j);
                    if (t1 == null) {
                        return;
                    }
                    action.call(t1);
                }
            }
        }
    }
    public interface Pred1<T> {
        boolean accept(T t);
    }
    public boolean lazyConsumeWhile(Pred1<Object> action) {
        AtomicReferenceArray<AtomicReferenceArray<Object>> as = arrays;
        int n = as.length();
        for (int i = 0; i < n; i++) {
            AtomicReferenceArray<Object> ara = as.get(i);
            if (ara != null) {
                int m = ara.length();
                for (int j = 0; j < m; j++) {
                    Object t1 = ara.get(j);
                    if (t1 == null) {
                        return true;
                    }
                    boolean a = action.accept(t1);
                    ara.lazySet(j, null);
                    if (!a) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
