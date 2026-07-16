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

/**
 * Deterministic addressing of binaries on the object storage, computed only
 * from metadata that both the writer (ingestion) and the readers (datasource,
 * enrichers) already own — no database indirection.
 *
 * <p>The tenant is the bucket, so it is not repeated in the key: one bucket
 * per tenant (default template {@value #DEFAULT_BUCKET_TEMPLATE}) isolates
 * tenants and lets the key be listable by datasource and by content.
 */
public final class BinaryKeys {

	/**
	 * Default bucket-name template. The {@value #TENANT_PLACEHOLDER}
	 * placeholder is replaced with the tenant id; the {@code openk9-} prefix
	 * avoids collisions with unrelated buckets.
	 */
	public static final String DEFAULT_BUCKET_TEMPLATE = "openk9-{tenant}";

	private static final String TENANT_PLACEHOLDER = "{tenant}";

	private BinaryKeys() {}

	/**
	 * Resolves the bucket name of a tenant from a template.
	 *
	 * @param tenantId the tenant (schema) name; must satisfy S3 bucket naming
	 * @param bucketTemplate a template containing {@value #TENANT_PLACEHOLDER}
	 * @return the bucket name with the placeholder replaced by the tenant id
	 */
	public static String bucket(String tenantId, String bucketTemplate) {
		return bucketTemplate.replace(TENANT_PLACEHOLDER, tenantId);
	}

	/**
	 * Builds the object key of a single binary, without the tenant segment
	 * (the tenant is already the bucket).
	 *
	 * @param datasourceId the datasource owning the content
	 * @param contentId the content the binary belongs to
	 * @param fileId the per-binary discriminator, unique inside the content
	 * @return the key {@code {datasourceId}/{contentId}/{fileId}}, listable by
	 * prefix at every level
	 */
	public static String key(long datasourceId, String contentId, String fileId) {
		return datasourceId + "/" + contentId + "/" + fileId;
	}

	/**
	 * Builds the key prefix that lists every binary of a datasource, used to
	 * drop a datasource working copy in one sweep.
	 *
	 * @param datasourceId the datasource owning the content
	 * @return the prefix {@code {datasourceId}/}, matching every key of the
	 * datasource
	 */
	public static String datasourcePrefix(long datasourceId) {
		return datasourceId + "/";
	}

}
