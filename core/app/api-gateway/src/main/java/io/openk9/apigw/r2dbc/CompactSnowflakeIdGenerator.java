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

package io.openk9.apigw.r2dbc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A compact, Snowflake-inspired 64-bit ID generator.
 * <p>
 * Layout:
 *   [0][41-bit timestamp][18-bit machineId][4-bit sequence]
 * <p>
 *  - 1 bit   : sign (always 0: positive long)
 *  - 41 bits : milliseconds since custom epoch (valid ~69 years)
 *  - 18 bits : machine identifier (random or user-provided)
 *  - 4 bits  : per-millisecond sequence counter (up to 16 IDs/ms/node)
 * <p>
 * Properties:
 *  - Time-ordered
 *  - Compact 64-bit long
 *  - Safe for small clusters (<= a few dozen nodes)
 *  - Less generation rate per-millisecond, good for slow rate inserts.
 *  - No central coordination required
 */
public class CompactSnowflakeIdGenerator {

	private static final long EPOCH = 1735689600000L; // 2025-01-01T00:00:00Z
	private static final int MACHINE_ID_BITS = 18;
	private static final int SEQUENCE_BITS = 4;

	private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
	private static final int MAX_SEQUENCE = ~(-1 << SEQUENCE_BITS);

	private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
	private static final long TIMESTAMP_SHIFT = MACHINE_ID_BITS + SEQUENCE_BITS;

	private final long machineId;
	private long lastTimestamp = -1L;
	private long sequence = 0L;

	/**
	 * Decode a Snowflake ID into its timestamp, machineId, and sequence components.
	 */
	public static DecodedId decode(long id) {
		long sequence = id & MAX_SEQUENCE;
		long machineId = (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;
		long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;

		return new DecodedId(timestamp, machineId, sequence);
	}

	/**
	 * Assembles a 64-bit Snowflake-like ID from its component parts.
	 */
	private static long composeSnowflakeId(long timestamp, long machineId, long sequence) {
		return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
			   | (machineId << MACHINE_ID_SHIFT)
			   | sequence;
	}

	/**
	 * Derive a random-like Machine ID based on hostname + random salt.
	 * This helps reduce collision probability on ephemeral nodes.
	 *
	 */
	private static long generateMachineId() {
		long hostHash = 0;

		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			hostHash = hostname.hashCode() & MAX_MACHINE_ID;
		}
		catch (UnknownHostException ignored) { /* ignored */ }

		long randomSalt = ThreadLocalRandom.current().nextLong(MAX_MACHINE_ID);
		return (hostHash ^ randomSalt) & MAX_MACHINE_ID;
	}

	/**
	 * Create a generator with an explicit machineId.
	 * @param machineId must be within [0, MAX_MACHINE_ID]
	 */
	public CompactSnowflakeIdGenerator(long machineId) {
		if (machineId < 0 || machineId > MAX_MACHINE_ID) {
			throw new IllegalArgumentException("machineId must be between 0 and " + MAX_MACHINE_ID);
		}
		this.machineId = machineId;
	}

	/**
	 * Create a generator with a random machineId (default).
	 */
	public CompactSnowflakeIdGenerator() {
		this(generateMachineId());
	}
	/**
	 * Generate the next unique, time-ordered ID.
	 */
	public synchronized long nextId() {
		long now = System.currentTimeMillis();

		// We can accept a clock sync error of 100ms at most
		if (lastTimestamp - now > 100) {
			throw new AssertionError("clock sync error is more than 100ms");
		}

		if (now > lastTimestamp) {
			sequence = 0;
		}
		else if (now == lastTimestamp) {
			sequence = (sequence + 1) & MAX_SEQUENCE;
			if (sequence == 0) {
				// Sequence overflow, wait for next millisecond
				while (now == lastTimestamp) {
					//Thread.onSpinWait();
					now = System.currentTimeMillis();
				}
			}
		}
		else {
			while (now <= lastTimestamp) {
				now = System.currentTimeMillis();
			}
		}

		lastTimestamp = now;

		return composeSnowflakeId(now, machineId, sequence);
	}

	public record DecodedId(long timestamp, long machineId, long sequence) {}

	public static void main(String[] args) {
		int iterations = 10000;
		int nGenerators = 50;
		int collisions = 0;

		// Each iteration empirically tests collisions within the ensemble of generators.
		for (int t = 0; t < iterations; t++) {
			Set<Long> seenNodes = new HashSet<>();

			for (int j = 0; j < nGenerators; j++) {

				long machineId = generateMachineId();

				if (!seenNodes.add(machineId)) {
					collisions++;
				}
			}
		}

		System.out.printf(
			"Out of %d trials, snowflakeId collisions occurred %d times (%.4f%%)%n",
			iterations, collisions, (collisions * 100.0 / iterations));

		CompactSnowflakeIdGenerator gen = new CompactSnowflakeIdGenerator();

		for (int i = 0; i < 10; i++) {
			long id = gen.nextId();
			System.out.printf("%d -> %016X -> %s%n", id, id, decode(id));
		}

	}
}