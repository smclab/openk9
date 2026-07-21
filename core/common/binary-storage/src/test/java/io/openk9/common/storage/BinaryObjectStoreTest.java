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

package io.openk9.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

class BinaryObjectStoreTest {

	private static BinaryObjectStore service(String bucketTemplate) {
		// region pinned so the SigV4 signature is computed locally, with no
		// network round-trip to discover the bucket region.
		MinioClient client = MinioClient
			.builder()
			.endpoint("http://localhost:9000")
			.region("us-east-1")
			.credentials("test-access", "test-secret")
			.build();

		return new BinaryObjectStore(client, bucketTemplate, 120);
	}

	@Test
	void presignedUrlTargetsTenantBucketAndKey() {
		String url = service(BinaryKeys.DEFAULT_BUCKET_TEMPLATE)
			.presignGet("acme", 42L, "content-1", "file-1");

		assertTrue(
			url.contains("/openk9-acme/42/content-1/file-1"),
			() -> "unexpected object path in " + url);
		assertTrue(
			url.contains("X-Amz-Signature="),
			() -> "not a signed URL: " + url);
		assertTrue(
			url.contains("X-Amz-Expires=120"),
			() -> "unexpected expiry in " + url);
	}

	@Test
	void bucketForResolvesConfiguredTemplate() {
		assertEquals(
			"openk9-acme",
			service("openk9-{tenant}").bucketFor("acme"));
	}

}
