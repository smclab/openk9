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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Issues short-lived pre-signed GET URLs for binaries on the object storage.
 * The signature is a local SigV4 computation (no call to the storage), so a
 * fresh URL can be minted on every enricher invocation and retry without I/O.
 *
 * <p>The URL is a bearer credential: it must have a short expiry and must
 * never be logged. Being standard SigV4, it works against any S3-compatible
 * endpoint; the only deployment constraint is signing against an endpoint
 * reachable by whoever downloads.
 */
@ApplicationScoped
public class PresignedUrlService {

	private final MinioClient minioClient;
	private final String bucketTemplate;
	private final int expirySeconds;

	@Inject
	public PresignedUrlService(
		MinioClient minioClient,
		@ConfigProperty(
			name = "openk9.binaries.bucket-template",
			defaultValue = BinaryKeys.DEFAULT_BUCKET_TEMPLATE) String bucketTemplate,
		@ConfigProperty(
			name = "openk9.binaries.presign-expiry-seconds",
			defaultValue = "300") int expirySeconds) {

		this.minioClient = minioClient;
		this.bucketTemplate = bucketTemplate;
		this.expirySeconds = expirySeconds;
	}

	/**
	 * Resolves the bucket name of a tenant from the configured template.
	 *
	 * @param tenantId the tenant (schema) name
	 * @return the tenant bucket name
	 */
	public String bucketFor(String tenantId) {
		return BinaryKeys.bucket(tenantId, bucketTemplate);
	}

	/**
	 * Mints a pre-signed GET URL for a single binary.
	 *
	 * @param tenantId the tenant owning the bucket
	 * @param datasourceId the datasource owning the content
	 * @param contentId the content the binary belongs to
	 * @param fileId the per-binary discriminator
	 * @return a short-lived pre-signed GET URL (bearer credential)
	 */
	public String presignGet(
		String tenantId, long datasourceId, String contentId, String fileId) {

		String bucket = BinaryKeys.bucket(tenantId, bucketTemplate);
		String key = BinaryKeys.key(datasourceId, contentId, fileId);

		try {
			return minioClient.getPresignedObjectUrl(
				GetPresignedObjectUrlArgs
					.builder()
					.method(Method.GET)
					.bucket(bucket)
					.object(key)
					.expiry(expirySeconds, TimeUnit.SECONDS)
					.build());
		}
		catch (Exception e) {
			throw new IllegalStateException(
				"Cannot pre-sign GET for object " + key, e);
		}
	}

	/**
	 * Deletes the whole working copy of a datasource from the tenant bucket.
	 *
	 * <p>The binaries staged during processing live under the datasource
	 * prefix and are only needed while the enrichers read them; a re-index
	 * re-fetches them from the connector. It is a no-op when the bucket does
	 * not exist or holds no object under the prefix.
	 *
	 * @param tenantId the tenant owning the bucket
	 * @param datasourceId the datasource whose staged binaries are removed
	 * @throws IllegalStateException if the storage cannot be listed or an
	 * object cannot be deleted
	 */
	public void deleteByDatasource(String tenantId, long datasourceId) {

		String bucket = BinaryKeys.bucket(tenantId, bucketTemplate);
		String prefix = BinaryKeys.datasourcePrefix(datasourceId);

		try {
			boolean exists = minioClient.bucketExists(
				BucketExistsArgs.builder().bucket(bucket).build());

			if (!exists) {
				return;
			}

			List<DeleteObject> objects = new ArrayList<>();

			Iterable<Result<Item>> items = minioClient.listObjects(
				ListObjectsArgs.builder()
					.bucket(bucket)
					.prefix(prefix)
					.recursive(true)
					.build());

			for (Result<Item> item : items) {
				objects.add(new DeleteObject(item.get().objectName()));
			}

			if (objects.isEmpty()) {
				return;
			}

			// removeObjects is lazy: iterating the results forces the deletion
			// and surfaces any object the storage refused to delete.
			Iterable<Result<DeleteError>> results = minioClient.removeObjects(
				RemoveObjectsArgs.builder()
					.bucket(bucket)
					.objects(objects)
					.build());

			List<String> failed = new ArrayList<>();

			for (Result<DeleteError> result : results) {
				failed.add(result.get().objectName());
			}

			if (!failed.isEmpty()) {
				throw new IllegalStateException(
					"Cannot delete objects " + failed + " in bucket " + bucket);
			}
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalStateException(
				"Cannot delete binaries under " + prefix
				+ " in bucket " + bucket, e);
		}
	}

}
