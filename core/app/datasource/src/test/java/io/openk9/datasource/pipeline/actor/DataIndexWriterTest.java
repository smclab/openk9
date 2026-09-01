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
import io.openk9.datasource.pipeline.stages.working.Writer;
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

class DataIndexWriterTest {

	static final String INDEX_NAME = "tenant-data-index";

	static final long DATASOURCE_ID = 1L;

	static final ShardingKey SHARDING_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1");

	// a plain, local ActorSystem: real dispatchers drive the writer's piped
	// delete/bulk futures, so the replies complete deterministically. No cluster.
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

	// the enrich pipeline hands over one JSON object, the whole document
	static byte[] payload() {
		return "{\"contentId\":\"content-1\",\"title\":\"a\"}".getBytes();
	}

	static HeldMessage heldMessage() {
		return new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");
	}

	@Test
	void start_deletes_then_indexes_and_replies_success() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(DataIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		// write the one-shot payload of a document
		writer.tell(new Writer.Start(payload(), heldMessage()));

		replyProbe.expectMessageClass(Writer.Success.class);

		// the delete MUST precede the insert (correctness invariant), so
		// reprocessing does not duplicate the document.
		Assertions.assertEquals(List.of("delete", "index"), transport.operations);

		// exactly one creation event, for the content just indexed
		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		var message = Assertions.assertInstanceOf(
			DatasourceMessage.New.class, captor.getValue());
		Assertions.assertEquals("content-1", message.getContentId());
		Assertions.assertEquals(INDEX_NAME, message.getIndexName());
	}

	@Test
	void start_without_payload_only_deletes_and_replies_once() {
		var transport = new RecordingTransport();
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(DataIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		// a document with no documentTypes: it is only deleted
		writer.tell(new Writer.Start(null, heldMessage()));

		// one reply for one document: no parsing is attempted on the missing
		// payload, so no spurious failure precedes the success.
		replyProbe.expectMessageClass(Writer.Success.class);
		replyProbe.expectNoMessage();

		Assertions.assertEquals(List.of("delete"), transport.operations);

		// exactly one deletion event, for the content just dropped
		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		var message = Assertions.assertInstanceOf(
			DatasourceMessage.Delete.class, captor.getValue());
		Assertions.assertEquals("content-1", message.getContentId());
		Assertions.assertEquals(INDEX_NAME, message.getIndexName());
	}

	@Test
	void a_rejected_document_replies_failure_and_emits_the_error_event() {
		// spawn a writer whose index requests are rejected
		var transport = RecordingTransport.failing("index", "boom");
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(DataIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		writer.tell(new Writer.Start(payload(), heldMessage()));

		// the document fails and the datasource events carry the bulk error
		replyProbe.expectMessageClass(Writer.Failure.class);

		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		var message = Assertions.assertInstanceOf(
			DatasourceMessage.Failure.class, captor.getValue());
		Assertions.assertEquals("content-1", message.getContentId());
	}

	@Test
	void a_failed_delete_replies_failure_and_emits_the_error_event() {
		// spawn a writer whose delete-by-contentId is rejected
		var transport = RecordingTransport.failing("delete", "boom");
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(DataIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		writer.tell(new Writer.Start(payload(), heldMessage()));

		replyProbe.expectMessageClass(Writer.Failure.class);

		// the document is never written on top of the version that could not be
		// dropped, and the failure reaches the datasource events like any other.
		Assertions.assertEquals(List.of("delete"), transport.operations);

		var captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(eventBus).send(Mockito.anyString(), captor.capture());

		Assertions.assertInstanceOf(
			DatasourceMessage.Failure.class, captor.getValue());
	}

	@Test
	void a_payload_that_is_not_a_document_fails_without_touching_the_index() {
		var transport = new RecordingTransport();
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(DataIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		// the writer answers the document instead of crashing on the cast: a
		// dead writer would leave the work stage waiting for its timeout.
		writer.tell(new Writer.Start("[]".getBytes(), heldMessage()));

		replyProbe.expectMessageClass(Writer.Failure.class);

		// nothing was deleted: the version already indexed survives a payload
		// the writer cannot read.
		Assertions.assertEquals(List.of(), transport.operations);
	}

}
