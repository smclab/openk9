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

package io.openk9.datasource.service;

import java.util.List;

import io.smallrye.mutiny.Uni;

public interface TenantRegistry {

	/**
	 * Resolves a {@code virtualHost} to its {@code tenantId} (schema name).
	 * <p>
	 * Issues a gRPC call to {@code tenant-manager}; not cached at this layer.
	 * <p>
	 * Two callers are expected:
	 * <ul>
	 *     <li>The API Gateway's {@code TenantIdResolverFilter}, which resolves once per
	 *     incoming HTTP request and propagates the result downstream as the
	 *     {@code X-TENANT-ID} header.</li>
	 *     <li>Server-side gRPC handlers as a legacy fallback, used only when an incoming
	 *     request still carries {@code virtualHost} but not {@code tenantId} (pre-#1849
	 *     clients). New code should receive {@code tenantId} from its caller instead.</li>
	 * </ul>
	 */
	Uni<String> getTenantId(String virtualHost);

	Uni<List<String>> getTenantIdList();

}
