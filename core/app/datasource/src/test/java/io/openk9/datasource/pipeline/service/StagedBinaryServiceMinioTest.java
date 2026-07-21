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

package io.openk9.datasource.pipeline.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of {@link StagedBinaryService}, the entry point the indexing
 * pipeline uses to reach the binaries staged on the object storage, against a
 * real MinIO instance provided by Quarkus DevServices. It exercises the real
 * datasource paths end to end, not only with mocks: minting the pre-signed GET
 * URL the enrichers read a binary with, and dropping the whole working copy of
 * a datasource when a scheduling closes (dispatched over the event bus).
 */
@QuarkusTest
class StagedBinaryServiceMinioTest {

	private static final byte[] PAYLOAD = "staged-bytes".getBytes();

	@Inject
	MinioClient minioClient;

	@Test
	void should_serve_a_staged_binary_via_its_presigned_url() throws Exception {
		// stage a binary under its deterministic key
		String tenantId = "presigntenant";
		String contentId = "doc-1";
		String fileId = "f1";
		stage(tenantId, BinaryKeys.key(7L, contentId, fileId));

		// mint a pre-signed GET URL through the pipeline entry point
		String url =
			StagedBinaryService.presignGet(tenantId, 7L, contentId, fileId);

		// the URL serves exactly the staged bytes over HTTP
		HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
			HttpRequest.newBuilder(URI.create(url)).GET().build(),
			HttpResponse.BodyHandlers.ofByteArray());

		assertEquals(200, response.statusCode());
		assertArrayEquals(PAYLOAD, response.body());
	}

	@Test
	void should_delete_every_staged_object_of_a_datasource() throws Exception {
		// stage two binaries of datasource 42 and one of a sibling datasource 99
		String tenantId = "deletetenant";
		String bucket = bucketOf(tenantId);

		stage(tenantId, BinaryKeys.key(42L, "c1", "f1"));
		stage(tenantId, BinaryKeys.key(42L, "c2", "f2"));
		stage(tenantId, BinaryKeys.key(99L, "c1", "f1"));

		// drop the working copy of datasource 42
		delete(tenantId, 42L);

		// datasource 42 is swept clean, the sibling datasource 99 is untouched
		assertTrue(
			objectNames(bucket, BinaryKeys.datasourcePrefix(42L)).isEmpty());
		assertEquals(
			List.of(BinaryKeys.key(99L, "c1", "f1")),
			objectNames(bucket, BinaryKeys.datasourcePrefix(99L)));
	}

	@Test
	void should_delete_a_large_working_copy() throws Exception {
		// stage fifty binaries of datasource 5 to exercise batch removal
		String tenantId = "bulktenant";
		String bucket = bucketOf(tenantId);

		for (int i = 0; i < 50; i++) {
			stage(tenantId, BinaryKeys.key(5L, "c" + i, "f" + i));
		}

		// drop the whole working copy in one sweep
		delete(tenantId, 5L);

		// every staged object is gone
		assertTrue(
			objectNames(bucket, BinaryKeys.datasourcePrefix(5L)).isEmpty());
	}

	@Test
	void should_be_a_noop_when_deleting_an_already_clean_datasource()
		throws Exception {

		// stage and delete a datasource working copy
		String tenantId = "idemtenant";
		stage(tenantId, BinaryKeys.key(3L, "c1", "f1"));
		delete(tenantId, 3L);

		// deleting again on the now-empty prefix is a no-op, not a failure
		delete(tenantId, 3L);
	}

	@Test
	void should_be_a_noop_when_the_bucket_does_not_exist() throws Exception {
		// a tenant that never staged any binary has no bucket
		String tenantId = "nobuckettenant";

		// deletion is a no-op, not a failure
		delete(tenantId, 1L);
	}

	private String bucketOf(String tenantId) {
		return BinaryKeys.bucket(tenantId, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);
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

	private void delete(String tenantId, long datasourceId) throws Exception {
		StagedBinaryService
			.deleteByDatasource(tenantId, datasourceId)
			.toCompletableFuture()
			.get(10, TimeUnit.SECONDS);
	}

	private List<String> objectNames(String bucket, String prefix)
		throws Exception {

		List<String> names = new ArrayList<>();

		Iterable<Result<Item>> items = minioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(bucket)
				.prefix(prefix)
				.recursive(true)
				.build());

		for (Result<Item> item : items) {
			names.add(item.get().objectName());
		}

		return names;
	}

}
