package hu.akarnokd.experiments.collections;

import java.util.*;

import rx.internal.util.unsafe.Pow2;

public final class RingList<T> implements Iterable<T> {
        long head;
        long tail;
        final T[] array;
        final int mask;
        final int capacity;
        public RingList(int capacity) {
            this.capacity = Pow2.roundToPowerOfTwo(capacity);
            this.mask = this.capacity - 1;
            array = (T[])new Object[this.capacity];
        }
        public RingList(Collection<? extends T> other) {
            int s = other.size();
            this.capacity = Pow2.roundToPowerOfTwo(s);
            this.mask = this.capacity - 1;
            array = (T[])new Object[this.capacity];
            Iterator<? extends T> it = other.iterator();
            for (int i = 0; i < s; i++) {
                array[i] = it.next();
            }
        }
        
        public int size() {
            return (int)(tail - head);
        }
        public boolean isEmpty() {
            return tail == head;
        }
        public boolean add(T value) {
            long t = tail;
            final T[] a = array;
            final int m = mask;
            final int ti = (int)t & m;
            
            if (a[ti] != null) {
                return false;
            }
            a[ti] = value;
            tail = t + 1;
            return true;
        }
        public boolean remove(T value) {
            final long h = head;
            final long t = tail;
            final T[] a = array;
            final int m = mask;
            
            for (long i = h; i != t; i++) {
                final int hi = (int)i & m;
                T v = a[hi];
                if (v == value || (v != null && v.equals(value))) {
                    int j = (int)h & m;
                    if (i != h) {
                        a[hi] = a[j];
                    }
                    a[j] = null;
                    head = h + 1;
                    return true;
                }
            }
            return false;
        }
        public void addTo(Collection<? super T> out) {
            final long h = head;
            final long t = tail;
            final T[] a = array;
            final int m = mask;
            
            for (long i = h; i != t; i++) {
                final int hi = (int)i & m;
                out.add(a[hi]);
            }
        }
        public void addAll(Iterable<? extends T> other) {
            for (T t : other) {
                add(t);
            }
        }
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                long h = head;
                final long t = tail;
                final T[] a = array;
                final int m = mask;
                @Override
                public boolean hasNext() {
                    return h != t;
                }
                @Override
                public T next() {
                    long h0 = h;
                    if (h0 != t) {
                        int hi = (int)h0 & m;
                        h = h0 + 1;
                        return a[hi];
                    }
                    throw new NoSuchElementException();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }