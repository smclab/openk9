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

package io.openk9.tenantmanager.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.ApiKeys;
import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.tenantmanager.model.ApiKeyStatus;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;

@ApplicationScoped
public class ApiKeyService {

	/**
	 * Generates a new apiKey for specific tenant and routes.
	 *
	 * @param request
	 * @return
	 */
	public Uni<CreateApiKeyResponse> create(
		CreateApiKeyRequest request) {

		var id = idGenerator.nextId();
		var prefix = "ok9";
		var apiKey = ApiKeys.generateApiKeyWithChecksum(prefix);
		var suffix = ApiKeys.getChecksum(apiKey);
		var tenantId = request.tenantId();
		var name = request.name();
		var routes = request.routes();
		var expirationDate = request.expirationDate();

		return pool.withTransaction(conn -> conn.preparedQuery("""
				INSERT INTO api_key (
					id, tenant_id,
					name, status, prefix, suffix,
					create_date, expiration_date
				)
				VALUES (
					$1, $2,
					$3, $4, $5, $6,
					$7, $8)
			""")
			.execute(Tuple.from(new Object[]{
				id,
				tenantId,
				name,
				ApiKeyStatus.ACTIVE,
				prefix, suffix
			}))
			.map(rows -> new CreateApiKeyResponse(apiKey))
		);
	}

	@Inject
	Pool pool;

	private static final CompactSnowflakeIdGenerator idGenerator =
		new CompactSnowflakeIdGenerator();

}
