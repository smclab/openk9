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

import java.time.Duration;
import java.util.List;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.quarkus.common.EventBusInstanceHolder;

import com.typesafe.config.ConfigFactory;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;

class ChunkStreamWriterTest {

	static final String INDEX_NAME = "tenant-data-index";

	static final long DATASOURCE_ID = 1L;

	static final ShardingKey SHARDING_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1");

	// a plain, local ActorSystem: real dispatchers drive the writer's piped
	// delete/bulk futures and its flush timer, so the ask completes
	// deterministically. No cluster.
	static final ActorTestKit TEST_KIT = ActorTestKit.create(
		ConfigFactory.parseString("pekko.actor.provider = local"));

	// the datasource events go through a static holder; a mock lets the test
	// assert on them without a Quarkus container.
	EventBus eventBus;

	@AfterAll
	static void tearDownTestKit() {
		TEST_KIT.shutdownTestKit();
	}

	@BeforeEach
	void setUpEventBus() {
		eventBus = Mockito.mock(EventBus.class);
		EventBusInstanceHolder.setEventBus(eventBus);
	}

	@AfterEach
	void tearDownEventBus() {
		EventBusInstanceHolder.setEventBus(null);
	}

	/** One finalized chunk-doc, the unit the stream now emits. */
	static byte[] chunkDoc(int number) {
		return ("{\"number\":" + number
			+ ",\"chunkText\":\"a\",\"contentId\":\"content-1\"}").getBytes();
	}

	static HeldMessage heldMessage() {
		return new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");
	}

	private static org.apache.pekko.actor.typed.ActorRef<ChunkStreamWriter.Command>
	writerOn(OpenSearchAsyncClient client) {

		return TEST_KIT.spawn(ChunkStreamWriter.create(
			client, INDEX_NAME, DATASOURCE_ID, heldMessage()));
	}

	/** Feeds {@code count} docs and consumes their acknowledgements. */
	private static void feed(
		org.apache.pekko.actor.typed.ActorRef<ChunkStreamWriter.Command> writer,
		TestProbe<ChunkStreamWriter.Response> ackProbe,
		int count) {

		for (int number = 1; number <= count; number++) {
			writer.tell(new ChunkStreamWriter.WriteChunk(
				chunkDoc(number), ackProbe.ref()));
		}

		for (int number = 1; number <= count; number++) {
			ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		}
	}

	@Test
	void a_buffered_chunk_is_acknowledged_without_touching_the_index() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// a single doc does not fill a batch: it is accepted and answered at
		// once, so the stream keeps flowing while the batch fills.
		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(1), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		Assertions.assertTrue(transport.operations.isEmpty());
	}

	@Test
	void a_full_batch_is_written_without_waiting_for_the_close() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		feed(writer, ackProbe, ChunkStreamWriter.BATCH_SIZE);

		// the delete MUST precede the insert (correctness invariant), so
		// reprocessing does not duplicate the content's chunks.
		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);

		// a second full batch only adds documents: no second delete-by-query.
		feed(writer, ackProbe, ChunkStreamWriter.BATCH_SIZE);

		Assertions.assertEquals(
			List.of("delete", "bulk", "bulk"), transport.operations);
	}

	@Test
	void the_timer_writes_a_partial_batch_on_its_own() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// one doc, far from filling a batch, and nothing else happens: no
		// close, no further chunk.
		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(1), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// the batch is written anyway once the max delay expires: it is what
		// keeps a slow stream (one embedding per image) progressively visible.
		transport.awaitOperations(2);

		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void the_close_writes_the_tail_and_announces_the_creation_once() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(1), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// closing before the timer fires must still write what is buffered
		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);

		// exactly one creation event, for the content just indexed
		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		var message = Assertions.assertInstanceOf(
			DatasourceMessage.New.class, captor.getValue());
		Assertions.assertEquals("content-1", message.getContentId());
		Assertions.assertEquals(INDEX_NAME, message.getIndexName());
	}

	@Test
	void zero_chunk_stream_succeeds_and_keeps_the_prior_indexed_version() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// the module answered the document with no chunk at all
		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));

		// the document is closed successfully. This is a deliberate difference
		// from the single-response GetMessages path, where an empty response
		// failed the document ("No chunks created from this payload").
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// nothing was deleted and nothing was indexed: on a reprocessing the
		// previously indexed version of the content survives untouched.
		Assertions.assertTrue(transport.operations.isEmpty());

		// and no creation is announced, since nothing was created
		Mockito.verify(eventBus, Mockito.never())
			.send(Mockito.anyString(), Mockito.any());
	}

	@Test
	void an_empty_payload_does_not_consume_the_first_batch() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// an empty payload must NOT delete: the prior version stays intact.
		writer.tell(new ChunkStreamWriter.WriteChunk(
			"[]".getBytes(), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		Assertions.assertTrue(transport.operations.isEmpty());

		// and the first batch that carries chunks is still the one that drops
		// the prior version.
		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(1), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void a_chunk_waits_for_the_pending_write() {
		// spawn a writer whose responses the test releases one at a time
		var transport = RecordingTransport.gated();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// fill a batch: the last doc starts the delete and hangs there
		for (int number = 1; number <= ChunkStreamWriter.BATCH_SIZE; number++) {
			writer.tell(new ChunkStreamWriter.WriteChunk(
				chunkDoc(number), ackProbe.ref()));
		}

		// the first 31 are acknowledged at once, the 32nd is not
		for (int number = 1; number < ChunkStreamWriter.BATCH_SIZE; number++) {
			ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		}
		transport.awaitOperations(1);

		// a further doc arrives while the delete is still in flight
		writer.tell(new ChunkStreamWriter.WriteChunk(
			chunkDoc(99), ackProbe.ref()));

		// it must not overtake it: indexing before the delete completes would
		// have the delete wipe the documents just written.
		ackProbe.expectNoMessage(Duration.ofMillis(300));
		Assertions.assertEquals(List.of("delete"), transport.operations);

		// delete completes, the batch is indexed, and only then both the
		// batch's doc and the one held behind it are answered
		transport.release();
		transport.awaitOperations(2);
		transport.release();

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void the_close_waits_for_the_pending_write() {
		// spawn a writer whose responses the test releases one at a time
		var transport = RecordingTransport.gated();
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		for (int number = 1; number <= ChunkStreamWriter.BATCH_SIZE; number++) {
			writer.tell(new ChunkStreamWriter.WriteChunk(
				chunkDoc(number), ackProbe.ref()));
		}
		for (int number = 1; number < ChunkStreamWriter.BATCH_SIZE; number++) {
			ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		}
		transport.awaitOperations(1);

		// the stream is closed while the write is still running: closing now
		// would stop the writer, send its own delete/bulk responses to dead
		// letters and leave the caller waiting for an ack until the timeout.
		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));
		ackProbe.expectNoMessage(Duration.ofMillis(300));

		transport.release();
		transport.awaitOperations(2);
		transport.release();

		// the batch's doc, then the close
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);

		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		Assertions.assertInstanceOf(
			DatasourceMessage.New.class, captor.getValue());
	}

	@Test
	void a_failed_timer_flush_is_reported_to_the_next_caller() {
		// a transport whose bulk always reports a failed item
		var transport = new RecordingTransport(
			RecordingTransport.failedBulk("mapper_parsing_exception"));
		var writer = writerOn(new OpenSearchAsyncClient(transport));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// buffered and answered: at this point nothing has been written yet
		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(1), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// the timer writes the partial batch, and the bulk fails with nobody
		// waiting on it
		transport.awaitOperations(2);

		// the failure is not lost: the next caller gets it, so the stream still
		// fails fast instead of running to the end on a broken index.
		writer.tell(new ChunkStreamWriter.WriteChunk(chunkDoc(2), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Failure.class);
	}

}
