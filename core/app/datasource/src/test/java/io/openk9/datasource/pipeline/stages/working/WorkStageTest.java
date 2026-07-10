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

import java.util.LinkedList;

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

}
