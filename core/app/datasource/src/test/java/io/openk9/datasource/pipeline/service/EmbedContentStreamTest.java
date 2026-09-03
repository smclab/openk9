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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.openk9.ml.grpc.EmbeddingOuterClass.EmbeddedChunk;
import io.openk9.ml.grpc.EmbeddingOuterClass.FloatVector;
import io.openk9.ml.grpc.EmbeddingOuterClass.VectorDataType;

import com.google.protobuf.ByteString;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the windowing and mapping of {@code embedContentStream},
 * driven with synthetic {@code EmbeddedChunk}s (no gRPC, OpenSearch or CDI).
 * The windows are asserted equal to the batch
 * {@link EmbeddingService#getPreviousWindow} /
 * {@link EmbeddingService#getNextWindow}, closing the loop with the
 * {@link ChunkWindowBuffer} equivalence its own test already checks.
 */
class EmbedContentStreamTest {

	@Test
	void should_map_n_chunks_to_n_docs_with_v1_windows() {

		int windowSize = 2;
		var chunks = textChunks(6);

		var docs = runToDocs(chunks, windowSize);

		// one doc per chunk, in order.
		Assertions.assertEquals(chunks.size(), docs.size());

		for (int index = 0; index < chunks.size(); index++) {

			var chunk = chunks.get(index);
			var doc = docs.get(index);
			int number = index + 1;

			Assertions.assertEquals(number, doc.getInteger("number"));
			Assertions.assertEquals(chunk.getText(), doc.getString("chunkText"));

			// windows equal to the getPreviousWindow / getNextWindow ones.
			Assertions.assertEquals(
				windowEntries(EmbeddingService.getPreviousWindow(
					windowSize, number, chunks)),
				doc.getJsonArray("previous"));

			Assertions.assertEquals(
				windowEntries(EmbeddingService.getNextWindow(
					windowSize, number, chunks.size(), chunks)),
				doc.getJsonArray("next"));
		}
	}

	@Test
	void should_emit_incrementally_when_windowSize_is_zero() {

		// windowSize 0: every chunk finalized on arrival, one emitted doc each,
		// with empty windows (pure incremental).
		var chunks = textChunks(4);

		var docs = runToDocs(chunks, 0);

		Assertions.assertEquals(chunks.size(), docs.size());

		for (int index = 0; index < chunks.size(); index++) {
			var doc = docs.get(index);
			Assertions.assertEquals(index + 1, doc.getInteger("number"));
			Assertions.assertTrue(doc.getJsonArray("previous").isEmpty());
			Assertions.assertTrue(doc.getJsonArray("next").isEmpty());
		}
	}

	@Test
	void should_be_equivalent_to_v1_for_text_only() {

		int windowSize = 1;
		var chunks = textChunks(3);
		var root = Map.<String, Object>of("title", "a title", "acl",
			Map.of("public", true));

		var docs = runToDocs(chunks, windowSize, root);

		Assertions.assertEquals(chunks.size(), docs.size());

		for (int index = 0; index < chunks.size(); index++) {

			var chunk = chunks.get(index);
			var doc = docs.get(index);

			// same fields mapToPayload writes.
			Assertions.assertEquals(chunk.getNumber(), doc.getInteger("number"));
			Assertions.assertEquals(chunk.getTotal(), doc.getInteger("total"));
			Assertions.assertEquals(chunk.getText(), doc.getString("chunkText"));

			var vector = doc.getJsonArray("vector");
			Assertions.assertEquals(chunk.getF32().getValuesCount(), vector.size());
			for (int j = 0; j < vector.size(); j++) {
				Assertions.assertEquals(
					chunk.getF32().getValues(j), vector.getDouble(j), 1e-6);
			}

			// the source document is merged.
			Assertions.assertEquals("a title", doc.getString("title"));

			// EmbedContent additions: text modality, no binary discriminator.
			Assertions.assertEquals("text", doc.getString("media_type"));
			Assertions.assertFalse(doc.containsKey("fileId"));
		}
	}

	@Test
	void should_emit_matured_docs_before_an_interrupted_stream_fails() {

		// windowSize 0: every offered chunk matures immediately, so all the
		// docs of the chunks received before the error are emitted (and thus
		// handed to the writer), then the failure propagates. Fail-fast.
		var received = textChunks(3);

		Multi<EmbeddedChunk> interrupted = Multi.createBy().concatenating()
			.streams(
				Multi.createFrom().iterable(received),
				Multi.createFrom().failure(new RuntimeException("module error")));

		var subscriber = EmbeddingService.windowAndEncode(
				interrupted, Map.of(), Map.of(), 0)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		Assertions.assertEquals(received.size(), subscriber.getItems().size());
	}

	@Test
	void should_emit_no_document_before_an_immediate_error() {

		// a stream that fails before any chunk: nothing is emitted, so the
		// writer's first-batch delete is never issued and the prior indexed
		// version is preserved.
		Multi<EmbeddedChunk> failing =
			Multi.createFrom().failure(new RuntimeException("precondition failed"));

		var subscriber = EmbeddingService.windowAndEncode(
				failing, Map.of(), Map.of(), 2)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		Assertions.assertTrue(subscriber.getItems().isEmpty());
	}

	@Test
	void should_carry_fileId_and_media_type_for_binary_chunks() {

		var text = textChunk(1, 2, "some text");
		var binary = binaryChunk(2, "file-1", 3.0f, 4.0f);

		var docs = runToDocs(
			List.of(text, binary), 0, Map.of(), Map.of("file-1", "image/png"));

		var textDoc = docs.get(0);
		Assertions.assertEquals("text", textDoc.getString("media_type"));
		Assertions.assertFalse(textDoc.containsKey("fileId"));

		var binaryDoc = docs.get(1);
		Assertions.assertEquals("file-1", binaryDoc.getString("fileId"));
		Assertions.assertEquals("image/png", binaryDoc.getString("media_type"));
	}

	@Test
	void should_default_media_type_when_content_type_is_unknown() {

		// a binary ref whose contentType was not provided (not in the map):
		// media_type falls back to the generic octet-stream, never null.
		var binary = binaryChunk(1, "file-x", 1.0f);

		var docs = runToDocs(List.of(binary), 0, Map.of(), Map.of());

		var doc = docs.get(0);
		Assertions.assertEquals("file-x", doc.getString("fileId"));
		Assertions.assertEquals(
			"application/octet-stream", doc.getString("media_type"));
	}

	@Test
	void should_keep_outstanding_bounded_when_the_writer_is_slow() {

		// The pipeline claims O(windowSize) memory because it is demand-driven:
		// with a fast source and a consumer that pulls one doc at a time (as
		// EmbeddingProcessor does via transformToUniAndConcatenate), the chunks
		// pulled-from-source-but-not-yet-written must stay bounded and must NOT
		// grow with the stream length.
		int windowSize = 2;

		// the lookahead plus the doc in flight; small and independent of N.
		int bound = 2 * windowSize + 2;

		// measure the peak outstanding at two very different stream lengths.
		int peakShort = maxOutstanding(1_000, windowSize);
		int peakLong = maxOutstanding(4_000, windowSize);

		// bounded by a small constant, far below the 1_000 / 4_000 chunks streamed.
		Assertions.assertTrue(
			peakShort <= bound,
			"peak outstanding " + peakShort + " exceeded bound " + bound);

		// and it does not grow with the stream length: an unbounded buffer in the
		// pipeline (e.g. a collect().asList()) would drive the peak up to N.
		Assertions.assertEquals(
			peakShort, peakLong,
			"peak outstanding grew with stream length: "
			+ peakShort + " -> " + peakLong);
	}

	@Test
	void should_survive_a_consumer_that_writes_slower_than_the_stream() {

		// A consumer that holds no demand while it writes must never break the
		// stream. It used to: the docs were grouped here by
		// group().intoLists().of(size, delay), and a time-based grouping has to
		// emit when its delay expires even if nobody is asking — which Reactive
		// Streams forbids, so it cancelled the source and raised
		// BackPressureFailure("Cannot emit item due to lack of requests").
		//
		// This exact shape — 5 chunks 300 ms apart, 400 ms per write — died at
		// ~1500 ms. Batching now lives in ChunkStreamWriter, where a timer is
		// just a message and answers to no demand contract.
		var chunks = textChunks(5);

		Multi<EmbeddedChunk> trickle = Multi.createFrom().iterable(chunks)
			.onItem().transformToUniAndConcatenate(chunk ->
				Uni.createFrom().item(chunk)
					.onItem().delayIt().by(Duration.ofMillis(300)));

		var written = EmbeddingService.windowAndEncode(
				trickle, Map.of(), Map.of(), 0)
			.onItem().transformToUniAndConcatenate(document ->
				Uni.createFrom().item(document)
					.onItem().delayIt().by(Duration.ofMillis(400)))
			.collect().asList()
			.await().atMost(Duration.ofSeconds(30));

		Assertions.assertEquals(chunks.size(), written.size());
	}

	@Test
	void should_map_vector_by_case() {

		var f32 = binaryChunk(1, "f", 1.5f, -2.5f);
		Assertions.assertEquals(
			List.of(1.5f, -2.5f), EmbeddingService.mapVector(f32));

		var i8 = EmbeddedChunk.newBuilder()
			.setNumber(1)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_BYTE)
			.setI8(ByteString.copyFrom(new byte[] {-128, 0, 127}))
			.build();
		Assertions.assertEquals(
			List.of(-128, 0, 127), EmbeddingService.mapVector(i8));

		// Packed bits keep the sign, like i8: a binary knn_vector is read by
		// OpenSearch as signed bytes, and any value above 127 is rejected with
		// "KNN vector values are not within in the byte range [-128, 127]".
		// 0xFF is therefore -1, not 255.
		var bits = EmbeddedChunk.newBuilder()
			.setNumber(1)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_BINARY)
			.setBits(ByteString.copyFrom(new byte[] {(byte) 0xFF, 0x00, 0x01}))
			.build();
		Assertions.assertEquals(
			List.of(-1, 0, 1), EmbeddingService.mapVector(bits));

		var allBitsSet = EmbeddedChunk.newBuilder()
			.setNumber(1)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_BINARY)
			.setBits(ByteString.copyFrom(
				new byte[] {(byte) 0x80, (byte) 0xC0, 0x7F}))
			.build();
		Assertions.assertTrue(
			((List<?>) EmbeddingService.mapVector(allBitsSet)).stream()
				.map(Integer.class::cast)
				.allMatch(value -> value >= -128 && value <= 127),
			"packed bits must stay inside the OpenSearch byte range");
	}

	@Test
	void should_report_refs_that_produced_no_chunk() {

		var missing = EmbeddingService.missingRefs(
			Set.of("a", "b", "c"), Set.of("a", "c"));

		Assertions.assertEquals(List.of("b"), missing);
	}

	@Test
	void should_report_the_missing_refs_when_the_stream_completes() {

		var reported = new ArrayList<List<String>>();

		var subscriber = EmbeddingService.reportMissingRefs(
				Multi.createFrom().items("batch"),
				Set.of("a", "b"), Set.of("a"),
				reported::add)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		Assertions.assertEquals(List.of(List.of("b")), reported);
	}

	@Test
	void should_report_the_missing_refs_when_the_stream_fails() {

		var reported = new ArrayList<List<String>>();

		// an interrupted stream is the case where knowing which binaries never
		// came back is worth the most, and the one a completion hook misses.
		var subscriber = EmbeddingService.reportMissingRefs(
				Multi.createFrom().<String>failure(
					new IllegalStateException("stream broke")),
				Set.of("a", "b"), Set.of("a"),
				reported::add)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		Assertions.assertEquals(List.of(List.of("b")), reported);
	}

	@Test
	void should_not_report_when_every_ref_produced_a_chunk() {

		var reported = new ArrayList<List<String>>();

		var subscriber = EmbeddingService.reportMissingRefs(
				Multi.createFrom().items("batch"),
				Set.of("a"), Set.of("a"),
				reported::add)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		Assertions.assertTrue(reported.isEmpty());
	}

	@Test
	void should_not_report_when_the_stream_is_cancelled() {

		var reported = new ArrayList<List<String>>();

		// a cancelled stream was dropped from the outside: the refs still
		// pending are not a symptom of anything and must not be logged.
		var subscriber = EmbeddingService.reportMissingRefs(
				Multi.createFrom().<String>nothing(),
				Set.of("a", "b"), Set.of(),
				reported::add)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.cancel();

		Assertions.assertTrue(reported.isEmpty());
	}

	// ---- helpers ---------------------------------------------------------

	/**
	 * Streams {@code n} chunks through {@link EmbeddingService#windowAndEncode}
	 * with a fast source and a consumer that pulls exactly one doc at a time,
	 * writing it before pulling the next. Returns the peak number of chunks
	 * emitted by the source but not yet written by the consumer.
	 */
	private static int maxOutstanding(int n, int windowSize) {

		var emitted = new AtomicInteger();
		var written = new AtomicInteger();
		var peak = new AtomicInteger();

		// a source that emits eagerly, recording the running gap between what it
		// has emitted and what the consumer has written so far.
		var source = Multi.createFrom().iterable(textChunks(n))
			.onItem().invoke(() -> peak.accumulateAndGet(
				emitted.incrementAndGet() - written.get(), Math::max));

		// a slow consumer with zero initial demand.
		var subscriber = EmbeddingService.windowAndEncode(
				source, Map.of(), Map.of(), windowSize)
			.subscribe().withSubscriber(AssertSubscriber.create());

		// pull one doc, "write" it, then pull the next, until the stream ends.
		// the request count is guarded so a broken pipeline that never completes
		// cannot hang the suite.
		int requests = 0;
		while (!subscriber.hasCompleted() && requests++ <= n) {
			subscriber.request(1);
			written.set(subscriber.getItems().size());
		}

		// the bounded-demand consumer must have drained the whole stream.
		Assertions.assertTrue(
			subscriber.hasCompleted(),
			"stream did not complete after " + requests + " requests");
		Assertions.assertEquals(n, emitted.get());

		return peak.get();
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize) {

		return runToDocs(chunks, windowSize, Map.of(), Map.of());
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize, Map<String, Object> root) {

		return runToDocs(chunks, windowSize, root, Map.of());
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize,
		Map<String, Object> root, Map<String, String> contentTypeByFileId) {

		var subscriber = EmbeddingService.windowAndEncode(
				Multi.createFrom().iterable(chunks),
				root, contentTypeByFileId, windowSize)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		return decode(subscriber.getItems());
	}

	/** One emitted item is one chunk-doc. */
	private static List<JsonObject> decode(List<byte[]> items) {

		List<JsonObject> docs = new ArrayList<>();
		for (byte[] item : items) {
			docs.add(new JsonObject(Buffer.buffer(item)));
		}

		return docs;
	}

	private static JsonArray windowEntries(List<EmbeddedChunk> window) {

		var array = new JsonArray();
		for (EmbeddedChunk chunk : window) {
			array.add(new JsonObject()
				.put("number", chunk.getNumber())
				.put("chunkText", chunk.getText()));
		}

		return array;
	}

	private static List<EmbeddedChunk> textChunks(int count) {

		List<EmbeddedChunk> chunks = new ArrayList<>(count);
		for (int number = 1; number <= count; number++) {
			chunks.add(textChunk(number, count, "chunk-" + number, number));
		}

		return chunks;
	}

	private static EmbeddedChunk textChunk(
		int number, int total, String text, float... vector) {

		var floatVector = FloatVector.newBuilder();
		for (float value : vector) {
			floatVector.addValues(value);
		}

		return EmbeddedChunk.newBuilder()
			.setNumber(number)
			.setTotal(total)
			.setText(text)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_FLOAT32)
			.setDimension(vector.length)
			.setF32(floatVector.build())
			.build();
	}

	private static EmbeddedChunk binaryChunk(
		int number, String fileId, float... vector) {

		var floatVector = FloatVector.newBuilder();
		for (float value : vector) {
			floatVector.addValues(value);
		}

		return EmbeddedChunk.newBuilder()
			.setNumber(number)
			.setText("binary-chunk-" + number)
			.setFileId(fileId)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_FLOAT32)
			.setDimension(vector.length)
			.setF32(floatVector.build())
			.build();
	}

}
