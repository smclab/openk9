package io.openk9.tenantmanager.service;

import io.openk9.common.graphql.util.service.GraphQLService;
import io.openk9.common.model.EntityService;
import io.openk9.common.model.EntityServiceValidatorWrapper;
import io.openk9.tenantmanager.dto.SchemaTuple;
import io.openk9.tenantmanager.dto.TenantDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.model.Tenant_;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.Validator;
import java.util.List;
import java.util.function.BiFunction;

@ApplicationScoped
public class TenantService
	extends GraphQLService<Tenant>
	implements EntityService<Tenant, TenantDTO> {

	public Uni<Tenant> findById(Long id) {
		return sf.withStatelessTransaction(
			(s) -> s.get(Tenant.class, id));
	}

	public Uni<Tenant> persist(Tenant tenant) {
		return sf.withTransaction(
			session -> session
				.persist(tenant)
				.map(__ -> tenant)
		);
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
		return sf.withTransaction(s -> {

			Uni<Tenant> tenantUni = s.find(Tenant.class, id);

			return tenantUni.flatMap(t -> {

				if (t != null) {
					return s.merge(tenant);
				}
				else {
					return Uni.createFrom().failure(
						new RuntimeException("Tenant not found with id: " + id));
				}
			});

		});
	}

	public Uni<Void> deleteTenant(long tenantId) {

		return sf.withTransaction(
			session ->
				session
				.createQuery("delete from BackgroundProcess where tenant.id = :tenantId")
				.setParameter("tenantId", tenantId)
				.executeUpdate()
				.chain(() ->
					session
						.createQuery("delete from Tenant where id = :tenantId")
						.setParameter("tenantId", tenantId)
						.executeUpdate()
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

	private EntityServiceValidatorWrapper<Tenant, TenantDTO>
		entityServiceValidatorWrapper;

}
