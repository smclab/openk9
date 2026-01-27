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
import io.openk9.tenantmanager.dto.SchemaTuple;
import io.openk9.tenantmanager.dto.TenantRequestDTO;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantDbService {

	public Uni<TenantResponseDTO> findById(Long id) {

		return pool.preparedQuery(FETCH_BY_ID_SQL)
			.execute(Tuple.of(id))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.onItem()
			.transform(iterator -> iterator.hasNext()
				? mapTenantResponseDTO((Row)iterator.next())
				: null
			);
	}

	public Uni<TenantResponseDTO> persist(
		String virtualHost, String schemaName, String liquibaseSchemaName,
		String issuerUri, String clientId, String clientSecret,
		OffsetDateTime createDate, OffsetDateTime modifiedDate) {

		long id = idGenerator.nextId();

		return pool.withTransaction(conn -> conn
				.preparedQuery(INSERT_SQL)
				.execute(Tuple.from(new Object[] {
					id,
					virtualHost,
					schemaName,
					liquibaseSchemaName,
					issuerUri,
					clientId,
					clientSecret,
					createDate != null ? createDate : OffsetDateTime.now(),
					modifiedDate != null ? modifiedDate : OffsetDateTime.now()
				}))
				.map(SqlResult::rowCount)
				.flatMap(rowCount -> TenantEventProducerUtils.sendEvent(
						producer,
						rowCount,
						TenantEvent.TenantCreated.builder()
							.tenantId(schemaName)
							.schemaName(schemaName)
							.hostName(virtualHost)
							.clientId(clientId)
							.issuerUri(issuerUri)
							.routeAuthorizationMap(Map.of(
								ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2,
								ApiGroup.SEARCH, AuthorizationScheme.NO_AUTH,
								ApiGroup.PUBLIC, AuthorizationScheme.NO_AUTH
							))
							.build()
					)
				)
			)
			.map(done -> TenantResponseDTO.builder()
				.id(String.valueOf(id))
				.virtualHost(virtualHost)
				.schemaName(schemaName)
				.liquibaseSchemaName(liquibaseSchemaName)
				.issuerUri(issuerUri)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.build()
			)
			.onFailure()
			.invoke((e) -> log.error("An error occurred, tx is rolled back", e))
			.onFailure().recoverWithNull();

	}

	public Uni<TenantResponseDTO> persist(Tenant tenant) {

		return persist(
			tenant.getVirtualHost(),
			tenant.getSchemaName(),
			tenant.getLiquibaseSchemaName(),
			tenant.getIssuerUri(),
			tenant.getClientId(),
			tenant.getClientSecret(),
			tenant.getCreateDate(),
			tenant.getModifiedDate());
	}

	public Uni<Response<TenantResponseDTO>> create(TenantRequestDTO tenantRequestDTO) {
		Set<ConstraintViolation<TenantRequestDTO>> violations = validator.validate(tenantRequestDTO);

		if (!violations.isEmpty()) {
			var fieldValidators = ResponseUtil.toFieldValidators(violations);
			return Uni.createFrom().item(Response.error(fieldValidators));
		}

		return persist(mapper.map(tenantRequestDTO))
			.map(Response::success);
	}

	public Uni<Void> deleteTenant(long id) {

		return findById(id)
			.flatMap(tenant -> pool
				.withTransaction(conn -> conn
					.preparedQuery(DELETE_SQL)
					.execute(Tuple.of(id))
					.map(SqlResult::rowCount)
					.flatMap(rowCount -> TenantEventProducerUtils.sendEvent(
							producer,
							rowCount,
							TenantEvent.TenantDeleted
								.builder()
								.tenantId(tenant.schemaName())
								.build()
						)
					)
				)
			)
			.onFailure()
			.invoke((e) -> log.error("An error occurred, tx is rolled back", e))
			.onFailure().recoverWithNull();
	}

	public Uni<List<TenantResponseDTO>> findAll() {
		return pool.preparedQuery(FETCH_ALL_SQL)
			.execute()
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(rows -> {
				List<TenantResponseDTO> tenants = new ArrayList<>();
				while (rows.hasNext()) {
					tenants.add(mapTenantResponseDTO((Row)rows.next()));
				}
				return tenants;
			});
	}

	public Uni<TenantResponseDTO> findByVirtualHost(String virtualHost) {
		return pool.preparedQuery(FETCH_BY_VIRTUAL_HOST_SQL)
			.execute(Tuple.of(virtualHost))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(it -> it.hasNext() ? mapTenantResponseDTO((Row)it.next()) : null);
	}

	public Uni<TenantResponseDTO> findByTenantName(String tenantId) {
		return pool.preparedQuery(FETCH_BY_SCHEMA_NAME_SQL)
			.execute(Tuple.of(tenantId))
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(it -> it.hasNext() ? mapTenantResponseDTO((Row)it.next()) : null);
	}

	public Uni<List<String>> findAllSchemaName() {
		return pool.preparedQuery(FETCH_ALL_SCHEMA_NAME_SQL)
			.execute()
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(rows -> {
				List<String> schemas = new ArrayList<>();
				while (rows.hasNext()) {
					Row row = (Row) rows.next();
					schemas.add(row.getString("schema_name"));
				}
				return schemas;
			});
	}

	public Uni<List<SchemaTuple>> findAllDatasourceSchemaTuples() {
		return pool.preparedQuery(FETCH_ALL_SCHEMA_NAMES_SQL)
			.execute()
			.map(RowSet::getDelegate)
			.map(io.vertx.sqlclient.RowSet::iterator)
			.map(rows -> {
				List<SchemaTuple> schemas = new ArrayList<>();
				while (rows.hasNext()) {
					Row row = (Row) rows.next();
					String schemaName = row.getString("schema_name");
					String liquibaseSchemaName = row.getString("liquibase_schema_name");
					schemas.add(new SchemaTuple(schemaName, liquibaseSchemaName));
				}
				return schemas;
			});
	}

	private static TenantResponseDTO mapTenantResponseDTO(Row row) {
		return TenantResponseDTO.builder()
			.id(String.valueOf(row.getLong("id")))
			.schemaName(row.getString("schema_name"))
			.liquibaseSchemaName(row.getString("liquibase_schema_name"))
			.virtualHost(row.getString("virtual_host"))
			.clientId(row.getString("client_id"))
			.clientSecret(row.getString("client_secret"))
			.issuerUri(row.getString("issuer_uri"))
			.build();
	}

	@Inject
	Pool pool;
	@Inject
	TenantMapper mapper;
	@Inject
	Validator validator;
	@Inject
	TenantEventProducer producer;

	private static final CompactSnowflakeIdGenerator idGenerator =
		new CompactSnowflakeIdGenerator();

	private static final String INSERT_SQL = """
		INSERT INTO tenant (
			id, virtual_host,
			schema_name, liquibase_schema_name,
			issuer_uri, client_id, client_secret,
			create_date, modified_date
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
		""";

	private static final String FETCH_BY_ID_SQL = "SELECT * FROM tenant WHERE id = $1";
	private static final String FETCH_ALL_SQL = "SELECT * FROM tenant";
	private static final String FETCH_BY_VIRTUAL_HOST_SQL = "SELECT * FROM tenant WHERE virtual_host = $1";
	private static final String FETCH_BY_SCHEMA_NAME_SQL = "SELECT * FROM tenant WHERE schema_name = $1";
	private static final String FETCH_ALL_SCHEMA_NAME_SQL = "SELECT schema_name FROM tenant";
	private static final String FETCH_ALL_SCHEMA_NAMES_SQL = "SELECT schema_name, liquibase_schema_name FROM tenant";
	private static final String DELETE_SQL = "DELETE FROM tenant WHERE id = $1";

	private static final Logger log = Logger.getLogger(TenantDbService.class);


}
