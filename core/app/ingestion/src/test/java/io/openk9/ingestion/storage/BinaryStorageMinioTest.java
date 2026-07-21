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

import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Integration test of the binary write path ({@link BinaryStorageService})
 * against a real MinIO instance provided by Quarkus DevServices, verifying that
 * a stored binary lands under its deterministic key and reads back unchanged,
 * not only with mocks.
 */
@QuarkusTest
class BinaryStorageMinioTest {

	@Inject
	MinioClient minioClient;
	@Inject
	BinaryStorageService binaryStorageService;

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

}
