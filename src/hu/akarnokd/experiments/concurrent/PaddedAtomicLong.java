/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package hu.akarnokd.experiments.concurrent;

/**
 * A padded atomic reference to fill in 4 cache lines to avoid any false sharing or
 * adjacent prefetch.
 */
public final class PaddedAtomicLong extends PaddedAtomicLongBase {
    /** */
	private static final long serialVersionUID = 8781891581317286855L;
	/** Padding. */
	public transient long p16, p17, p18, p19, p20, p21, p22;      // 56 bytes (the remaining 8 is in the base)
	/** Padding. */
    public transient long p24, p25, p26, p27, p28, p29, p30; // 64 bytes
}