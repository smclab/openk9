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

package io.openk9.datasource.pipeline.consumer;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.service.SeaweedFsTestResource;
import io.openk9.datasource.processor.payload.BinaryPayload;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.ResourcesPayload;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.pekko.actor.typed.Props;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks, against a real RabbitMQ and a real S3 storage (SeaweedFS), that a
 * document drops its staged binaries only once its message is acked, and
 * keeps them when the message is nacked or its ask times out, since the retry
 * queue brings it back and the enrichers must read them again.
 */
@QuarkusTest
@QuarkusTestResource(SeaweedFsTestResource.class)
class MainConsumerStagedBinariesTest {

	// SeaweedFS allocates volumes per bucket and the container shared with the
	// other S3 tests has few of them: one bucket, one content per test
	private static final String TENANT_ID = "consumerstaged";

	private static final long DATASOURCE_ID = 11L;

	private static final byte[] PAYLOAD = "staged-bytes".getBytes();

	@Inject
	RabbitMQClient rabbitMQClient;

	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	MinioClient minioClient;

	private Connection connection;
	private Channel channel;
	private String queue;

	@BeforeEach
	void setup() throws Exception {
		this.connection = rabbitMQClient.connect();
		this.channel = connection.createChannel();
		this.queue = channel.queueDeclare().getQueue();
	}

	@Test
	void should_delete_the_staged_binaries_once_the_message_is_acked()
		throws Exception {

		// stage the binaries of two contents
		stage(BinaryKeys.key(DATASOURCE_ID, "acked-1", "f1"));
		stage(BinaryKeys.key(DATASOURCE_ID, "acked-2", "f1"));

		// the scheduling is done with the first one
		consumer().settle(
			deliver(), document("acked-1", true),
			Scheduling.Success.INSTANCE, null);

		// only its binaries are gone
		awaitGone(BinaryKeys.contentPrefix(DATASOURCE_ID, "acked-1"));
		Assertions.assertEquals(
			List.of(BinaryKeys.key(DATASOURCE_ID, "acked-2", "f1")),
			objectNames(BinaryKeys.contentPrefix(DATASOURCE_ID, "acked-2")));
	}

	@Test
	void should_keep_the_staged_binaries_when_the_message_is_nacked()
		throws Exception {

		// stage the binary of a content
		stage(BinaryKeys.key(DATASOURCE_ID, "nacked-1", "f1"));

		// the scheduling failed the content
		consumer().settle(
			deliver(), document("nacked-1", true),
			new Scheduling.Failure("work stage failed"), null);

		// the retry finds the binary again
		assertKept("nacked-1");
	}

	@Test
	void should_keep_the_staged_binaries_when_the_ask_times_out()
		throws Exception {

		// stage the binary of a content
		stage(BinaryKeys.key(DATASOURCE_ID, "timeout-1", "f1"));

		// the ask expired while the content is still being worked
		consumer().settle(
			deliver(), document("timeout-1", true),
			null, new TimeoutException("ask timed out"));

		// the redelivered message finds the binary again
		assertKept("timeout-1");
	}

	@Test
	void should_leave_the_storage_alone_for_a_document_without_binaries()
		throws Exception {

		// stage a binary under a content whose message carries none
		stage(BinaryKeys.key(DATASOURCE_ID, "nobinaries-1", "f1"));

		// the scheduling is done with it
		consumer().settle(
			deliver(), document("nobinaries-1", false),
			Scheduling.Success.INSTANCE, null);

		// no deletion was requested for it
		assertKept("nobinaries-1");
	}

	@AfterEach
	void tearDown() throws Exception {
		if (connection != null) {
			connection.close();
		}
	}

	private MainConsumer consumer() {
		return new MainConsumer(
			channel,
			spawnContext(),
			new QueueManager.QueueBind(TENANT_ID + "#" + UUID.randomUUID()),
			ingestionPayloadMapper);
	}

	/**
	 * Publishes a message and takes it unacked, so that settling it acks or
	 * nacks a real delivery tag.
	 */
	private Envelope deliver() throws Exception {
		channel.basicPublish(
			"", queue, MessageProperties.PERSISTENT_BASIC, new byte[0]);

		var deadline = Instant.now().plus(Duration.ofSeconds(10));

		while (Instant.now().isBefore(deadline)) {
			var response = channel.basicGet(queue, false);

			if (response != null) {
				return response.getEnvelope();
			}

			Thread.sleep(50);
		}

		return Assertions.fail("the published message was not delivered");
	}

	private static DataPayload document(String contentId, boolean withBinary) {
		var resources = withBinary
			? ResourcesPayload.of(List.of(BinaryPayload.builder().id("f1").build()))
			: ResourcesPayload.of(List.of());

		return DataPayload.builder()
			.type(PayloadType.DOCUMENT)
			.datasourceId(DATASOURCE_ID)
			.contentId(contentId)
			.resources(resources)
			.build();
	}

	private void assertKept(String contentId) throws Exception {
		Thread.sleep(500);

		Assertions.assertEquals(
			List.of(BinaryKeys.key(DATASOURCE_ID, contentId, "f1")),
			objectNames(BinaryKeys.contentPrefix(DATASOURCE_ID, contentId)));
	}

	private void awaitGone(String prefix) throws Exception {
		var deadline = Instant.now().plus(Duration.ofSeconds(10));

		while (!objectNames(prefix).isEmpty()) {
			if (Instant.now().isAfter(deadline)) {
				Assertions.fail("staged binaries still under " + prefix);
			}

			Thread.sleep(100);
		}
	}

	private void stage(String key) throws Exception {
		String bucket = bucketOf();

		boolean exists = minioClient.bucketExists(
			BucketExistsArgs.builder().bucket(bucket).build());

		if (!exists) {
			minioClient.makeBucket(
				MakeBucketArgs.builder().bucket(bucket).build());
		}

		minioClient.putObject(
			PutObjectArgs.builder()
				.bucket(bucket)
				.object(key)
				.stream(new ByteArrayInputStream(PAYLOAD), PAYLOAD.length, -1)
				.build());
	}

	private List<String> objectNames(String prefix)
		throws Exception {

		List<String> names = new ArrayList<>();

		Iterable<Result<Item>> items = minioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(bucketOf())
				.prefix(prefix)
				.recursive(true)
				.build());

		for (Result<Item> item : items) {
			names.add(item.get().objectName());
		}

		return names;
	}

	private static String bucketOf() {
		return BinaryKeys.bucket(TENANT_ID, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);
	}

	/**
	 * The consumer needs an ActorContext, which only exists inside an actor:
	 * a throwaway actor on the application ActorSystem provides one.
	 */
	private ActorContext<Void> spawnContext() {
		var contextFuture = new CompletableFuture<ActorContext<Void>>();

		actorSystemProvider.getActorSystem().systemActorOf(
			Behaviors.<Void>setup(ctx -> {
				contextFuture.complete(ctx);

				return Behaviors.empty();
			}),
			"staged-binaries-test-" + UUID.randomUUID(),
			Props.empty()
		);

		return contextFuture
			.orTimeout(10, TimeUnit.SECONDS)
			.join();
	}

}
