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

package io.openk9.ingestion.storage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;
import io.openk9.common.storage.PresignedUrlService;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of the binary storage against a real MinIO instance provided
 * by Quarkus DevServices, covering the write path ({@link BinaryStorageService})
 * and the datasource read/cleanup path ({@link PresignedUrlService}) end to end,
 * not only with mocks.
 */
@QuarkusTest
class BinaryStorageMinioTest {

	@Inject
	MinioClient minioClient;
	@Inject
	BinaryStorageService binaryStorageService;
	@Inject
	PresignedUrlService presignedUrlService;

	@Test
	void should_store_a_binary_under_its_key() throws Exception {
		// store a binary under its deterministic key
		String tenantId = "storetenant";
		String bucket =
			BinaryKeys.bucket(tenantId, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);
		String key = BinaryKeys.key(100L, "doc-1", "f1");
		byte[] data = "hello minio".getBytes();

		binaryStorageService
			.store(tenantId, key, data, "text/plain")
			.await().indefinitely();

		// the object is readable back from the tenant bucket with the same bytes
		try (GetObjectResponse response = minioClient.getObject(
			GetObjectArgs.builder().bucket(bucket).object(key).build())) {

			assertArrayEquals(data, response.readAllBytes());
		}
	}

	@Test
	void should_serve_a_stored_binary_via_its_presigned_url() throws Exception {
		// store a binary
		String tenantId = "presigntenant";
		String contentId = "doc-1";
		String fileId = "f1";
		byte[] data = "presigned bytes".getBytes();

		binaryStorageService
			.store(tenantId, BinaryKeys.key(7L, contentId, fileId),
				data, "text/plain")
			.await().indefinitely();

		// mint a pre-signed GET URL and fetch it over HTTP
		String url = presignedUrlService.presignGet(tenantId, 7L, contentId, fileId);

		HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
			HttpRequest.newBuilder(URI.create(url)).GET().build(),
			HttpResponse.BodyHandlers.ofByteArray());

		// the URL serves exactly the stored bytes
		assertEquals(200, response.statusCode());
		assertArrayEquals(data, response.body());
	}

	@Test
	void should_delete_every_staged_object_of_a_datasource() throws Exception {
		// 1. stage two binaries of datasource 42 and one of datasource 99
		String tenantId = "deletetenant";
		String bucket =
			BinaryKeys.bucket(tenantId, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);

		store(tenantId, BinaryKeys.key(42L, "c1", "f1"));
		store(tenantId, BinaryKeys.key(42L, "c2", "f2"));
		store(tenantId, BinaryKeys.key(99L, "c1", "f1"));

		// 2. delete the working copy of datasource 42
		presignedUrlService.deleteByDatasource(tenantId, 42L);

		// datasource 42 is swept clean, datasource 99 is untouched
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
		String bucket =
			BinaryKeys.bucket(tenantId, BinaryKeys.DEFAULT_BUCKET_TEMPLATE);

		for (int i = 0; i < 50; i++) {
			store(tenantId, BinaryKeys.key(5L, "c" + i, "f" + i));
		}

		// delete the whole working copy in one sweep
		presignedUrlService.deleteByDatasource(tenantId, 5L);

		// every staged object is gone
		assertTrue(
			objectNames(bucket, BinaryKeys.datasourcePrefix(5L)).isEmpty());
	}

	@Test
	void should_be_a_noop_when_deleting_an_already_clean_datasource()
		throws Exception {

		// stage and delete a datasource working copy
		String tenantId = "idemtenant";
		store(tenantId, BinaryKeys.key(3L, "c1", "f1"));
		presignedUrlService.deleteByDatasource(tenantId, 3L);

		// deleting again on the now-empty prefix is a no-op, not a failure
		presignedUrlService.deleteByDatasource(tenantId, 3L);
	}

	private void store(String tenantId, String key) {
		binaryStorageService
			.store(tenantId, key, "some-bytes".getBytes(), "text/plain")
			.await().indefinitely();
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
