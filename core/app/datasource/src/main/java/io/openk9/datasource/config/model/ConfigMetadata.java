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

package io.openk9.datasource.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package-level provenance and the {@code TenantBinding} pointers needed to
 * make the imported configuration operational. {@code TenantBinding} is not a
 * {@link ConfigEntity} (it has no DTO and its {@code virtualHost} is an
 * environment-specific routing identity): on import these handles drive the
 * rebind of the destination tenant, while {@code virtualHost} is preserved
 * (never transferred on import).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMetadata {

	private String exportedAt;
	private String sourceVirtualHost;
	private String defaultBucketRef;
	private String enabledEmbeddingModelRef;
	private String enabledLargeLanguageModelRef;

}
