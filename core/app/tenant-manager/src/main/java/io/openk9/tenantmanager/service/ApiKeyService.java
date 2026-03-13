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
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.ApiKeys;
import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.TenantEvent;
import io.openk9.event.tenant.TenantEventProducer;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.ApiKey;
import io.openk9.tenantmanager.service.dto.ApiKeyResponse;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
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
		var hash = ApiKeys.sha256Hex(apiKey);
		var suffix = ApiKeys.getChecksum(apiKey);
		var tenantName = request.tenantName();
		var name = request.name();
		var apiGroup = request.apiGroup();
		var creationDate = OffsetDateTime.now();
		var expirationDate = request.expirationDate() != null
			? request.expirationDate()
			: creationDate.plusWeeks(1);

		return tenantDbService.findByTenantName(tenantName)
			.map(TenantResponseDTO::id)
			.map(Long::valueOf)
			.flatMap(tenantIdentifier -> pool
				.withTransaction(conn -> conn
					.preparedQuery(INSERT_SQL)
					.execute(Tuple.from(new Object[]{
						id, tenantIdentifier,
						hash, name, ApiKey.Status.ACTIVE,
						prefix, suffix, apiGroup,
						creationDate, expirationDate
					}))
					.map(SqlResult::rowCount)
					.flatMap(rowCount -> TenantEventProducerUtils.sendEvent(
						producer,
						rowCount,
						TenantEvent.ApiKeyCreated.builder()
							.apiKeyHash(hash)
							.checksum(suffix)
							.tenantId(tenantName)
							.apiGroup(apiGroup)
							.expirationDate(expirationDate)
							.build()
					))
					.map(rows -> new CreateApiKeyResponse(String.valueOf(id), apiKey))
				)
			)
			.onFailure()
			.invoke(log::error);
	}

	/**
	 * Permanently deletes an API Key from the database.
	 * Use this for physical removal;
	 * for logical deactivation, see {@link #revoke(String)}.
	 *
	 * @param apiKeyId the unique identifier of the API Key to delete.
	 * @return a boolean indicating true if a record was deleted, false otherwise.
	 */
	public Uni<Boolean> delete(String apiKeyId) {
		long id = Long.parseLong(apiKeyId);
		return pool.withTransaction(conn -> getApiKeyIdentifier(conn, id)
			.flatMap(apiKeyIdentifier -> {
				if (apiKeyIdentifier == null) {
					return Uni.createFrom().item(false);
				}

				var tenantName = apiKeyIdentifier.tenantName();
				var hash = apiKeyIdentifier.hash();

				return conn.preparedQuery(DELETE_SQL)
						.execute(Tuple.of(id))
						.flatMap(res -> TenantEventProducerUtils.sendEvent(
							producer,
							res.rowCount(),
							new TenantEvent.ApiKeyRevoked(tenantName, hash)
						))
						.map(v -> true);
				}
			)
		);
	}

	/**
	 * Retrieves all API Keys associated with a specific tenant identifier.
	 * This performs a lookup to resolve the tenant's internal ID before querying keys.
	 *
	 * @param tenantId the external string identifier of the tenant.
	 * @return a list of API Key data transfer objects.
	 */
	public Uni<List<ApiKeyResponse>> findAllByTenantId(long tenantId) {

		return pool.withConnection(conn -> conn
			.preparedQuery(FETCH_ALL_BY_TENANT_ID_SQL)
			.execute(Tuple.of(tenantId))
			.map(rows -> {
				List<ApiKeyResponse> list = new ArrayList<>();

				for (Row row : rows) {
					var apiKeyResponse = mapApiKeyResponse(row);
					list.add(apiKeyResponse);
				}

				return list;
			})
		);
	}

	public Uni<ApiKeyResponse> findById(long id) {

		return pool.withConnection(conn -> conn
				.preparedQuery(FETCH_BY_ID_SQL)
				.execute(Tuple.of(id))
				.map(RowSet::iterator)
				.onItem()
				.transform(iterator -> iterator.hasNext()
					? mapApiKeyResponse(iterator.next())
					: null
				)
			);
	}

	private Uni<ApiKeyIdentifier> getApiKeyIdentifier(SqlConnection conn, long apiKeyId) {

		return conn
			.preparedQuery(FETCH_TENANT_NAME_AND_HASH_BY_ID_SQL)
			.execute(Tuple.of(apiKeyId))
			.map(RowSet::iterator)
			.onItem()
			.transform(iterator -> iterator.hasNext()
				? mapApiKeyIdentifier(iterator.next())
				: null
			);
	}

	/**
	 * Revoke the API Key updating the status.
	 *
	 * @param apiKeyId the identifier of this api key.
	 *
	 * @return a boolean indicating true if a record was deleted, false otherwise.
	 */
	public Uni<Boolean> revoke(String apiKeyId) {
		var id = Long.parseLong(apiKeyId);
		return pool.withTransaction(conn -> getApiKeyIdentifier(conn, id)
			.flatMap(apiKeyIdentifier -> {
				if (apiKeyIdentifier == null) {
					return Uni.createFrom().item(false);
				}

				var tenantName = apiKeyIdentifier.tenantName();
				var hash = apiKeyIdentifier.hash();

				return conn.preparedQuery(REVOKE_SQL)
					.execute(Tuple.of(ApiKey.Status.REVOKED, id))
					.flatMap(res -> TenantEventProducerUtils.sendEvent(
						producer,
						res.rowCount(),
						new TenantEvent.ApiKeyRevoked(tenantName, hash)
					))
					.map(v -> true);
				}
			)
		);
	}

	private static ApiKeyResponse mapApiKeyResponse(Row row) {

		return new ApiKeyResponse(
			row.getLong("id").toString(),
			row.getLong("tenant_id").toString(),
			row.getString("hash"),
			row.getString("name"),
			row.getString("status"),
			row.getString("prefix"),
			row.getString("suffix"),
			row.getString("api_group"),
			row.getOffsetDateTime("create_date"),
			row.getOffsetDateTime("expiration_date")
		);
	}

	private static ApiKeyIdentifier mapApiKeyIdentifier(Row row) {
		return new ApiKeyIdentifier(
			row.getString("schema_name"),
			row.getString("hash"));
	}


	private static final String DELETE_SQL = "DELETE FROM api_key WHERE id = $1";

	private static final String FETCH_ALL_BY_TENANT_ID_SQL = """
		SELECT
			id, tenant_id,
			hash, name, status,
			prefix, suffix, api_group,
			create_date, expiration_date
		FROM api_key WHERE tenant_id = $1
		""";

	private static final String FETCH_BY_ID_SQL = """
		SELECT
			id, tenant_id,
			hash, name, status,
			prefix, suffix, api_group,
			create_date, expiration_date
		FROM api_key WHERE id = $1
		""";

	private static final String FETCH_TENANT_NAME_AND_HASH_BY_ID_SQL = """
		SELECT ap.id as id, t.schema_name as schema_name, ap.hash as hash
		FROM api_key ap
		INNER JOIN tenant t ON ap.tenant_id = t.id
		WHERE ap.id = $1
		""";

	private static final String INSERT_SQL = """
		INSERT INTO api_key (
			id, tenant_id,
			hash, name, status,
			prefix, suffix, api_group,
			create_date, expiration_date
		)
		VALUES (
			$1, $2,
			$3, $4, $5,
			$6, $7, $8,
			$9, $10)
		""";

	private static final String REVOKE_SQL =
		"UPDATE api_key SET status = $1 WHERE id = $2";

	private static final String API_KEY_PREFIX = "ok9";

	private static final CompactSnowflakeIdGenerator ID_GENERATOR =
		new CompactSnowflakeIdGenerator();

	private static final Logger log = Logger.getLogger(ApiKeyService.class);

	@Inject
	Pool pool;
	@Inject
	TenantDbService tenantDbService;
	@Inject
	TenantEventProducer producer;

	private record ApiKeyIdentifier(String tenantName, String hash) {}
}
