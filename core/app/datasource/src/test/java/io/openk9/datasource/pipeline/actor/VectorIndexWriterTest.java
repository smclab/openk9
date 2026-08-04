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

class VectorIndexWriterTest {

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

	static byte[] payload() {
		return "[{\"chunkText\":\"a\",\"contentId\":\"content-1\"}]".getBytes();
	}

	static HeldMessage heldMessage() {
		return new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");
	}

	@Test
	void start_deletes_then_indexes_and_replies_success() {
		// spawn a writer on a transport that records the operations issued
		var transport = new RecordingTransport();
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(VectorIndexWriter.create(
			new OpenSearchAsyncClient(transport),
			INDEX_NAME, DATASOURCE_ID, replyProbe.ref()));

		// write the one-shot payload of a document
		writer.tell(new Writer.Start(payload(), heldMessage()));

		replyProbe.expectMessageClass(Writer.Success.class);

		// the delete MUST precede the insert (correctness invariant), so
		// reprocessing does not duplicate the content's chunks.
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
	void bulk_errors_reply_failure_and_emit_the_error_event() {
		// spawn a writer whose bulk responses carry an item error
		var transport = new RecordingTransport(
			RecordingTransport.failedBulk("boom"));
		var replyProbe = TEST_KIT.<Writer.Response>createTestProbe();
		var writer = TEST_KIT.spawn(VectorIndexWriter.create(
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

}
