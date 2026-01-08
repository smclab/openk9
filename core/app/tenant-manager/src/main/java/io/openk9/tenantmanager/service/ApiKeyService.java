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

import java.time.OffsetDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.ApiKeys;
import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.tenantmanager.model.ApiKey;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ApiKeyService {

	/**
	 * Generates a new apiKey for specific tenant and api-group.
	 *
	 * @param request an object containing the new API Key requirements.
	 * @return a response object containing the text plain API Key.
	 *         Text plain API Key is visible just once.
	 */
	public Uni<CreateApiKeyResponse> create(CreateApiKeyRequest request) {

		var id = ID_GENERATOR.nextId();
		// TODO: prefix could be made in conjunction with the API Group.
		var prefix = API_KEY_PREFIX;
		var apiKey = ApiKeys.generateApiKeyWithChecksum(prefix);
		var suffix = ApiKeys.getChecksum(apiKey);
		var tenantId = request.tenantId();
		var name = request.name();
		var apiGroup = request.apiGroup();
		var creationDate = OffsetDateTime.now();
		var expirationDate = request.expirationDate() != null
			? request.expirationDate()
			: creationDate.plusWeeks(1);

		return pool.withTransaction(conn -> conn
			.preparedQuery(INSERT_SQL)
			.execute(Tuple.from(new Object[]{
				id, tenantId,
				name, ApiKey.Status.ACTIVE,
				prefix, suffix, apiGroup,
				creationDate, expirationDate
			}))
			.onFailure()
			.invoke(log::error)
			.map(SqlResult::rowCount)
			.map(rowCount -> new CreateApiKeyResponse(apiKey))
		);
	}

	/**
	 * Revoke the API Key updating the status.
	 *
	 * @param apiKeyId the identifier of this api key.
	 *
	 * @return nothing.
	 */
	public Uni<Void> revoke(String apiKeyId) {

		var id = Long.valueOf(apiKeyId);
		return pool.withTransaction(conn -> conn
				.preparedQuery("UPDATE api_key SET status = $1 WHERE id = $2")
				.execute(Tuple.of(ApiKey.Status.REVOKED, id))
			).replaceWithVoid();
	}

	@Inject
	Pool pool;

	private static final String INSERT_SQL = """
			INSERT INTO api_key (
				id, tenant_id,
				name, status,
				prefix, suffix, api_group,
				create_date, expiration_date
			)
			VALUES (
				$1, $2,
				$3, $4,
				$5, $6, $7,
				$8, $9)
		""";

	private static final String API_KEY_PREFIX = "ok9";

	private static final CompactSnowflakeIdGenerator ID_GENERATOR =
		new CompactSnowflakeIdGenerator();

	private static final Logger log = Logger.getLogger(ApiKeyService.class);

}
