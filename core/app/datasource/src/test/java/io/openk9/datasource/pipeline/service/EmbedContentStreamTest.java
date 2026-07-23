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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.openk9.ml.grpc.EmbeddingOuterClass.EmbeddedChunk;
import io.openk9.ml.grpc.EmbeddingOuterClass.FloatVector;
import io.openk9.ml.grpc.EmbeddingOuterClass.VectorDataType;

import com.google.protobuf.ByteString;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the v2 streaming windowing / mapping / batching, driven with
 * synthetic {@code EmbeddedChunk}s (no gRPC / OpenSearch / CDI). The windows are
 * asserted equal to the batch v1 {@link EmbeddingService#getPreviousWindow} /
 * {@link EmbeddingService#getNextWindow}, closing the loop with the pure
 * {@link ChunkWindowBuffer} equivalence already checked by its own test.
 */
class EmbedContentStreamTest {

	private static final int BIG_BATCH = 10_000;

	@Test
	void should_map_n_chunks_to_n_docs_with_v1_windows() {

		int windowSize = 2;
		var chunks = textChunks(6);

		var docs = runToDocs(chunks, windowSize, BIG_BATCH);

		// one doc per chunk, in order.
		Assertions.assertEquals(chunks.size(), docs.size());

		for (int index = 0; index < chunks.size(); index++) {

			var chunk = chunks.get(index);
			var doc = docs.get(index);
			int number = index + 1;

			Assertions.assertEquals(number, doc.getInteger("number"));
			Assertions.assertEquals(chunk.getText(), doc.getString("chunkText"));

			// windows equal to the v1 EmbeddingService computation.
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

		// windowSize 0 + batchSize 1: every chunk finalized on arrival, one
		// batch each, with empty windows (pure incremental).
		var chunks = textChunks(4);

		var subscriber = EmbeddingService.windowAndBatch(
				Multi.createFrom().iterable(chunks),
				Map.of(), Map.of(), 0, 1)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		var batches = subscriber.getItems();
		Assertions.assertEquals(chunks.size(), batches.size());

		for (int index = 0; index < chunks.size(); index++) {
			var batch = new JsonArray(Buffer.buffer(batches.get(index)));

			Assertions.assertEquals(1, batch.size());

			var doc = batch.getJsonObject(0);
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

		var docs = runToDocs(chunks, windowSize, BIG_BATCH, root);

		Assertions.assertEquals(chunks.size(), docs.size());

		for (int index = 0; index < chunks.size(); index++) {

			var chunk = chunks.get(index);
			var doc = docs.get(index);

			// same fields v1 writes.
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

			// v2 additions: text modality, no binary discriminator.
			Assertions.assertEquals("text", doc.getString("media_type"));
			Assertions.assertFalse(doc.containsKey("fileId"));
		}
	}

	@Test
	void should_leave_matured_batches_on_interrupted_stream() {

		// windowSize 0: every offered chunk matures immediately. At batchSize 1
		// each matured doc is its own batch, so all the batches of the chunks
		// received before the error are emitted, then the failure propagates
		// (fail-fast, no partial tail to flush at this batch size).
		var received = textChunks(3);

		Multi<EmbeddedChunk> interrupted = Multi.createBy().concatenating()
			.streams(
				Multi.createFrom().iterable(received),
				Multi.createFrom().failure(new RuntimeException("module error")));

		var subscriber = EmbeddingService.windowAndBatch(
				interrupted, Map.of(), Map.of(), 0, 1)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		// the matured batches were emitted before the failure.
		Assertions.assertEquals(received.size(), subscriber.getItems().size());
	}

	@Test
	void should_flush_matured_docs_on_interrupt_at_real_batch_size() {

		// an interrupted stream at the production batch size: 3 chunks mature
		// (windowSize 0) then the stream errors before the batch of 32 fills.
		// The grouping drops its partial batch on failure, but the matured docs
		// must still be written, so they are flushed as a last batch before the
		// failure propagates.
		var received = textChunks(3);

		Multi<EmbeddedChunk> interrupted = Multi.createBy().concatenating()
			.streams(
				Multi.createFrom().iterable(received),
				Multi.createFrom().failure(new RuntimeException("module error")));

		var subscriber = EmbeddingService.windowAndBatch(
				interrupted, Map.of(), Map.of(), 0, 32)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		// all 3 matured docs were flushed (in a partial batch) before failing.
		int docs = 0;
		for (byte[] batch : subscriber.getItems()) {
			docs += new JsonArray(Buffer.buffer(batch)).size();
		}
		Assertions.assertEquals(received.size(), docs);
	}

	@Test
	void should_emit_no_batch_before_an_immediate_error() {

		// a stream that fails before any chunk: no batch is emitted, so the
		// writer's first-batch delete is never issued and the prior indexed
		// version is preserved.
		Multi<EmbeddedChunk> failing =
			Multi.createFrom().failure(new RuntimeException("precondition failed"));

		var subscriber = EmbeddingService.windowAndBatch(
				failing, Map.of(), Map.of(), 2, BIG_BATCH)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitFailure();

		Assertions.assertTrue(subscriber.getItems().isEmpty());
	}

	@Test
	void should_carry_fileId_and_media_type_for_binary_chunks() {

		var text = textChunk(1, 2, "some text");
		var binary = binaryChunk(2, "file-1", 3.0f, 4.0f);

		var docs = runToDocs(
			List.of(text, binary), 0, BIG_BATCH,
			Map.of(), Map.of("file-1", "image/png"));

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

		var docs = runToDocs(
			List.of(binary), 0, BIG_BATCH, Map.of(), Map.of());

		var doc = docs.get(0);
		Assertions.assertEquals("file-x", doc.getString("fileId"));
		Assertions.assertEquals(
			"application/octet-stream", doc.getString("media_type"));
	}

	@Test
	void should_split_documents_into_batches_of_batch_size() {

		var docs = new ArrayList<Integer>();

		var subscriber = EmbeddingService.windowAndBatch(
				Multi.createFrom().iterable(textChunks(5)),
				Map.of(), Map.of(), 0, 2)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		for (byte[] batch : subscriber.getItems()) {
			docs.add(new JsonArray(Buffer.buffer(batch)).size());
		}

		// 5 docs, batchSize 2 -> [2, 2, 1].
		Assertions.assertEquals(List.of(2, 2, 1), docs);
	}

	@Test
	void should_keep_outstanding_bounded_when_the_writer_is_slow() {

		// The pipeline claims O(windowSize + batchSize) memory because it is
		// demand-driven: with a fast source and a consumer that pulls one batch
		// at a time (as EmbeddingProcessor does via transformToUniAndConcatenate),
		// the chunks pulled-from-source-but-not-yet-written must stay bounded and
		// must NOT grow with the stream length.
		int windowSize = 2;
		int batchSize = 32;

		// the observed peak is windowSize + batchSize; the extra batchSize is
		// headroom for the grouping's n * batchSize upstream request rounding.
		int bound = windowSize + 2 * batchSize;

		// measure the peak outstanding at two very different stream lengths.
		int peakShort = maxOutstanding(1_000, windowSize, batchSize);
		int peakLong = maxOutstanding(4_000, windowSize, batchSize);

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

		var bits = EmbeddedChunk.newBuilder()
			.setNumber(1)
			.setVectorDataType(VectorDataType.VECTOR_DATA_TYPE_BINARY)
			.setBits(ByteString.copyFrom(new byte[] {(byte) 0xFF, 0x00, 0x01}))
			.build();
		Assertions.assertEquals(
			List.of(255, 0, 1), EmbeddingService.mapVector(bits));
	}

	@Test
	void should_report_refs_that_produced_no_chunk() {

		var missing = EmbeddingService.missingRefs(
			Set.of("a", "b", "c"), Set.of("a", "c"));

		Assertions.assertEquals(List.of("b"), missing);
	}

	// ---- helpers ---------------------------------------------------------

	/**
	 * Streams {@code n} chunks through {@link EmbeddingService#windowAndBatch}
	 * with a fast source and a consumer that pulls exactly one batch at a time,
	 * writing it before pulling the next. Returns the peak number of chunks
	 * emitted by the source but not yet written by the consumer.
	 */
	private static int maxOutstanding(int n, int windowSize, int batchSize) {

		var emitted = new AtomicInteger();
		var written = new AtomicInteger();
		var peak = new AtomicInteger();

		// a source that emits eagerly, recording the running gap between what it
		// has emitted and what the consumer has written so far.
		var source = Multi.createFrom().iterable(textChunks(n))
			.onItem().invoke(() -> peak.accumulateAndGet(
				emitted.incrementAndGet() - written.get(), Math::max));

		// a slow consumer with zero initial demand.
		var subscriber = EmbeddingService.windowAndBatch(
				source, Map.of(), Map.of(), windowSize, batchSize)
			.subscribe().withSubscriber(AssertSubscriber.create());

		// pull one batch, "write" it, then pull the next, until the stream ends.
		// the request count is guarded (a batch is at least one doc) so a broken
		// pipeline that never completes cannot hang the suite.
		int requests = 0;
		while (!subscriber.hasCompleted() && requests++ < n) {
			subscriber.request(1);

			int docs = 0;
			for (byte[] batch : subscriber.getItems()) {
				docs += new JsonArray(Buffer.buffer(batch)).size();
			}
			written.set(docs);
		}

		// the bounded-demand consumer must have drained the whole stream.
		Assertions.assertTrue(
			subscriber.hasCompleted(),
			"stream did not complete after " + requests + " requests");
		Assertions.assertEquals(n, emitted.get());

		return peak.get();
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize, int batchSize) {

		return runToDocs(chunks, windowSize, batchSize, Map.of(), Map.of());
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize, int batchSize,
		Map<String, Object> root) {

		return runToDocs(chunks, windowSize, batchSize, root, Map.of());
	}

	private static List<JsonObject> runToDocs(
		List<EmbeddedChunk> chunks, int windowSize, int batchSize,
		Map<String, Object> root, Map<String, String> contentTypeByFileId) {

		var subscriber = EmbeddingService.windowAndBatch(
				Multi.createFrom().iterable(chunks),
				root, contentTypeByFileId, windowSize, batchSize)
			.subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));

		subscriber.awaitCompletion();

		List<JsonObject> docs = new ArrayList<>();
		for (byte[] batch : subscriber.getItems()) {
			var array = new JsonArray(Buffer.buffer(batch));
			for (int i = 0; i < array.size(); i++) {
				docs.add(array.getJsonObject(i));
			}
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
