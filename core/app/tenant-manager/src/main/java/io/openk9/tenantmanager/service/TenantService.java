package io.openk9.tenantmanager.service;

import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.model.Tenant_;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@ApplicationScoped
public class TenantService {

	public Uni<Tenant> addTenant(Tenant tenant) {
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

	@Inject
	Mutiny.SessionFactory sf;

}
