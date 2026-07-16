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

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Persists ingested binaries on the tenant bucket of the object storage, under
 * their deterministic key. The bytes are stored once and only a lightweight
 * reference to them travels on the ingestion bus, keeping large payloads off
 * the message broker while enrichers read the binaries back from storage.
 */
@ApplicationScoped
public class BinaryStorageService {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@ConfigProperty(
		name = "openk9.binaries.bucket-template",
		defaultValue = BinaryKeys.DEFAULT_BUCKET_TEMPLATE)
	String bucketTemplate;

	@Inject
	MinioClient minioClient;
	@Inject
	Logger logger;

	private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();

	/**
	 * Stores a binary in the tenant bucket under the given deterministic key.
	 *
	 * @param tenantId the tenant owning the bucket
	 * @param key the object key, see {@link BinaryKeys#key}
	 * @param data the binary content
	 * @param contentType the declared content type, may be null
	 * @return a Uni completing when the object is written
	 */
	public Uni<Void> store(
		String tenantId, String key, byte[] data, String contentType) {

		String bucket = BinaryKeys.bucket(tenantId, bucketTemplate);

		return Uni
			.createFrom()
			.<Void>item(() -> {
				try {
					ensureBucketExists(bucket);

					minioClient.putObject(
						PutObjectArgs
							.builder()
							.bucket(bucket)
							.object(key)
							.stream(new ByteArrayInputStream(data), data.length, -1)
							.contentType(contentType != null
								? contentType
								: DEFAULT_CONTENT_TYPE)
							.build());

					logger.infof("Stored binary %s in bucket %s", key, bucket);

					return null;
				}
				catch (Exception e) {
					throw new IllegalStateException(
						"Cannot store binary " + key + " in bucket " + bucket, e);
				}
			})
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}

	private void ensureBucketExists(String bucket) throws Exception {
		if (ensuredBuckets.contains(bucket)) {
			return;
		}

		boolean exists = minioClient.bucketExists(
			BucketExistsArgs.builder().bucket(bucket).build());

		if (!exists) {
			logger.infof("Bucket does not exist, creating: %s", bucket);
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
		}

		ensuredBuckets.add(bucket);
	}

}
