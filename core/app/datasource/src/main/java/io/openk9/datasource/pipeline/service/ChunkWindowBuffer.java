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
import java.util.function.Function;

/**
 * Incremental "previous/next" chunk windowing with a fixed lookahead: the
 * streaming counterpart of the batch windows of
 * {@code EmbeddingService.mapToPayload}.
 *
 * <p>Chunks are offered one at a time, in stream order, and each is finalized
 * as soon as its full next-window is known: chunk {@code N} when chunk
 * {@code N + windowSize} arrives, or on {@link #flush()}. The windows are
 * identical to the batch ones for the same sequence, but memory stays
 * {@code O(windowSize)}: the buffer holds at most {@code 2 * windowSize + 1}
 * chunks. A {@code windowSize} of {@code 0} finalizes every chunk on arrival,
 * with empty windows.
 *
 * <p>Neighbours are counted on what has been received: the total number of
 * chunks is never assumed, being optional in the {@code EmbedContent} contract.
 * Window entries expose only {@code number} and {@code text}, derived from the
 * caller's chunk type through the mapper passed at construction.
 *
 * <p>Not thread-safe: a single stream must be driven from one thread at a time.
 *
 * @param <T> the caller's chunk type
 */
public final class ChunkWindowBuffer<T> {

	private final int windowSize;
	private final Function<? super T, WindowEntry> entryMapper;

	/**
	 * Sliding buffer of the chunks still needed to build windows. The element
	 * at list index {@code i} sits at absolute position {@code baseIndex + i}.
	 */
	private final List<T> window = new ArrayList<>();

	private int baseIndex = 0;
	private int received = 0;
	private int nextToFinalize = 0;

	private ChunkWindowBuffer(
		int windowSize, Function<? super T, WindowEntry> entryMapper) {

		if (windowSize < 0) {
			throw new IllegalArgumentException(
				"windowSize must be >= 0, was " + windowSize);
		}

		this.windowSize = windowSize;
		this.entryMapper = entryMapper;
	}

	/**
	 * Creates a buffer with the given lookahead and entry mapper.
	 *
	 * @param windowSize  number of neighbours on each side; {@code 0} means
	 *                    pure incremental emission with empty windows
	 * @param entryMapper extracts the window-relevant view ({@code number},
	 *                    {@code text}) from a chunk
	 * @param <T>         the caller's chunk type
	 * @return a new, empty buffer
	 * @throws IllegalArgumentException if {@code windowSize} is negative
	 */
	public static <T> ChunkWindowBuffer<T> of(
		int windowSize, Function<? super T, WindowEntry> entryMapper) {

		return new ChunkWindowBuffer<>(windowSize, entryMapper);
	}

	/**
	 * Offers the next chunk of the stream, in emission order.
	 *
	 * @param chunk the chunk that just arrived
	 * @return the chunks finalized by this arrival, in order; usually zero or
	 *         one, always empty until the lookahead has been reached
	 */
	public List<Windowed<T>> offer(T chunk) {

		window.add(chunk);
		int arrivedPos = received;
		received++;

		List<Windowed<T>> finalized = new ArrayList<>();

		// A chunk can be finalized once its whole next-window is available,
		// i.e. once a chunk windowSize positions ahead of it has arrived.
		while (nextToFinalize + windowSize <= arrivedPos) {
			finalized.add(finalizeAt(nextToFinalize, received - 1));
			nextToFinalize++;
			evict();
		}

		return finalized;
	}

	/**
	 * Signals end of stream and finalizes every remaining chunk, whose
	 * next-windows are now necessarily complete (fewer than {@code windowSize}
	 * neighbours at the tail).
	 *
	 * @return the remaining finalized chunks, in order
	 */
	public List<Windowed<T>> flush() {

		List<Windowed<T>> finalized = new ArrayList<>();

		while (nextToFinalize < received) {
			finalized.add(finalizeAt(nextToFinalize, received - 1));
			nextToFinalize++;
			evict();
		}

		return finalized;
	}

	/**
	 * Number of chunks currently buffered; bounded by {@code 2 * windowSize + 1}.
	 * Exposed for tests that assert the {@code O(windowSize)} memory property.
	 *
	 * @return the count of buffered chunks
	 */
	int bufferedCount() {
		return window.size();
	}

	private Windowed<T> finalizeAt(int position, int lastAvailablePosition) {

		var previous = collect(
			Math.max(position - windowSize, 0),
			position - 1
		);

		var next = collect(
			position + 1,
			Math.min(position + windowSize, lastAvailablePosition)
		);

		return new Windowed<>(at(position), previous, next);
	}

	private List<WindowEntry> collect(int fromInclusive, int toInclusive) {

		List<WindowEntry> entries = new ArrayList<>();

		for (int position = fromInclusive; position <= toInclusive; position++) {
			entries.add(entryMapper.apply(at(position)));
		}

		return entries;
	}

	private T at(int position) {
		return window.get(position - baseIndex);
	}

	private void evict() {

		// The oldest position still needed as a "previous" neighbour is
		// nextToFinalize - windowSize; drop anything below it.
		int keepFrom = nextToFinalize - windowSize;

		while (baseIndex < keepFrom && !window.isEmpty()) {
			window.remove(0);
			baseIndex++;
		}
	}

	/**
	 * Window-relevant view of a chunk: the same pair
	 * {@code EmbeddingService.mapToChunkWindowObject} writes.
	 *
	 * @param number the progressive chunk number (1..N over the stream)
	 * @param text   the chunk text
	 */
	public record WindowEntry(
		int number,
		String text
	) {}

	/**
	 * A finalized chunk with its previous and next neighbour windows.
	 *
	 * @param chunk    the finalized chunk itself (carrying vector, fileId, ...)
	 * @param previous up to {@code windowSize} preceding neighbours, in order
	 * @param next     up to {@code windowSize} following neighbours, in order
	 * @param <T>      the caller's chunk type
	 */
	public record Windowed<T>(
		T chunk,
		List<WindowEntry> previous,
		List<WindowEntry> next
	) {}

}
