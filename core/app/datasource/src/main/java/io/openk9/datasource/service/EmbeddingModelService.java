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

import io.openk9.datasource.mapper.EmbeddingModelMapper;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.EmbeddingModel_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class EmbeddingModelService extends BaseK9EntityService<EmbeddingModel, EmbeddingModelDTO> {

	EmbeddingModelService(EmbeddingModelMapper mapper) {
		this.mapper = mapper;
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

}
