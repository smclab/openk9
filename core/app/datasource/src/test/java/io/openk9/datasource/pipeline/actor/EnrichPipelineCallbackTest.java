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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.pipeline.actor.enrichitem.CallbackToken;
import io.openk9.datasource.pipeline.service.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.processor.payload.DataPayload;

import com.typesafe.config.ConfigFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The callback of an HTTP_ASYNC enricher is addressed to the pipeline
 * entity itself: these tests drive the entity with a fake HTTP actor that
 * answers 200 at once and never calls back, so the test plays the enricher.
 */
class EnrichPipelineCallbackTest {

	// a plain local ActorSystem, no cluster: the entity behavior is tested
	// as an ordinary actor
	static final ActorTestKit TEST_KIT = ActorTestKit.create(
		ConfigFactory.parseString("pekko.actor.provider = local"));

	static final ShardingKey PROCESS_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1", "m3k9x", "1");

	static final byte[] CALLBACK_BODY =
		"{\"stub\":{\"done\":true}}".getBytes(StandardCharsets.UTF_8);

	@AfterAll
	static void shutdown() {
		TEST_KIT.shutdownTestKit();
	}

	@Test
	void should_reject_callback_when_no_pipeline_is_running_and_stop() {
		// an entity recreated by the sharding for a stale callback
		var posts = TEST_KIT.<Http.POST>createTestProbe();
		var pipeline = TEST_KIT.spawn(EnrichPipeline.create(PROCESS_KEY, fakeHttp(posts)));
		var callbacks = TEST_KIT.<EnrichPipeline.CallbackResponse>createTestProbe();

		// the callback is unknown and the entity does not linger
		pipeline.tell(new EnrichPipeline.Callback("nonce", CALLBACK_BODY, callbacks.ref()));

		callbacks.expectMessage(EnrichPipeline.CallbackResponse.UNKNOWN);
		TEST_KIT.createTestProbe().expectTerminated(pipeline);
	}

	@Test
	void should_deliver_the_awaited_callback_and_reject_the_others() {
		// 1. start a pipeline with one HTTP_ASYNC item
		var posts = TEST_KIT.<Http.POST>createTestProbe();
		var pipeline = TEST_KIT.spawn(EnrichPipeline.create(PROCESS_KEY, fakeHttp(posts)));
		var scheduling = TEST_KIT.<Processor.Response>createTestProbe();
		var callbacks = TEST_KIT.<EnrichPipeline.CallbackResponse>createTestProbe();

		pipeline.tell(start(scheduler(60_000L), scheduling.ref()));

		// 2. the enricher receives a token addressed to this entity
		var post = posts.receiveMessage();
		var token = CallbackToken.decode(replyToOf(post));
		Assertions.assertEquals(PROCESS_KEY, token.processKey());

		// 3. a callback with another nonce is refused, the pipeline goes on
		pipeline.tell(new EnrichPipeline.Callback("other", CALLBACK_BODY, callbacks.ref()));
		callbacks.expectMessage(EnrichPipeline.CallbackResponse.UNKNOWN);
		scheduling.expectNoMessage(Duration.ofMillis(200));

		// 4. the awaited callback is accepted and its body reaches the result
		pipeline.tell(new EnrichPipeline.Callback(token.nonce(), CALLBACK_BODY, callbacks.ref()));
		callbacks.expectMessage(EnrichPipeline.CallbackResponse.ACCEPTED);

		var success = scheduling.expectMessageClass(Processor.Success.class);
		var payload = new String(success.payload(), StandardCharsets.UTF_8);
		Assertions.assertTrue(payload.contains("\"stub\""), payload);

		// 5. the work is done, the entity stops
		TEST_KIT.createTestProbe().expectTerminated(pipeline);
	}

	@Test
	void should_deliver_a_callback_arriving_before_the_http_ok() {
		// 1. an HTTP actor that holds the 200: the test decides when it arrives
		var posts = TEST_KIT.<Http.POST>createTestProbe();
		Supplier<Behavior<Http.Command>> silentHttp = () -> Behaviors
			.receive(Http.Command.class)
			.onMessage(Http.POST.class, post -> {
				posts.ref().tell(post);
				return Behaviors.same();
			})
			.build();
		var pipeline = TEST_KIT.spawn(EnrichPipeline.create(PROCESS_KEY, silentHttp));
		var scheduling = TEST_KIT.<Processor.Response>createTestProbe();
		var callbacks = TEST_KIT.<EnrichPipeline.CallbackResponse>createTestProbe();

		pipeline.tell(start(scheduler(3_000L), scheduling.ref()));
		var post = posts.receiveMessage();
		var token = CallbackToken.decode(replyToOf(post));

		// 2. the enricher calls back before its 200 reaches the datasource
		pipeline.tell(new EnrichPipeline.Callback(token.nonce(), CALLBACK_BODY, callbacks.ref()));
		callbacks.expectMessage(EnrichPipeline.CallbackResponse.ACCEPTED);
		post.replyTo().tell(new Http.OK(new byte[0]));

		// 3. the step completes with the callback body
		var success = scheduling.expectMessageClass(
			Processor.Success.class, Duration.ofSeconds(6));
		var payload = new String(success.payload(), StandardCharsets.UTF_8);
		Assertions.assertTrue(payload.contains("\"stub\""), payload);
	}

	@Test
	void should_fail_when_the_callback_never_comes() {
		// a short requestTimeout and an enricher that never calls back
		var posts = TEST_KIT.<Http.POST>createTestProbe();
		var pipeline = TEST_KIT.spawn(EnrichPipeline.create(PROCESS_KEY, fakeHttp(posts)));
		var scheduling = TEST_KIT.<Processor.Response>createTestProbe();

		pipeline.tell(start(scheduler(300L), scheduling.ref()));
		posts.receiveMessage();

		// behaviorOnError FAIL: the pipeline fails and stops
		scheduling.expectMessageClass(Processor.Failure.class, Duration.ofSeconds(5));
		TEST_KIT.createTestProbe().expectTerminated(pipeline);
	}

	@Test
	void should_fail_fast_a_start_redelivered_while_in_progress() {
		// 1. a pipeline waiting for its callback
		var posts = TEST_KIT.<Http.POST>createTestProbe();
		var pipeline = TEST_KIT.spawn(EnrichPipeline.create(PROCESS_KEY, fakeHttp(posts)));
		var scheduling = TEST_KIT.<Processor.Response>createTestProbe();
		var callbacks = TEST_KIT.<EnrichPipeline.CallbackResponse>createTestProbe();

		pipeline.tell(start(scheduler(60_000L), scheduling.ref()));
		var token = CallbackToken.decode(
			replyToOf(posts.receiveMessage()));

		// 2. the same message is redelivered to the busy entity
		var redelivery = TEST_KIT.<Processor.Response>createTestProbe();
		pipeline.tell(start(scheduler(60_000L), redelivery.ref()));

		var failure = redelivery.expectMessageClass(Processor.Failure.class);
		Assertions.assertTrue(
			failure.exception().getMessage().contains("previous delivery"),
			failure.exception().getMessage());

		// 3. the first delivery is unaffected and completes
		pipeline.tell(new EnrichPipeline.Callback(token.nonce(), CALLBACK_BODY, callbacks.ref()));
		callbacks.expectMessage(EnrichPipeline.CallbackResponse.ACCEPTED);
		scheduling.expectMessageClass(Processor.Success.class);
	}

	// on this line the enricher request travels as json bytes: the token
	// is the replyTo field of that json
	private static String replyToOf(Http.POST post) {
		return new JsonObject(new String(post.body(), StandardCharsets.UTF_8))
			.getString("replyTo");
	}

	// the fake HTTP actor: hands every POST to the probe and answers 200
	private static Supplier<Behavior<Http.Command>> fakeHttp(TestProbe<Http.POST> posts) {
		return () -> Behaviors
			.receive(Http.Command.class)
			.onMessage(Http.POST.class, post -> {
				posts.ref().tell(post);
				post.replyTo().tell(new Http.OK(new byte[0]));
				return Behaviors.same();
			})
			.build();
	}

	private static SchedulerDTO scheduler(long requestTimeout) {
		var enrichItem = new EnrichItemDTO();
		enrichItem.setId(7L);
		enrichItem.setName("async-stub");
		enrichItem.setType(EnrichItem.EnrichItemType.HTTP_ASYNC);
		enrichItem.setServiceName("http://async-enricher-stub:5000/start-task/");
		enrichItem.setRequestTimeout(requestTimeout);
		enrichItem.setBehaviorOnError(EnrichItem.BehaviorOnError.FAIL);

		var scheduler = new SchedulerDTO();
		scheduler.setId(1L);
		scheduler.setTenantId("tenant");
		scheduler.setScheduleId("schedule-1");
		scheduler.setDatasourceId(1L);
		scheduler.setNewDataIndexName("tenant-data-1");
		scheduler.setEnrichItems(new LinkedHashSet<>(List.of(enrichItem)));

		return scheduler;
	}

	private static Processor.Start start(
		SchedulerDTO scheduler, ActorRef<Processor.Response> replyTo) {

		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DOCUMENT)
				.tenantId("tenant")
				.datasourceId(1L)
				.contentId("content-1")
				.documentTypes(new String[] {"web"})
				.build())
			.getBytes();

		var heldMessage = new HeldMessage(PROCESS_KEY, 1, 0L, "content-1");

		return new Processor.Start(payload, scheduler, heldMessage, replyTo);
	}

}
