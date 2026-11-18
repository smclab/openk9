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

package io.openk9.datasource.pipeline.stages.working;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.openk9.common.util.ShardingKey;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.VectorIndexWriter;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;

import io.vertx.core.json.Json;
import org.apache.pekko.actor.testkit.typed.Effect;
import org.apache.pekko.actor.testkit.typed.javadsl.BehaviorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestInbox;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkStageTest {

	static final ShardingKey SHARDING_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1");

	static SchedulerDTO scheduler() {
		var scheduler = new SchedulerDTO();
		scheduler.setDatasourceId(1L);

		return scheduler;
	}

	@Test
	void should_reply_invalid_when_partial_document_has_no_contentId() {
		// setup a WorkStage with test inboxes
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), VectorIndexWriter::create)
		));

		// send a PARTIAL_DOCUMENT payload without contentId
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.PARTIAL_DOCUMENT)
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		// the validation fails with an Invalid response
		var response = replyToInbox.receiveMessage();
		var invalid = Assertions.assertInstanceOf(WorkStage.Invalid.class, response);
		Assertions.assertEquals("content-id is null", invalid.errorMessage());
	}

	@Test
	void should_start_partial_writer_bypassing_the_processor_chain() {
		// setup a WorkStage with test inboxes
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), VectorIndexWriter::create)
		));
		workStage.getAllEffects();

		// send a PARTIAL_DOCUMENT payload with a valid contentId
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.PARTIAL_DOCUMENT)
				.contentId("content-1")
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		// the message is held as Working
		var response = replyToInbox.receiveMessage();
		var working = Assertions.assertInstanceOf(WorkStage.Working.class, response);
		Assertions.assertEquals("content-1", working.heldMessage().contentId());

		// only the partial writer is spawned: no ProcessorChain
		var spawned = workStage.getAllEffects()
			.stream()
			.filter(effect -> effect instanceof Effect.SpawnedAnonymous<?>)
			.toList();

		Assertions.assertEquals(1, spawned.size());
	}

	@Test
	void should_delete_the_content_of_a_deleted_document() {
		// setup a WorkStage with test inboxes
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), (dto, writerAdapter) -> Behaviors.ignore())
		));
		workStage.getAllEffects();

		// send a DELETED_DOCUMENT payload with a valid contentId
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DELETED_DOCUMENT)
				.contentId("content-1")
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		// the deletion is held as Working, so Scheduling can track it
		var response = replyToInbox.receiveMessage();
		var working = Assertions.assertInstanceOf(WorkStage.Working.class, response);
		Assertions.assertEquals("content-1", working.heldMessage().contentId());

		// the writer is asked to delete the content, with no ProcessorChain
		assertOnlyTheWriterDeletes(workStage);
	}

	@Test
	void should_reply_invalid_when_deleted_document_has_no_contentId() {
		// setup a WorkStage with test inboxes
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), (dto, writerAdapter) -> Behaviors.ignore())
		));

		// send a DELETED_DOCUMENT payload without contentId
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DELETED_DOCUMENT)
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		// the validation fails with an Invalid response
		var response = replyToInbox.receiveMessage();
		var invalid = Assertions.assertInstanceOf(WorkStage.Invalid.class, response);
		Assertions.assertEquals("content-id is null", invalid.errorMessage());
	}

	@Test
	void should_delete_a_deleted_document_that_has_documentTypes() {
		// setup a WorkStage with test inboxes
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), (dto, writerAdapter) -> Behaviors.ignore())
		));
		workStage.getAllEffects();

		// send a DELETED_DOCUMENT payload that also has documentTypes
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DELETED_DOCUMENT)
				.contentId("content-1")
				.documentTypes(new String[] {"web"})
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		// the type wins over the documentTypes: the content is deleted
		Assertions.assertInstanceOf(
			WorkStage.Working.class, replyToInbox.receiveMessage());

		assertOnlyTheWriterDeletes(workStage);
	}

	@Test
	void should_give_a_distinct_process_key_to_each_instance() throws Exception {
		// two WorkStages for the same scheduling, as after a restart
		var first = workingKeyOf(newWorkStage());
		Thread.sleep(2);
		var second = workingKeyOf(newWorkStage());

		// both keys address the same scheduling
		Assertions.assertEquals(SHARDING_KEY, first.baseKey());
		Assertions.assertEquals(SHARDING_KEY, second.baseKey());

		// but the first message of each instance gets its own entity
		Assertions.assertNotEquals(first, second);
	}

	@Test
	void should_number_the_messages_within_an_instance() {
		// two messages through the same WorkStage
		var workStage = newWorkStage();
		var first = workingKeyOf(workStage);
		var second = workingKeyOf(workStage);

		// same instance segment, different counter
		Assertions.assertEquals(4, first.elements().length);
		Assertions.assertEquals(first.elements()[2], second.elements()[2]);
		Assertions.assertNotEquals(first, second);
	}

	// checks that the WorkStage spawned only the writer, with no
	// ProcessorChain, and asked it to delete: a Start without payload
	private static void assertOnlyTheWriterDeletes(
		BehaviorTestKit<WorkStage.Command> workStage) {

		var spawned = workStage.getAllEffects()
			.stream()
			.filter(effect -> effect instanceof Effect.SpawnedAnonymous<?>)
			.map(effect -> (Effect.SpawnedAnonymous<?>) effect)
			.toList();

		Assertions.assertEquals(1, spawned.size());

		var writerInbox = workStage.childInbox(spawned.get(0).ref());
		var start = Assertions.assertInstanceOf(
			Writer.Start.class, writerInbox.receiveMessage());

		Assertions.assertNull(start.dataPayload());
	}

	private static BehaviorTestKit<WorkStage.Command> newWorkStage() {
		var replyToInbox = TestInbox.<WorkStage.Response>create();
		var workStage = BehaviorTestKit.create(WorkStage.create(
			SHARDING_KEY,
			replyToInbox.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(), (dto, writerAdapter) -> Behaviors.ignore())
		));
		REPLY_TO_INBOXES.put(workStage, replyToInbox);

		return workStage;
	}

	// drives a deletion through the WorkStage and returns the process key it
	// assigned to the message
	private static ShardingKey workingKeyOf(BehaviorTestKit<WorkStage.Command> workStage) {
		var requesterInbox = TestInbox.<Scheduling.Response>create();
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DOCUMENT)
				.contentId("content-1")
				.build())
			.getBytes();

		workStage.run(new WorkStage.StartWorker(
			scheduler(), payload, requesterInbox.getRef()));

		var working = Assertions.assertInstanceOf(
			WorkStage.Working.class,
			REPLY_TO_INBOXES.get(workStage).receiveMessage());

		return working.heldMessage().processKey();
	}

	private static final Map<BehaviorTestKit<WorkStage.Command>, TestInbox<WorkStage.Response>>
		REPLY_TO_INBOXES = new HashMap<>();

}
