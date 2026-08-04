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

import java.util.List;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.quarkus.common.EventBusInstanceHolder;

import com.typesafe.config.ConfigFactory;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
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
	// delete/bulk futures, so the ask completes deterministically. No cluster.
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

	static byte[] batch() {
		return "[{\"chunkText\":\"a\",\"contentId\":\"content-1\"}]".getBytes();
	}

	static HeldMessage heldMessage() {
		return new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");
	}

	@Test
	void first_batch_deletes_before_indexing_then_acks() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(ChunkStreamWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, heldMessage()));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// write the first batch of the content
		writer.tell(new ChunkStreamWriter.WriteBatch(batch(), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// the delete MUST precede the insert (correctness invariant), so
		// reprocessing does not duplicate the content's chunks.
		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void subsequent_batch_indexes_without_delete() {
		// spawn a writer and consume its first batch
		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(ChunkStreamWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, heldMessage()));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		writer.tell(new ChunkStreamWriter.WriteBatch(batch(), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// write a second batch of the same content
		writer.tell(new ChunkStreamWriter.WriteBatch(batch(), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// no second delete-by-query: later batches only add documents.
		Assertions.assertEquals(
			List.of("delete", "bulk", "bulk"), transport.operations);
	}

	@Test
	void empty_first_batch_acks_without_consuming_the_first_batch() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var writer = TEST_KIT.spawn(ChunkStreamWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, heldMessage()));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		// an empty batch must NOT delete: the prior version stays intact.
		writer.tell(new ChunkStreamWriter.WriteBatch(
			"[]".getBytes(), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		Assertions.assertTrue(transport.operations.isEmpty());

		// and it must not consume the first batch either: the first batch that
		// carries chunks is still the one that drops the prior version.
		writer.tell(new ChunkStreamWriter.WriteBatch(batch(), ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);
		Assertions.assertEquals(List.of("delete", "bulk"), transport.operations);
	}

	@Test
	void end_stream_emits_no_creation_event_when_nothing_was_written() {
		// spawn a writer and close it without any batch
		var writer = TEST_KIT.spawn(ChunkStreamWriter.create(
			new OpenSearchAsyncClient(new RecordingTransport()),
			INDEX_NAME, DATASOURCE_ID, heldMessage()));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));

		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// a zero-chunk stream indexed nothing, so it must not announce a
		// creation to the datasource events.
		Mockito.verify(eventBus, Mockito.never())
			.send(Mockito.anyString(), Mockito.any());
	}

	@Test
	void end_stream_emits_the_creation_event_once_when_a_batch_was_written() {
		// spawn a writer and let it write a batch
		var writer = TEST_KIT.spawn(ChunkStreamWriter.create(
			new OpenSearchAsyncClient(new RecordingTransport()),
			INDEX_NAME, DATASOURCE_ID, heldMessage()));

		var ackProbe = TEST_KIT.<ChunkStreamWriter.Response>createTestProbe();

		writer.tell(new ChunkStreamWriter.WriteBatch(batch(), ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// close the stream
		writer.tell(new ChunkStreamWriter.EndStream(ackProbe.ref()));
		ackProbe.expectMessageClass(ChunkStreamWriter.Ack.class);

		// exactly one creation event, for the content just indexed
		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		var message = Assertions.assertInstanceOf(
			DatasourceMessage.New.class, captor.getValue());
		Assertions.assertEquals("content-1", message.getContentId());
		Assertions.assertEquals(INDEX_NAME, message.getIndexName());
	}

}
