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
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.validation.Validator;

import io.openk9.common.graphql.util.service.GraphQLService;
import io.openk9.common.model.EntityService;
import io.openk9.common.model.EntityServiceValidatorWrapper;
import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.event.tenant.TenantManagementEventProducer;
import io.openk9.tenantmanager.dto.SchemaTuple;
import io.openk9.tenantmanager.dto.TenantDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.model.Tenant_;

import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantService extends GraphQLService<Tenant>
	implements EntityService<Tenant, TenantDTO> {

	private static final CompactSnowflakeIdGenerator idGenerator =
		new CompactSnowflakeIdGenerator();

	private static Tenant from(Row row) {
		return Tenant.builder()
			.id(row.getLong("id"))
			.createDate(row.getOffsetDateTime("create_date"))
			.modifiedDate(row.getOffsetDateTime("modified_date"))
			.schemaName(row.getString("schema_name"))
			.liquibaseSchemaName(row.getString("liquibase_schema_name"))
			.virtualHost(row.getString("virtual_host"))
			.clientId(row.getString("client_id"))
			.clientSecret(row.getString("client_secret"))
			.realmName(row.getString("realm_name"))
			.build();
	}

	public Uni<Tenant> findById(Long id) {

		return pool.preparedQuery("SELECT * FROM tenant WHERE id = $1")
			.execute(Tuple.of(id))
			.onItem().transform(RowSet::iterator)
			.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Tenant> persist(
		String virtualHost, String schemaName, String liquibaseSchemaName,
		String realmName, String clientId, String clientSecret,
		OffsetDateTime createDate, OffsetDateTime modifiedDate) {

		String issuerUri = "http://localhost:9090/realms/" + realmName;
		var id = idGenerator.nextId();

		return pool.withTransaction(sqlConnection -> sqlConnection.preparedQuery("""
			INSERT INTO tenant (
				id, virtual_host,
				schema_name, liquibase_schema_name,
				realm_name, client_id, client_secret,
				create_date, modified_date
			)
			VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
			""")
			.execute(Tuple.from(new Object[] {
				id, virtualHost,
				schemaName, liquibaseSchemaName,
				realmName, clientId, clientSecret,
				createDate, modifiedDate
			}))
			.flatMap(rowSet -> VertxContextSupport.executeBlocking(() -> {
				try {
					producer.send(
						TenantManagementEvent.TenantCreated.builder()
							.tenantId(schemaName)
							.hostName(virtualHost)
							.clientId(clientId)
							.issuerUri(issuerUri)
							.routeAuthorizationMap(Map.of(
								"DATASOURCE", "OAUTH2",
								"SEARCHER", "NO_OAUTH"
							))
							.build()
					);

					return Tenant.builder()
						.id(id)
						.virtualHost(virtualHost)
						.schemaName(schemaName)
						.liquibaseSchemaName(liquibaseSchemaName)
						.realmName(realmName)
						.clientId(clientId)
						.clientSecret(clientSecret)
						.createDate(createDate)
						.modifiedDate(modifiedDate)
						.build();
				}
				catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}))
		);

	}

	public Uni<Tenant> persist(Tenant tenant) {
		return persist(
			tenant.getVirtualHost(),
			tenant.getSchemaName(),
			tenant.getLiquibaseSchemaName(),
			tenant.getRealmName(),
			tenant.getClientId(),
			tenant.getClientSecret(),
			tenant.getCreateDate(),
			tenant.getModifiedDate());
	}

	@Override
	public Uni<Tenant> patch(long id, TenantDTO tenantDTO) {
		return update(id, mapper.patch(tenantDTO));
	}

	@Override
	public Uni<Tenant> update(long id, TenantDTO tenantDTO) {
		return update(id, mapper.map(tenantDTO));
	}

	@Override
	public Uni<Tenant> create(TenantDTO tenantDTO) {
		return persist(mapper.map(tenantDTO));
	}

	public EntityServiceValidatorWrapper<Tenant, TenantDTO> getValidator() {

		if (entityServiceValidatorWrapper == null) {
			entityServiceValidatorWrapper =
				new EntityServiceValidatorWrapper<>(this, validator);
		}

		return entityServiceValidatorWrapper;

	}

	public Uni<Tenant> update(long id, Tenant tenant) {
		return sf.withTransaction((session, tx) -> {

			Uni<Tenant> tenantUni = session.find(Tenant.class, id);

			return tenantUni.flatMap(t -> {

				if (t != null) {
					return session.merge(tenant);
				}
				else {
					return Uni.createFrom().failure(
						new RuntimeException("Tenant not found with id: " + id));
				}
			});

		});
	}

	public Uni<Void> deleteTenant(long tenantId) {

		return sf.withTransaction((session, tx) -> session
			.find(Tenant.class, tenantId)
			.call(session::remove)
			.replaceWithVoid()
		);

	}

	public Uni<List<Tenant>> findAllTenant() {
		return sf.withStatelessSession(
			s -> {

				CriteriaBuilder cb = sf.getCriteriaBuilder();

				CriteriaQuery<Tenant> query = cb.createQuery(Tenant.class);

				query.from(Tenant.class);

				return s.createQuery(query).getResultList();

			}
		);
	}

	public Uni<Tenant> findTenantByVirtualHost(String virtualHost) {
		return sf.withStatelessSession(
			s -> {

				CriteriaBuilder cb = sf.getCriteriaBuilder();

				CriteriaQuery<Tenant> query = cb.createQuery(Tenant.class);

				Root<Tenant> root = query.from(Tenant.class);

				query.where(
					cb.equal(
						root.get(Tenant_.virtualHost), virtualHost
					)
				);

				return s.createQuery(query).getSingleResultOrNull();

			}
		);
	}

	public Uni<List<String>> findAllSchemaName() {
		return sf.withStatelessSession(
			s -> {

				CriteriaBuilder cb = sf.getCriteriaBuilder();

				CriteriaQuery<String> query = cb.createQuery(String.class);

				Root<Tenant> root = query.from(Tenant.class);

				query.select(root.get(Tenant_.schemaName));

				query.distinct(true);

				return s.createQuery(query).getResultList();

			}
		);
	}

	public Uni<List<SchemaTuple>> findAllSchemaNameAndLiquibaseSchemaName() {
		return sf.withStatelessSession(
			s -> {

				CriteriaBuilder cb = sf.getCriteriaBuilder();

				CriteriaQuery<SchemaTuple> query = cb.createQuery(SchemaTuple.class);

				Root<Tenant> root = query.from(Tenant.class);

				query.multiselect(
					root.get(Tenant_.schemaName),
					root.get(Tenant_.liquibaseSchemaName)
				);

				query.distinct(true);

				return s.createQuery(query).getResultList();

			}
		);
	}

	@Override
	protected Class<Tenant> getEntityClass() {
		return Tenant.class;
	}

	@Override
	protected String[] getSearchFields() {
		return new String[] {
			Tenant_.VIRTUAL_HOST,
			Tenant_.REALM_NAME,
			Tenant_.CLIENT_ID,
			Tenant_.SCHEMA_NAME
		};
	}

	@Override
	protected CriteriaBuilder getCriteriaBuilder() {
		return sf.getCriteriaBuilder();
	}

	@Override
	protected Mutiny.SessionFactory getSessionFactory() {
		return sf;
	}

	@Override
	protected final SingularAttribute<Tenant, Long> getIdAttribute() {
		return Tenant_.id;
	}

	@Inject
	Pool pool;
	@Inject
	Mutiny.SessionFactory sf;
	@Inject
	TenantMapper mapper;
	@Inject
	Validator validator;
	@Inject
	TenantManagementEventProducer producer;

	private EntityServiceValidatorWrapper<Tenant, TenantDTO>
		entityServiceValidatorWrapper;

	private static final Logger log = Logger.getLogger(TenantService.class);


}
