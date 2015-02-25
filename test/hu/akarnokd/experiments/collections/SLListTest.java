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
package hu.akarnokd.experiments.collections;

import java.util.*;

import org.junit.Assert;

import org.junit.Test;

public class SLListTest {
    @Test
    public void testRemoveFirst() {
        SLList<Integer> list = new SLList<Integer>();
        list.add(1);
        list.add(2);
        
        list.remove(1);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(2), other);
    }
    @Test
    public void testRemoveLast() {
        SLList<Integer> list = new SLList<Integer>();
        list.add(1);
        list.add(2);
        
        list.remove(2);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Arrays.asList(1), other);
    }
    @Test
    public void testRemoveMiddle() {
        SLList<Integer> list = new SLList<Integer>();
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
        SLList<Integer> list = new SLList<Integer>();
        list.add(1);
        
        list.remove(1);
        
        List<Integer> other = new ArrayList<Integer>();
        list.addTo(other);
        
        Assert.assertEquals(Collections.emptyList(), other);

        list.add(1);
        
        list.addTo(other);
        Assert.assertEquals(Arrays.asList(1), other);
    }
}
