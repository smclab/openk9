package io.openk9.tenantmanager.service;

import io.openk9.common.graphql.util.service.GraphQLService;
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
import java.util.List;
import java.util.function.BiFunction;

@ApplicationScoped
public class TenantService extends GraphQLService<Tenant> {

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
	protected final SingularAttribute<Tenant, Long> getIdAttribute() {
		return Tenant_.id;
	}

	@Override
	protected <T> Uni<T> withTransaction(
		BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> function) {
		return sf.withTransaction(function);
	}

	@Inject
	Mutiny.SessionFactory sf;

}
