/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.experimental.spring_apigw_sample.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.openk9.experimental.spring_apigw_sample.r2dbc.CompactSnowflakeIdGenerator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("performance-test")
public class CompactSnowflakeIdGeneratorPerformanceTest {

	/**
	 * Verify IDs are unique and ordered when generated from a single instance.
	 */
	@Test
	void testSequentialUniquenessAndOrder() {
		var generator = new CompactSnowflakeIdGenerator();

		long prev = -1;
		Set<Long> seen = new HashSet<>();

		for (int i = 0; i < 100_000; i++) {
			long id = generator.nextId();

			assertTrue(seen.add(id), "Duplicate ID detected");
			assertTrue(id > prev, "IDs are not strictly increasing");
			prev = id;
		}

	}

	/**
	 * Test multi-threaded generation from a single instance.
	 * Ensures internal synchronization avoids duplicates.
	 */
	@Test
	void testConcurrentUniquenessSingleInstance() throws InterruptedException {
		var generator = new CompactSnowflakeIdGenerator();
		var seen = ConcurrentHashMap.newKeySet();
		int threads = 8;
		int idsPerThread = 10_000;

		try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {

			List<Callable<Void>> tasks = new ArrayList<>();

			for (int t = 0; t < threads; t++) {
				tasks.add(() -> {
					for (int i = 0; i < idsPerThread; i++) {
						seen.add(generator.nextId());
					}
					return null;
				});
			}

			boolean allDone = true;
			for (Future<Void> future : pool.invokeAll(tasks)) {
				allDone &= future.isDone();
			}

			assertTrue(allDone, "uncompleted tasks");

			pool.shutdown();
		}

		assertEquals(threads * idsPerThread, seen.size(),
			"Duplicate IDs detected under concurrency");
	}

	/**
	 * Simulate multiple independent nodes (each with its own generator)
	 * to measure collision probability and throughput.
	 */
	@Test
	void testMultiNodeCollisionAndPerformance() throws InterruptedException {
		int nNodes = 50;
		int idsPerNode = 10_000;

		AtomicInteger collisions = new AtomicInteger();

		long start = System.nanoTime();

		try (ExecutorService pool = Executors.newFixedThreadPool(nNodes)) {

			List<Callable<Void>> tasks = new ArrayList<>();

			for (int i = 0; i < nNodes; i++) {
				tasks.add(() -> {
					var gen = new CompactSnowflakeIdGenerator();
					Set<Long> localIds = new HashSet<>();

					for (int j = 0; j < idsPerNode; j++) {
						long id = gen.nextId();

						// Local collision check
						if (!localIds.add(id)) {
							collisions.incrementAndGet();
						}
					}

					return null;
				});
			}

			boolean allDone = true;
			for (Future<Void> future : pool.invokeAll(tasks)) {
				allDone &= future.isDone();
			}

			assertTrue(allDone, "uncompleted tasks");
			pool.shutdown();
		}

		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		long total = nNodes * (long) idsPerNode;
		double throughput = total / (elapsedMs / 1000.0);

		System.out.printf("""
            === Snowflake Performance Report ===
            Nodes           : %d
            IDs per node    : %d
            Total IDs       : %,d
            Time elapsed    : %d ms
            Throughput      : %.2f IDs/sec
            Collisions      : %d (%.6f%%)
            ====================================
            """,
			nNodes, idsPerNode, total, elapsedMs,
			throughput, collisions.get(), collisions.get() * 100.0 / total);

		assertEquals(0, collisions.get(), "Collisions occurred across nodes");
	}

	/**
	 * Benchmark the raw generation rate of a single generator.
	 */
	@Test
	void testPerformanceSingleNode() {
		var gen = new CompactSnowflakeIdGenerator();
		int iterations = 100_000;

		long start = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			gen.nextId();
		}

		long elapsed = System.currentTimeMillis() - start;
		double rate = iterations / (elapsed / 1000.0d);

		assertTrue(rate > 10_000, "Throughput too low");
	}
}
