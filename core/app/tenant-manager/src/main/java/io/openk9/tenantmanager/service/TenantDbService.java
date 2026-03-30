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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.common.util.web.Response;
import io.openk9.common.util.web.ResponseUtil;
import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.AuthorizationScheme;
import io.openk9.event.tenant.TenantEvent;
import io.openk9.event.tenant.TenantEventProducer;
import io.openk9.tenantmanager.dto.TenantRequestDTO;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Preconfiguration;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.model.Tenant;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.sqlclient.Row;

@ApplicationScoped
public class TenantDbService {

	private static final CompactSnowflakeIdGenerator idGenerator =
		new CompactSnowflakeIdGenerator();

	private static final String INSERT_SQL = """
		INSERT INTO tenant (
			id, virtual_host,
			tenant_name,
			issuer_uri, client_id, client_secret, security_configuration,
			create_date, modified_date, realm_provisioned
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
		""";
	private static final String FETCH_BY_ID_SQL =
		"SELECT * FROM tenant WHERE id = $1";
	private static final String FETCH_ALL_SQL = "SELECT * FROM tenant";
	private static final String FETCH_BY_VIRTUAL_HOST_SQL =
		"SELECT * FROM tenant WHERE virtual_host = $1";
	private static final String FETCH_BY_TENANT_NAME_SQL =
		"SELECT * FROM tenant WHERE tenant_name = $1";
	private static final String FETCH_ALL_TENANT_NAME_SQL =
		"SELECT tenant_name FROM tenant";
	private static final String DELETE_SQL =
		"DELETE FROM tenant WHERE id = $1";

	@Inject
	Pool pool;
	@Inject
	TenantMapper mapper;
	@Inject
	Validator validator;
	@Inject
	TenantEventProducer producer;

	/**
	 * Validates and persists a new tenant from the given DTO.
	 *
	 * @param tenantRequestDTO the request DTO with tenant data
	 * @return a response containing the created tenant DTO,
	 *         or validation errors
	 */
	public Uni<Response<TenantResponseDTO>> create(
		TenantRequestDTO tenantRequestDTO) {

		Set<ConstraintViolation<TenantRequestDTO>> violations =
			validator.validate(tenantRequestDTO);

		if (!violations.isEmpty()) {
			var fieldValidators =
				ResponseUtil.toFieldValidators(violations);
			return Uni.createFrom().item(Response.error(fieldValidators));
		}

		return persist(mapper.map(tenantRequestDTO))
			.map(Response::success);
	}

	/**
	 * Deletes a tenant by its numeric ID and publishes a
	 * {@code TenantDeleted} event.
	 *
	 * @param id the tenant's numeric identifier
	 * @return a void item on success
	 */
	public Uni<Void> deleteTenant(long id) {

		return findById(id)
			.flatMap(tenant -> pool
				.withTransaction(conn -> conn
					.preparedQuery(DELETE_SQL)
					.execute(Tuple.of(id))
					.map(SqlResult::rowCount)
					.flatMap(rowCount ->
						TenantEventProducerUtils.sendEvent(
							producer,
							rowCount,
							TenantEvent.TenantDeleted
								.builder()
								.tenantId(tenant.tenantName())
								.build()
						)
					)
				)
			);
	}

	/**
	 * Returns all tenants as DTOs.
	 *
	 * @return list of all tenant response DTOs
	 */
	public Uni<List<TenantResponseDTO>> findAll() {
		return pool.preparedQuery(FETCH_ALL_SQL)
			.execute()
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(rows -> {
				List<TenantResponseDTO> tenants = new ArrayList<>();
				while (rows.hasNext()) {
					tenants.add(
						mapTenantResponseDTO((Row) rows.next()));
				}
				return tenants;
			});
	}

	/**
	 * Returns all tenant names.	
	 *
	 * @return list of tenant name strings
	 */
	public Uni<List<String>> findAllTenantName() {
		return pool.preparedQuery(FETCH_ALL_TENANT_NAME_SQL)
			.execute()
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(rows -> {
				List<String> names = new ArrayList<>();
				while (rows.hasNext()) {
					Row row = (Row) rows.next();
					names.add(row.getString("tenant_name"));
				}
				return names;
			});
	}

	/**
	 * Finds a tenant by its numeric ID.
	 *
	 * @param id the tenant's numeric identifier
	 * @return the tenant DTO, or null if not found
	 */
	public Uni<TenantResponseDTO> findById(Long id) {

		return pool.preparedQuery(FETCH_BY_ID_SQL)
			.execute(Tuple.of(id))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.onItem()
			.transform(iterator -> iterator.hasNext()
				? mapTenantResponseDTO((Row) iterator.next())
				: null
			);
	}

	/**
	 * Finds a tenant by its tenant name.
	 *
	 * @param tenantName the tenant name to look up
	 * @return the tenant DTO, or null if not found
	 */
	public Uni<TenantResponseDTO> findByTenantName(String tenantName) {
		return pool.preparedQuery(FETCH_BY_TENANT_NAME_SQL)
			.execute(Tuple.of(tenantName))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(it ->
				it.hasNext()
					? mapTenantResponseDTO((Row) it.next())
					: null);
	}

	/**
	 * Finds a tenant by its virtual host name.
	 *
	 * @param virtualHost the virtual host to look up
	 * @return the tenant DTO, or null if not found
	 */
	public Uni<TenantResponseDTO> findByVirtualHost(String virtualHost) {
		return pool.preparedQuery(FETCH_BY_VIRTUAL_HOST_SQL)
			.execute(Tuple.of(virtualHost))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(it ->
				it.hasNext()
					? mapTenantResponseDTO((Row) it.next())
					: null);
	}

	/**
	 * Persists a new tenant row in the database and publishes a
	 * {@code TenantCreated} event.
	 *
	 * @param virtualHost          the virtual host for the tenant
	 * @param tenantName           the name of the tenant,
	 * 				used for schema name and
	 * 				eventually for realm name
	 * @param issuerUri            the OAuth2 issuer URI
	 * @param clientId             the OAuth2 client ID
	 * @param clientSecret         the OAuth2 client secret
	 * @param securityConfiguration the tenant's security model
	 * @param createDate           the creation timestamp
	 * @param modifiedDate         the modification timestamp
	 * @param realmProvisioned     whether a Keycloak realm was
	 *                             auto-provisioned
	 * @return the created tenant response DTO
	 */
	public Uni<TenantResponseDTO> persist(
		String virtualHost,
		String tenantName,
		String issuerUri,
		String clientId,
		String clientSecret,
		SecurityConfiguration securityConfiguration,
		OffsetDateTime createDate,
		OffsetDateTime modifiedDate,
		boolean realmProvisioned) {

		long id = idGenerator.nextId();

		return pool.withTransaction(conn -> conn
				.preparedQuery(INSERT_SQL)
				.execute(Tuple.from(new Object[]{
					id,
					virtualHost,
					tenantName,
					issuerUri,
					clientId,
					clientSecret,
					securityConfiguration.name(),
					createDate != null
						? createDate
						: OffsetDateTime.now(),
					modifiedDate != null
						? modifiedDate
						: OffsetDateTime.now(),
					realmProvisioned
				}))
				.map(SqlResult::rowCount)
				.flatMap(rowCount ->
					TenantEventProducerUtils.sendEvent(
						producer,
						rowCount,
						TenantEvent.TenantCreated.builder()
							.tenantId(tenantName)
							.tenantName(tenantName)
							.hostName(virtualHost)
							.clientId(clientId)
							.clientSecret(clientSecret)
							.issuerUri(issuerUri)
							.routeAuthorizationMap(
								fromSecurityConfiguration(
									securityConfiguration))
							.build()
					)
				)
			)
			.map(done -> TenantResponseDTO.builder()
				.id(String.valueOf(id))
				.virtualHost(virtualHost)
				.tenantName(tenantName)
				.issuerUri(issuerUri)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.realmProvisioned(realmProvisioned)
				.build()
			);

	}

	/**
	 * Persists a new tenant from a {@link Tenant} entity instance.
	 *
	 * @param tenant the tenant entity to persist
	 * @return the created tenant response DTO
	 */
	public Uni<TenantResponseDTO> persist(Tenant tenant) {

		return persist(
			tenant.getVirtualHost(),
			tenant.getTenantName(),
			tenant.getIssuerUri(),
			tenant.getClientId(),
			tenant.getClientSecret(),
			tenant.getSecurityConfiguration(),
			tenant.getCreateDate(),
			tenant.getModifiedDate(),
			tenant.isRealmProvisioned()
		);
	}

	private static Map<ApiGroup, AuthorizationScheme>
		fromSecurityConfiguration(
			SecurityConfiguration securityConfiguration) {

		Map<ApiGroup, AuthorizationScheme> map = new HashMap<>();
		for (Preconfiguration.Config config :
			Preconfiguration.PRECONFIGURATION_MAP
				.get(securityConfiguration)) {

			map.put(config.apiGroup(), config.authScheme());
		}

		return map;
	}

	private static TenantResponseDTO mapTenantResponseDTO(Row row) {
		return TenantResponseDTO.builder()
			.id(String.valueOf(row.getLong("id")))
			.tenantName(row.getString("tenant_name"))
			.virtualHost(row.getString("virtual_host"))
			.clientId(row.getString("client_id"))
			.clientSecret(row.getString("client_secret"))
			.issuerUri(row.getString("issuer_uri"))
			.securityConfiguration(SecurityConfiguration.valueOf(
				row.getString("security_configuration")))
			.realmProvisioned(row.getBoolean("realm_provisioned"))
			.build();
	}

}
