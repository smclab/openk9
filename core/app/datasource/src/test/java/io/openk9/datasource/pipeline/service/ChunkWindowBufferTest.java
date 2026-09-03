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

package io.openk9.datasource.pipeline.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import io.openk9.datasource.pipeline.service.ChunkWindowBuffer.WindowEntry;
import io.openk9.datasource.pipeline.service.ChunkWindowBuffer.Windowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChunkWindowBufferTest {

	private static final Function<WindowEntry, WindowEntry> IDENTITY = entry -> entry;

	@Test
	void should_match_v1_windows_across_sizes_and_lengths() {

		// exhaustively compare the streaming windows against the batch
		// EmbeddingService windows for many window sizes and stream lengths.
		int[] windowSizes = {0, 1, 2, 3, 5};
		int[] lengths = {0, 1, 2, 3, 4, 5, 8, 13, 20};

		for (int windowSize : windowSizes) {
			for (int length : lengths) {

				assertEquivalentToV1(windowSize, length);
			}
		}
	}

	@Test
	void should_match_v1_windows_on_random_sequences() {

		// property-style check with a fixed seed for reproducibility.
		var random = new Random(20268L);

		for (int iteration = 0; iteration < 500; iteration++) {

			int windowSize = random.nextInt(6);
			int length = random.nextInt(40);

			assertEquivalentToV1(windowSize, length);
		}
	}

	@Test
	void should_emit_incrementally_when_windowSize_is_zero() {

		// with windowSize 0 every chunk is finalized on arrival, no lookahead.
		var buffer = ChunkWindowBuffer.of(0, IDENTITY);
		var chunks = entries(3);

		var first = buffer.offer(chunks.get(0));
		var second = buffer.offer(chunks.get(1));
		var third = buffer.offer(chunks.get(2));

		// each offer finalizes exactly its own chunk with empty windows.
		Assertions.assertEquals(1, first.size());
		Assertions.assertEquals(chunks.get(0), first.get(0).chunk());
		Assertions.assertTrue(first.get(0).previous().isEmpty());
		Assertions.assertTrue(first.get(0).next().isEmpty());

		Assertions.assertEquals(1, second.size());
		Assertions.assertEquals(chunks.get(1), second.get(0).chunk());

		Assertions.assertEquals(1, third.size());
		Assertions.assertEquals(chunks.get(2), third.get(0).chunk());

		// nothing is left buffered at end of stream.
		Assertions.assertTrue(buffer.flush().isEmpty());
	}

	@Test
	void should_hold_back_until_lookahead_reached() {

		// with windowSize 2, no chunk can be finalized before the third arrival.
		var buffer = ChunkWindowBuffer.of(2, IDENTITY);
		var chunks = entries(5);

		Assertions.assertTrue(buffer.offer(chunks.get(0)).isEmpty());
		Assertions.assertTrue(buffer.offer(chunks.get(1)).isEmpty());

		// arrival of chunk at position 2 finalizes position 0.
		var finalizedByThird = buffer.offer(chunks.get(2));
		Assertions.assertEquals(1, finalizedByThird.size());
		Assertions.assertEquals(chunks.get(0), finalizedByThird.get(0).chunk());
		Assertions.assertEquals(
			List.of(chunks.get(1), chunks.get(2)),
			finalizedByThird.get(0).next());
	}

	@Test
	void should_finalize_short_stream_only_on_flush() {

		// fewer chunks than the window: nothing finalizes until the stream ends.
		var buffer = ChunkWindowBuffer.of(5, IDENTITY);
		var chunks = entries(3);

		for (WindowEntry chunk : chunks) {
			Assertions.assertTrue(buffer.offer(chunk).isEmpty());
		}

		// flush finalizes all three with truncated windows.
		var flushed = buffer.flush();
		Assertions.assertEquals(3, flushed.size());

		var middle = flushed.get(1);
		Assertions.assertEquals(chunks.get(1), middle.chunk());
		Assertions.assertEquals(List.of(chunks.get(0)), middle.previous());
		Assertions.assertEquals(List.of(chunks.get(2)), middle.next());
	}

	@Test
	void should_return_nothing_on_flush_of_empty_stream() {

		var buffer = ChunkWindowBuffer.of(3, IDENTITY);

		Assertions.assertTrue(buffer.flush().isEmpty());
	}

	@Test
	void should_keep_memory_bounded_by_window() {

		// the buffer must never hold more than 2 * windowSize + 1 chunks.
		int windowSize = 4;
		int length = 1000;
		var buffer = ChunkWindowBuffer.of(windowSize, IDENTITY);
		int maxBound = 2 * windowSize + 1;

		for (WindowEntry chunk : entries(length)) {
			buffer.offer(chunk);
			Assertions.assertTrue(
				buffer.bufferedCount() <= maxBound,
				"buffered " + buffer.bufferedCount() + " > " + maxBound);
		}

		buffer.flush();
	}

	/**
	 * Drives the streaming buffer with a synthetic stream of {@code length}
	 * chunks and asserts, for every chunk, that the previous/next windows match
	 * the batch windows computed by {@link EmbeddingService}.
	 */
	private void assertEquivalentToV1(int windowSize, int length) {

		// build the reference chunk sequence, numbered 1..length.
		var chunks = entries(length);

		// drive the streaming buffer: offer everything, then flush the tail.
		var buffer = ChunkWindowBuffer.of(windowSize, IDENTITY);
		List<Windowed<WindowEntry>> streamed = new ArrayList<>();

		for (WindowEntry chunk : chunks) {
			streamed.addAll(buffer.offer(chunk));
		}
		streamed.addAll(buffer.flush());

		// the stream must finalize exactly one document per chunk, in order.
		Assertions.assertEquals(
			length, streamed.size(),
			"windowSize=" + windowSize + " length=" + length);

		for (int index = 0; index < length; index++) {

			int number = index + 1;
			var windowed = streamed.get(index);

			// self chunk preserved and in order.
			Assertions.assertEquals(chunks.get(index), windowed.chunk());

			// windows identical to the getPreviousWindow / getNextWindow ones.
			List<WindowEntry> expectedPrevious =
				EmbeddingService.getPreviousWindow(windowSize, number, chunks);
			List<WindowEntry> expectedNext =
				EmbeddingService.getNextWindow(windowSize, number, length, chunks);

			Assertions.assertEquals(
				expectedPrevious, windowed.previous(),
				"previous mismatch windowSize=" + windowSize
				+ " length=" + length + " number=" + number);
			Assertions.assertEquals(
				expectedNext, windowed.next(),
				"next mismatch windowSize=" + windowSize
				+ " length=" + length + " number=" + number);
		}
	}

	private static List<WindowEntry> entries(int length) {

		List<WindowEntry> chunks = new ArrayList<>(length);

		for (int index = 0; index < length; index++) {
			chunks.add(new WindowEntry(index + 1, "chunk-" + (index + 1)));
		}

		return chunks;
	}

}
