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

import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
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

}
