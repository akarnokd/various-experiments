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

package hu.akarnokd.experiments;

import rx.Observable;
import gnu.trove.list.array.TIntArrayList;

import com.google.common.collect.Sets;

/**
 * Just verify if all dependency libraries are accessible.
 */
public class DependencyVerify {
	public static void main(String[] args) {
		// Java 8
		Runnable r = () -> System.out.println("Hello world!");
		r.run();
	
		// Guava
		System.out.println(Sets.newHashSet());

		// Trove
		System.out.println(new TIntArrayList());
		
		// RxJava
		Observable.just(1).subscribe(System.out::println, Throwable::printStackTrace, r::run);
	}
}
