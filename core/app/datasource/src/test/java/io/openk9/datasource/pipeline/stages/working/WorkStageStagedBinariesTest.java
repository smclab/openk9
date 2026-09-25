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

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.WriterException;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.Json;
import org.apache.pekko.actor.testkit.typed.javadsl.BehaviorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestInbox;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Checks, against a real MinIO provided by Quarkus DevServices, that the
 * binaries staged for a content are dropped as soon as the content is written,
 * and kept when the write fails.
 */
@QuarkusTest
class WorkStageStagedBinariesTest {

	private static final long DATASOURCE_ID = 11L;

	private static final byte[] PAYLOAD = "staged-bytes".getBytes();

	@Inject
	MinioClient minioClient;

	@Test
	void should_delete_the_staged_binaries_once_the_content_is_written()
		throws Exception {

		String tenantId = "workstagewritten";
		stage(tenantId, BinaryKeys.key(DATASOURCE_ID, "content-1", "f1"));
		stage(tenantId, BinaryKeys.key(DATASOURCE_ID, "content-2", "f1"));

		var run = start(tenantId, "content-1");
		run.writerAdapter().tell(new Writer.Success(run.heldMessage()));
		run.workStage().runOne();

		Assertions.assertInstanceOf(
			WorkStage.Done.class, run.replyTo().receiveMessage());

		awaitGone(tenantId, BinaryKeys.contentPrefix(DATASOURCE_ID, "content-1"));
		Assertions.assertEquals(
			List.of(BinaryKeys.key(DATASOURCE_ID, "content-2", "f1")),
			objectNames(
				tenantId, BinaryKeys.contentPrefix(DATASOURCE_ID, "content-2")));
	}

	@Test
	void should_keep_the_staged_binaries_when_the_write_fails() throws Exception {
		String tenantId = "workstagefailed";
		stage(tenantId, BinaryKeys.key(DATASOURCE_ID, "content-1", "f1"));

		var run = start(tenantId, "content-1");
		run.writerAdapter().tell(new Writer.Failure(
			new WriterException(new IllegalStateException("bulk failed")),
			run.heldMessage()));
		run.workStage().runOne();

		Assertions.assertInstanceOf(
			WorkStage.Failed.class, run.replyTo().receiveMessage());

		Thread.sleep(500);
		Assertions.assertEquals(
			List.of(BinaryKeys.key(DATASOURCE_ID, "content-1", "f1")),
			objectNames(
				tenantId, BinaryKeys.contentPrefix(DATASOURCE_ID, "content-1")));
	}

	private static Run start(String tenantId, String contentId) {
		var replyTo = TestInbox.<WorkStage.Response>create();
		var requester = TestInbox.<Scheduling.Response>create();
		var writerAdapter = new AtomicReference<ActorRef<Writer.Response>>();

		var workStage = BehaviorTestKit.create(WorkStage.create(
			ShardingKey.fromStrings(tenantId, "schedule-1"),
			replyTo.getRef(),
			new WorkStage.Configurations(
				new LinkedList<>(),
				(scheduler, adapter) -> {
					writerAdapter.set(adapter);
					return Behaviors.ignore();
				})
		));

		// a document without documentTypes goes straight to the writer
		var payload = Json.encodeToBuffer(DataPayload.builder()
				.type(PayloadType.DOCUMENT)
				.contentId(contentId)
				.build())
			.getBytes();

		var scheduler = new SchedulerDTO();
		scheduler.setDatasourceId(DATASOURCE_ID);

		workStage.run(new WorkStage.StartWorker(
			scheduler, payload, requester.getRef()));

		var working = (WorkStage.Working) replyTo.receiveMessage();

		return new Run(
			workStage, replyTo, writerAdapter.get(), working.heldMessage());
	}

	private void awaitGone(String tenantId, String prefix) throws Exception {
		var deadline = Instant.now().plus(Duration.ofSeconds(10));

		while (!objectNames(tenantId, prefix).isEmpty()) {
			if (Instant.now().isAfter(deadline)) {
				Assertions.fail("staged binaries still under " + prefix);
			}

			Thread.sleep(100);
		}
	}

	private void stage(String tenantId, String key) throws Exception {
		String bucket = bucketOf(tenantId);

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

	private List<String> objectNames(String tenantId, String prefix)
		throws Exception {

		List<String> names = new ArrayList<>();

		Iterable<Result<Item>> items = minioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(bucketOf(tenantId))
				.prefix(prefix)
				.recursive(true)
				.build());

		for (Result<Item> item : items) {
			names.add(item.get().objectName());
		}

		return names;
	}

	private static String bucketOf(String tenantId) {
		return BinaryKeys.bucket(tenantId, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);
	}

	private record Run(
		BehaviorTestKit<WorkStage.Command> workStage,
		TestInbox<WorkStage.Response> replyTo,
		ActorRef<Writer.Response> writerAdapter,
		HeldMessage heldMessage
	) {}

}
