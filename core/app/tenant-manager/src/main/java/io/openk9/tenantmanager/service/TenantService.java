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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantService extends GraphQLService<Tenant>
	implements EntityService<Tenant, TenantDTO> {

	public Uni<Tenant> findById(Long id) {
		return sf.withStatelessTransaction(
			(s) -> s.get(Tenant.class, id));
	}

	public Uni<Tenant> persist(Tenant tenant) {
		var realmName = tenant.getRealmName();
		var issuerUri = authServerUrl.replace("/tenant-manager", "/" + realmName);

		return sf.withTransaction((session, tx) ->
			VertxContextSupport.executeBlocking(() -> session.persist(tenant)
				.chain(() -> VertxContextSupport.executeBlocking(() -> {
					try {
						producer.send(
							TenantManagementEvent.TenantCreated.builder()
								.tenantId(tenant.getSchemaName())
								.hostName(tenant.getVirtualHost())
								.clientId(tenant.getClientId())
								.issuerUri(issuerUri)
								.routeAuthorizationMap(Map.of(
									"DATASOURCE", "OAUTH2",
									"SEARCHER", "NO_OAUTH"
								))
								.build()
						);
					}
					catch (Exception e) {
						log.error(e);
						tx.markForRollback();
					}

					return null;
				}))
				.map(__ -> tenant)
			)
		).flatMap(Function.identity());
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
					var realmName = tenant.getRealmName();
					var issuerUri = authServerUrl.replace("/tenant-manager", "/" + realmName);

					return session.merge(tenant)
						.invoke(() -> {
							try {
								producer.send(
									TenantManagementEvent.TenantUpdated.builder()
										.tenantId(tenant.getSchemaName())
										.hostName(tenant.getVirtualHost())
										.clientId(tenant.getClientId())
										.issuerUri(issuerUri)
										.routeAuthorizationMap(Map.of(
											"DATASOURCE", "OAUTH2",
											"SEARCHER", "OAUTH2"
										))
										.build()
								);
							}
							catch (Exception e) {
								tx.markForRollback();
							}
						});
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
			.call((tenant) -> session.remove(tenant)
				.invoke(() -> {
					try {
						producer.send(
							TenantManagementEvent.TenantDeleted.builder()
								.tenantId(tenant.getSchemaName())
								.build()
						);
					}
					catch (Exception e) {
						tx.markForRollback();
					}
				})
			)
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
	Mutiny.SessionFactory sf;
	@Inject
	TenantMapper mapper;
	@Inject
	Validator validator;
	@Inject
	TenantManagementEventProducer producer;

	@ConfigProperty(name = "quarkus.oidc.auth-server.url")
	String authServerUrl;

	private EntityServiceValidatorWrapper<Tenant, TenantDTO>
		entityServiceValidatorWrapper;

	private static final Logger log = Logger.getLogger(TenantService.class);

	public void onStartup(@Observes Startup startup) {

		log.info("insert a tenant");

		var tenant = new Tenant();
		tenant.setRealmName("alabasta");
		tenant.setSchemaName("alabasta");
		tenant.setVirtualHost("http://alabasta.localhost:8080");
		tenant.setClientId("openk9");
		tenant.setLiquibaseSchemaName("alabasta_liquibase");

		persist(tenant).subscribe().with(tenant1 -> {});

	}


}
