package hu.akarnokd.experiments.concurrent;

import java.util.function.Predicate;

import rx.functions.Action1;

/**
 * A lock-free append-only resizable array with optimized first bank of 16 elements.
 */
public final class AtomicResizableArrayUnsafe {
    final Object[][] arrays;
    static final long P_ARRAY_OFFSET;
    static final int P_ARRAY_SHIFT;
    static final int P_ARRAY_DIFF;
    static {
        P_ARRAY_OFFSET = UnsafeAccess.UNSAFE.arrayBaseOffset(Object[].class);
        P_ARRAY_DIFF = UnsafeAccess.UNSAFE.arrayIndexScale(Object[].class);
        int is = 2;
        if (P_ARRAY_DIFF > 8) {
            throw new IllegalStateException("Unsupported array scale: " + P_ARRAY_DIFF);
        } else
        if (P_ARRAY_DIFF == 8) {
            is = 3;
        }
        P_ARRAY_SHIFT = is;
        
    }
    public AtomicResizableArrayUnsafe() {
        arrays = new Object[27][];
    }
    public AtomicResizableArrayUnsafe(int initialCapacity) {
        this();
        // TODO
    }
    void soElement(Object array, long offset, Object o) {
        UnsafeAccess.UNSAFE.putOrderedObject(array, offset, o);
    }
    void spElement(Object array, long offset, Object o) {
        UnsafeAccess.UNSAFE.putObject(array, offset, o);
    }
    Object lvElement(Object array, long offset) {
        return UnsafeAccess.UNSAFE.getObjectVolatile(array, offset);
    }
    Object lpElement(Object array, long offset) {
        return UnsafeAccess.UNSAFE.getObject(array, offset);
    }
    boolean casElement(Object array, long offset, Object expected, Object value) {
        return UnsafeAccess.UNSAFE.compareAndSwapObject(array, offset, expected, value);
    }
    long calcOffset(int index) {
        return P_ARRAY_OFFSET + (((long)index) << P_ARRAY_SHIFT);
    }
    public void insert(int index, Object value) {
        if (value == null) {
            throw new NullPointerException("value != null required");
        }
        Object as = arrays;
        Object array;
        if (index < 16) {
            long offset = calcOffset(0);
            array = lvElement(as, offset);
            if (array == null) {
                array = new Object[16];
                if (!casElement(as, offset, null, array)) {
                    array = lvElement(as, offset);
                }
            }
            long offset2 = calcOffset(index);
            soElement(array, offset2, value);
        } else {
            int bankRaw = 32 - Integer.numberOfLeadingZeros(index);
            int bankCorr = bankRaw - 4;
            int offset = (index & ((1 << bankRaw) - 1) >> 1);
            
            long offs2 = calcOffset(bankCorr);
            array = lvElement(as, offs2);
            if (array == null) {
                int bankSize = 1 << (bankRaw - 1);
                try {
                    array = new Object[bankSize];
                    if (!casElement(as, offs2, null, array)) {
                        array = lvElement(as, offs2);
                    }
                } catch (OutOfMemoryError ex) {
                    int retries = 128;
                    while ((array = lvElement(as, offs2)) == null && retries-- > 0);
                    if (array == null) {
                        throw ex;
                    }
                }
            }            
            long offset2 = calcOffset(offset);
            soElement(array, offset2, value);
        }
    }
    /**
     * Returns the value at the specified index and spins
     * until the value becomes visible.
     * @param index
     * @return
     */
    public Object get(int index) {
        Object as = arrays;
        Object array;
        if (index < 16) {
            long offset = calcOffset(0);
            array = lvElement(as, offset);
            if (array != null) {
                long offset2 = calcOffset(index);
                return lvElement(array, offset2);
            }
        } else {
            int bankRaw = 32 - Integer.numberOfLeadingZeros(index);
            int bankCorr = bankRaw - 4;
            int offset = (index & ((1 << bankRaw) - 1) >> 1);
            
            long offs2 = calcOffset(bankCorr);
            array = lvElement(as, offs2);
            if (array != null) {
                long offset2 = calcOffset(offset);
                return lvElement(array, offset2);
            }
        }
        return null;
    }
    /**
     * Clears the counter to zero and the contents of the arrays to null.
     * Should not run concurrently with any add.
     */
    public void lazyClear() {
        Object[][] as = arrays;
        for (int i = 0; i < as.length; i++) {
            Object[] ara = as[i];
            if (ara != null) {
                int m = ara.length;
                if (m < 64) {
                    for (int j = 0; j < m; j++) {
                        ara[j] = null;
                    }
                } else {
                    for (int j = 0; j < m; j += 4) {
                        if (ara[j] == null) {
                            break;
                        }
                        ara[j] = null;
                        ara[j + 1] = null;
                        ara[j + 2] = null;
                        ara[j + 3] = null;
                    }
                }
            }
        }
    }
    /**
     * Deallocates all arrays and resets the counter to zero.
     * Should not run concurrently with any add.
     */
    public void lazyReset() {
        Object[] as = arrays;
        for (int i = 0; i < as.length; i++) {
            as[i] = null;
        }		
    }
    public void lazyForEach(Action1<Object> action) {
        Object[][] as = arrays;
        for (int i = 0; i < as.length; i++) {
            Object[] ara = as[i];
            if (ara == null) {
                return;
            }
            for (int j = 0; j < ara.length; j++) {
                Object o = ara[j];
                if (o == null) {
                    return;
                }
                action.call(action);
            }
        }
    }
    public boolean lazyConsumeWhile(Predicate<Object> action) {
        Object[][] as = arrays;
        long ao = calcOffset(0);
        int diff = P_ARRAY_DIFF;
        for (int i = 0; i < as.length; i++) {
            Object[] ara = as[i];
            if (ara == null) {
                return true;
            }
            int j = 0;
            long offset = ao;
            int len = ara.length;
            for (; j < len; j++, offset += diff) {
                Object o = lpElement(ara, offset);
                if (o == null) {
                    break;
                }
                if (!action.test(o)) {
                    return false;
                }
            }
            offset = ao;
            for (int k = 0; k < j; k++, offset += diff) {
                spElement(ara, offset, null);
            }
        }
        return true;
    }
    /**
     * @return counts the elements in the array in a linear fashion.
     */
    public int size() {
        int s = 0;
        for (Object[] o : arrays) {
            if (o != null) {
                for (Object a : o) {
                    if (a != null) {
                        s++;
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return s;
    }
}
