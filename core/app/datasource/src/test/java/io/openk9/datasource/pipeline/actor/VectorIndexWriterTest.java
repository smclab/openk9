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

package io.openk9.datasource.pipeline.actor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.TestUtils;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.transport.Endpoint;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.TransportOptions;

class VectorIndexWriterTest {

	static byte[] chunks = TestUtils
		.getResourceAsJsonArray("vectoridxwriter/chunks.json")
		.toBuffer()
		.getBytes();

	static byte[] document = TestUtils
		.getResourceAsJsonObject("vectoridxwriter/document.json")
		.toBuffer()
		.getBytes();

	static byte[] empty = new byte[]{};

	static byte[] emptyArray = "[]".getBytes();

	static Sample johnDoe = new Sample("John", "Doe", "john.doe@acme.com", 20);

	@Test
	void should_get_chunks_as_list() {
		var chunks = VectorIndexWriter.parseChunks(VectorIndexWriterTest.chunks);

		Assertions.assertEquals(3, chunks.size());

		var chunk = chunks.getFirst();
		var metadata = (Map<String, Object>) chunk.get("sample");
		var sample = new Sample(
			(String) metadata.get("firstName"),
			(String) metadata.get("lastName"),
			(String) metadata.get("email"),
			(int) metadata.get("age")
		);

		Assertions.assertEquals(johnDoe, sample);
	}

	@Test
	void should_get_document_as_list() {
		var document = VectorIndexWriter.parseChunks(VectorIndexWriterTest.document);

		Assertions.assertEquals(1, document.size());

		var chunk = (Map<String, Object>) document.getFirst();
		var metadata = (Map<String, Object>) chunk.get("sample");
		var sample = new Sample(
			(String) metadata.get("firstName"),
			(String) metadata.get("lastName"),
			(String) metadata.get("email"),
			(int) metadata.get("age")
		);

		Assertions.assertEquals(johnDoe, sample);
	}

	@Test
	void should_get_emptyArray_as_empty_list() {
		var emptyArray = VectorIndexWriter.parseChunks(VectorIndexWriterTest.emptyArray);

		Assertions.assertTrue(emptyArray.isEmpty());
	}

	@Test
	void should_throws_on_empty_string() {

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> VectorIndexWriter.parseChunks(VectorIndexWriterTest.empty)
		);

	}


	// ---- streaming incremental write -------------------------------------

	static final String INDEX_NAME = "tenant-data-index";

	static final ShardingKey SHARDING_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1");

	// a plain, local ActorSystem: real dispatchers drive the writer's piped
	// delete/bulk futures, so the ask completes deterministically. No cluster.
	static final ActorTestKit TEST_KIT = ActorTestKit.create(
		ConfigFactory.parseString("pekko.actor.provider = local"));

	@AfterAll
	static void tearDown() {
		TEST_KIT.shutdownTestKit();
	}

	static byte[] batch() {
		return "[{\"chunkText\":\"a\",\"contentId\":\"content-1\"}]".getBytes();
	}

	@Test
	void first_batch_deletes_before_indexing_then_acks() {

		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(VectorIndexWriter.create(
			new OpenSearchAsyncClient(transport), INDEX_NAME, 1L,
			TEST_KIT.<Writer.Response>createTestProbe().ref()));

		var ackProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var heldMessage = new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");

		writer.tell(new Writer.WriteBatch(
			batch(), true, heldMessage, ackProbe.ref()));

		var ack = ackProbe.expectMessageClass(Writer.BatchAck.class);
		Assertions.assertEquals(heldMessage, ack.heldMessage());

		// the delete MUST precede the insert (correctness invariant), so
		// reprocessing does not duplicate the content's chunks.
		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void subsequent_batch_indexes_without_delete() {

		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(VectorIndexWriter.create(
			new OpenSearchAsyncClient(transport), INDEX_NAME, 1L,
			TEST_KIT.<Writer.Response>createTestProbe().ref()));

		var ackProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var heldMessage = new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");

		writer.tell(new Writer.WriteBatch(
			batch(), false, heldMessage, ackProbe.ref()));

		ackProbe.expectMessageClass(Writer.BatchAck.class);

		// no delete-by-query: later batches only add documents.
		Assertions.assertEquals(List.of("bulk"), transport.operations);
	}

	@Test
	void empty_first_batch_acks_without_deleting() {

		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(VectorIndexWriter.create(
			new OpenSearchAsyncClient(transport), INDEX_NAME, 1L,
			TEST_KIT.<Writer.Response>createTestProbe().ref()));

		var ackProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var heldMessage = new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");

		// an empty first batch must NOT delete: the prior version stays intact.
		writer.tell(new Writer.WriteBatch(
			"[]".getBytes(), true, heldMessage, ackProbe.ref()));

		ackProbe.expectMessageClass(Writer.BatchAck.class);
		Assertions.assertTrue(transport.operations.isEmpty());
	}

	record Sample(
		String firstName,
		String lastName,
		String email,
		int age
	) {}

	/**
	 * Records the OpenSearch operations issued and returns canned successful
	 * responses; the writer inspects only the completion, not the body.
	 */
	static final class RecordingTransport implements OpenSearchTransport {

		final List<String> operations =
			Collections.synchronizedList(new java.util.ArrayList<>());
		private final JsonpMapper mapper = new JacksonJsonpMapper();

		@Override
		public <RequestT, ResponseT, ErrorT> ResponseT performRequest(
			RequestT request,
			Endpoint<RequestT, ResponseT, ErrorT> endpoint,
			TransportOptions options) {

			throw new UnsupportedOperationException("async only");
		}

		@Override
		@SuppressWarnings("unchecked")
		public <RequestT, ResponseT, ErrorT> CompletableFuture<ResponseT>
		performRequestAsync(
			RequestT request,
			Endpoint<RequestT, ResponseT, ErrorT> endpoint,
			TransportOptions options) {

			if (request instanceof DeleteByQueryRequest) {
				operations.add("delete");
				return CompletableFuture.completedFuture(
					(ResponseT) DeleteByQueryResponse.of(b -> b));
			}

			if (request instanceof BulkRequest) {
				operations.add("bulk");
				return CompletableFuture.completedFuture((ResponseT)
					BulkResponse.of(b -> b
						.took(0)
						.errors(false)
						.items(List.of())));
			}

			throw new IllegalArgumentException("unexpected request: " + request);
		}

		@Override
		public JsonpMapper jsonpMapper() {
			return mapper;
		}

		@Override
		public TransportOptions options() {
			return TransportOptions.builder().build();
		}

		@Override
		public void close() {
		}
	}

}