package hu.akarnokd.experiments.collections;

import java.util.*;

import org.junit.*;


public class RingListTest {
    @Test
    public void testRemoveFirst() {
        RingList<Integer> list = new RingList<Integer>(4);
        list.add(1);
        list.add(2);
        
        list.remove(1);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(2), other);
    }
    @Test
    public void testRemoveLast() {
        RingList<Integer> list = new RingList<Integer>(4);
        list.add(1);
        list.add(2);
        
        list.remove(2);
        
        List<Integer> other = new ArrayList<Integer>(4);
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(1), other);
    }
    @Test
    public void testRemoveMiddle() {
        RingList<Integer> list = new RingList<Integer>(4);
        list.add(1);
        list.add(2);
        list.add(3);
        
        list.remove(2);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(1, 3), other);
    }
    @Test
    public void testRemoveSingle() {
        RingList<Integer> list = new RingList<Integer>(4);
        list.add(1);
        
        list.remove(1);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Collections.emptyList(), other);

        list.add(1);
        
        list.addTo(other);
        Assert.assertEquals(Arrays.asList(1), other);
    }
    
    @Test
    public void testWrap() {
        RingList<Integer> list = new RingList<Integer>(4);
        list.add(1);
        list.add(2);
        
        list.remove(1);
        list.remove(2);

        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(3, 4, 5, 6), other);
    }

}
