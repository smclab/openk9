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

import io.openk9.datasource.processor.payload.BinaryPayload;
import io.openk9.ml.grpc.EmbeddingOuterClass.EmbeddedChunk;
import io.openk9.ml.grpc.EmbeddingOuterClass.FloatVector;
import io.openk9.ml.grpc.EmbeddingOuterClass.VectorDataType;

import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
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

			// a text chunk carries no binary.
			Assertions.assertFalse(doc.containsKey("binary"));
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
	void should_carry_the_data_of_the_binary_on_its_chunk() {

		// a text chunk and the chunk of an image with sourceUrl and page;
		// the source document still carries its resources
		var text = textChunk(1, 2, "some text");
		var image = binaryChunk(2, "file-1", 3.0f, 4.0f);
		var binary = BinaryPayload.builder()
			.id("file-1")
			.name("photo.png")
			.contentType("image/png")
			.sourceUrl("https://source/photo.png")
			.metadata(Map.of("page", 12))
			.build();
		Map<String, Object> root = Map.of(
			"title", "a title",
			"resources", Map.of("binaries", List.of(Map.of("id", "file-1"))));

		var docs = runToDocs(
			List.of(text, image), 0, root, Map.of("file-1", binary));

		// the image chunk carries its binary
		var binaryDoc = docs.get(1).getJsonObject("binary");
		Assertions.assertEquals("file-1", binaryDoc.getString("id"));
		Assertions.assertEquals("photo.png", binaryDoc.getString("name"));
		Assertions.assertEquals("image/png", binaryDoc.getString("contentType"));
		Assertions.assertEquals(
			"https://source/photo.png", binaryDoc.getString("sourceUrl"));
		Assertions.assertEquals(
			12, binaryDoc.getJsonObject("metadata").getInteger("page"));

		// the text chunk does not
		Assertions.assertFalse(docs.get(0).containsKey("binary"));

		// no chunk carries fileId, media_type or resources
		for (var doc : docs) {
			Assertions.assertFalse(doc.containsKey("fileId"));
			Assertions.assertFalse(doc.containsKey("media_type"));
			Assertions.assertFalse(doc.containsKey("resources"));
			Assertions.assertEquals("a title", doc.getString("title"));
		}
	}

	@Test
	void should_carry_on_each_chunk_only_its_own_binary() {

		// three images of the same content, pages 1, 2 and 3
		Map<String, BinaryPayload> binaries = Map.of(
			"img-1", binary("img-1", Map.of("page", 1)),
			"img-2", binary("img-2", Map.of("page", 2)),
			"img-3", binary("img-3", Map.of("page", 3)));

		var docs = runToDocs(
			List.of(
				binaryChunk(1, "img-1", 1.0f),
				binaryChunk(2, "img-2", 1.0f),
				binaryChunk(3, "img-3", 1.0f)),
			0, Map.of(), binaries);

		// three image chunks, each with the id and page of its image
		Assertions.assertEquals(3, docs.size());
		for (int i = 0; i < 3; i++) {
			var binaryDoc = docs.get(i).getJsonObject("binary");
			Assertions.assertEquals("img-" + (i + 1), binaryDoc.getString("id"));
			Assertions.assertEquals(
				i + 1, binaryDoc.getJsonObject("metadata").getInteger("page"));
		}
	}

	@Test
	void should_carry_id_name_and_content_type_of_a_bare_binary() {

		// an image without sourceUrl and metadata
		var docs = runToDocs(
			List.of(binaryChunk(1, "img-1", 1.0f)),
			0, Map.of(), Map.of("img-1", binary("img-1", null)));

		// the binary carries only id, name and contentType
		Assertions.assertEquals(
			new JsonObject()
				.put("id", "img-1")
				.put("name", "img-1.png")
				.put("contentType", "image/png"),
			docs.get(0).getJsonObject("binary"));
	}

	@Test
	void should_let_the_metadata_of_the_chunk_win() {

		// a PDF with page 1 and a chunk the module sent with page 3
		var pdf = binary("pdf-1", Map.of("page", 1, "caption", "a report"));
		var chunk = binaryChunk(1, "pdf-1", 1.0f).toBuilder()
			.setMetadata(Struct.newBuilder()
				.putFields("page", Value.newBuilder().setNumberValue(3).build()))
			.build();

		var docs = runToDocs(List.of(chunk), 0, Map.of(), Map.of("pdf-1", pdf));

		// the page is the chunk's, as an integer; the other keys stay
		var metadata = docs.get(0)
			.getJsonObject("binary")
			.getJsonObject("metadata");
		Assertions.assertInstanceOf(Integer.class, metadata.getValue("page"));
		Assertions.assertEquals(3, metadata.getInteger("page"));
		Assertions.assertEquals("a report", metadata.getString("caption"));
	}

	@Test
	void should_not_window_text_chunks_with_binary_chunks() {

		var docs = runToDocs(
			List.of(
				textChunk(1, 3, "text 1"),
				textChunk(2, 3, "text 2"),
				textChunk(3, 3, "text 3"),
				binaryChunk(4, "image-a")),
			1);

		Assertions.assertEquals(
			new JsonArray().add(windowEntry(2, "text 2")),
			docs.get(2).getJsonArray("previous"));
		Assertions.assertTrue(docs.get(2).getJsonArray("next").isEmpty());

		Assertions.assertTrue(docs.get(3).getJsonArray("previous").isEmpty());
		Assertions.assertTrue(docs.get(3).getJsonArray("next").isEmpty());
	}

	@Test
	void should_window_each_binary_on_its_own() {

		// text, then two binaries of 2 and 3 chunks: every window stays inside
		// the content its chunk belongs to, numbers stay progressive.
		var docs = runToDocs(
			List.of(
				textChunk(1, 1, "text 1"),
				binaryChunk(2, "sub-it"),
				binaryChunk(3, "sub-it"),
				binaryChunk(4, "sub-en"),
				binaryChunk(5, "sub-en"),
				binaryChunk(6, "sub-en")),
			2);

		Assertions.assertEquals(6, docs.size());

		var text = docs.get(0);
		Assertions.assertTrue(text.getJsonArray("previous").isEmpty());
		Assertions.assertTrue(text.getJsonArray("next").isEmpty());

		var lastItalian = docs.get(2);
		Assertions.assertEquals(3, lastItalian.getInteger("number"));
		Assertions.assertEquals(
			new JsonArray().add(windowEntry(2, "binary-chunk-2")),
			lastItalian.getJsonArray("previous"));
		Assertions.assertTrue(lastItalian.getJsonArray("next").isEmpty());

		var firstEnglish = docs.get(3);
		Assertions.assertEquals(4, firstEnglish.getInteger("number"));
		Assertions.assertTrue(firstEnglish.getJsonArray("previous").isEmpty());
		Assertions.assertEquals(
			new JsonArray()
				.add(windowEntry(5, "binary-chunk-5"))
				.add(windowEntry(6, "binary-chunk-6")),
			firstEnglish.getJsonArray("next"));
	}

	@Test
	void should_default_the_content_type_when_it_is_unknown() {

		// a binary ref whose contentType was not provided (not in the map):
		// contentType falls back to the generic octet-stream, never null.
		var binary = binaryChunk(1, "file-x", 1.0f);

		var docs = runToDocs(List.of(binary), 0, Map.of(), Map.of());

		var binaryDoc = docs.get(0).getJsonObject("binary");
		Assertions.assertEquals("file-x", binaryDoc.getString("id"));
		Assertions.assertEquals(
			"application/octet-stream", binaryDoc.getString("contentType"));
	}

	@Test
	void should_use_the_caption_as_the_text_of_an_image() {

		// the same image, with and without a caption
		var chunk = imageChunk(1, "img-1", 3.0f, 4.0f);
		var captioned = binary("img-1", Map.of("caption", "schema impianto 12V"));

		var doc = runToDocs(
			List.of(chunk), 0, Map.of(), Map.of("img-1", captioned)).get(0);
		var bare = runToDocs(
			List.of(chunk), 0, Map.of(), Map.of("img-1", binary("img-1", null)))
			.get(0);

		// the caption is the text, the vector does not change
		Assertions.assertEquals(
			"schema impianto 12V", doc.getString("chunkText"));
		Assertions.assertEquals(
			bare.getJsonArray("vector"), doc.getJsonArray("vector"));
	}

	@Test
	void should_leave_empty_the_text_of_an_image_without_caption() {

		// an image with a name and a blank caption, one with no metadata
		var blank = binary("img-1", Map.of("caption", " "));
		var bare = binary("img-2", null);

		var docs = runToDocs(
			List.of(imageChunk(1, "img-1", 1.0f), imageChunk(2, "img-2", 1.0f)),
			0, Map.of(), Map.of("img-1", blank, "img-2", bare));

		// the text stays empty, the name is not used
		for (var doc : docs) {
			Assertions.assertEquals("", doc.getString("chunkText"));
		}
	}

	@Test
	void should_keep_the_text_the_module_sent() {

		// an image the module sent a text for, and a text file with a caption
		var image = binary("img-1", Map.of("caption", "a caption"));
		var textFile = BinaryPayload.builder()
			.id("txt-1")
			.contentType("text/plain")
			.metadata(Map.of("caption", "a caption"))
			.build();
		var line = binaryChunk(2, "txt-1", 1.0f);

		var docs = runToDocs(
			List.of(
				binaryChunk(1, "img-1", 1.0f),
				line,
				line.toBuilder().setNumber(3).clearText().build()),
			0, Map.of(), Map.of("img-1", image, "txt-1", textFile));

		// the module's text wins; the caption fills only the text of an image
		Assertions.assertEquals(
			"binary-chunk-1", docs.get(0).getString("chunkText"));
		Assertions.assertEquals(
			"binary-chunk-2", docs.get(1).getString("chunkText"));
		Assertions.assertEquals("", docs.get(2).getString("chunkText"));
	}

	@Test
	void should_keep_the_caption_out_of_the_windows() {

		// a text in two chunks and an image with a caption
		var image = binary("img-1", Map.of("caption", "schema impianto 12V"));

		var docs = runToDocs(
			List.of(
				textChunk(1, 2, "first", 1.0f),
				textChunk(2, 2, "second", 1.0f),
				imageChunk(3, "img-1", 1.0f)),
			1, Map.of(), Map.of("img-1", image));

		// the image chunk has the caption, no window of the text carries it
		Assertions.assertEquals(
			"schema impianto 12V", docs.get(2).getString("chunkText"));
		for (var doc : docs.subList(0, 2)) {
			var windows = doc.getJsonArray("previous").encode()
				+ doc.getJsonArray("next").encode();
			Assertions.assertFalse(windows.contains("schema impianto 12V"));
		}
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
		Map<String, Object> root, Map<String, BinaryPayload> binaryByFileId) {

		var subscriber = EmbeddingService.windowAndEncode(
				Multi.createFrom().iterable(chunks),
				root, binaryByFileId, windowSize)
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

	private static JsonObject windowEntry(int number, String text) {

		return new JsonObject().put("number", number).put("chunkText", text);
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

	private static BinaryPayload binary(
		String id, Map<String, Object> metadata) {

		return BinaryPayload.builder()
			.id(id)
			.name(id + ".png")
			.contentType("image/png")
			.metadata(metadata)
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

	/** The chunk of an image: the module sends it without a text. */
	private static EmbeddedChunk imageChunk(
		int number, String fileId, float... vector) {

		return binaryChunk(number, fileId, vector).toBuilder()
			.clearText()
			.build();
	}

}
