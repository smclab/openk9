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

package io.openk9.datasource.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.NotFoundException;

import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.mapper.EmbeddingModelMapper;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.EmbeddingModel_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.util.K9Entity;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class EmbeddingModelService extends BaseK9EntityService<EmbeddingModel, EmbeddingModelDTO> {

	@Inject
	IndexMappingService indexMappingService;

	EmbeddingModelService(EmbeddingModelMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<EmbeddingModel> fetchCurrent(Mutiny.Session session) {
		return session.createNamedQuery(
				EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
			.getSingleResult();
	}

	public Uni<EmbeddingModel> fetchCurrent(String tenantId) {
		return sessionFactory.withTransaction(tenantId, (s, t) -> s
			.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
			.getSingleResult());
	}

	@Override
	public <T extends K9Entity> Uni<T> merge(Mutiny.Session session, T entity) {

		return super.merge(session, entity)
			.call(model -> createComponentTemplate(session, (EmbeddingModel) model));

	}

	@Override
	public <T extends K9Entity> Uni<T> persist(Mutiny.Session session, T entity) {

		return super.persist(session, entity)
			.call(model -> createComponentTemplate(session, (EmbeddingModel) model));

	}

	@Override
	public Class<EmbeddingModel> getEntityClass() {
		return EmbeddingModel.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{EmbeddingModel_.NAME, EmbeddingModel_.DESCRIPTION};
	}

	public Uni<EmbeddingModel> enable(Mutiny.Session s, long id) {
		return findById(s, id)
			.flatMap(embeddingModel -> {

				if (embeddingModel == null) {
					return Uni
						.createFrom()
						.failure(new NotFoundException("EmbeddingModel not found for id " + id));
				}

				TenantBinding tenantBinding = embeddingModel.getTenantBinding();

				if (tenantBinding == null) {
					CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();

					CriteriaQuery<TenantBinding> query =
						criteriaBuilder.createQuery(TenantBinding.class);

					query.from(TenantBinding.class);

					return s
						.createQuery(query)
						.getSingleResultOrNull()
						.flatMap(tb -> {

							if (tb == null) {
								return Uni
									.createFrom()
									.failure(new NotFoundException(
										"Tenant binding not found create one first"));
							}

							embeddingModel.setTenantBinding(tb);
							tb.setEmbeddingModel(embeddingModel);

							return s
								.persist(tb)
								.map(t -> embeddingModel)
								.call(s::flush);

						});

				}

				return Uni.createFrom().item(embeddingModel);

			});
	}

	public Uni<EmbeddingModel> enable(long id) {
		return sessionFactory.withTransaction((s, t) -> enable(s, id));
	}

	private Uni<Void> createComponentTemplate(Mutiny.Session session, EmbeddingModel entity) {

		return getCurrentTenant(session)
			.flatMap(tenant -> {
				var tenantId = tenant.schemaName();

				var embeddingComponentTemplate = new EmbeddingComponentTemplate(
					tenantId,
					entity.getName(),
					entity.getVectorSize()
				);

				return indexMappingService.createEmbeddingComponentTemplate(
					session,
					embeddingComponentTemplate);
			});

	}

}
