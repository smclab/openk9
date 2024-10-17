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

import io.openk9.datasource.mapper.LargeLanguageModelMapper;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.LargeLanguageModel_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.LargeLanguageModelDTO;
import io.openk9.datasource.model.projection.BucketLargeLanguageModel;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class LargeLanguageModelService
	extends BaseK9EntityService<LargeLanguageModel, LargeLanguageModelDTO> {

	LargeLanguageModelService(LargeLanguageModelMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<LargeLanguageModel> enable(long id) {
		return sessionFactory.withTransaction((s, t) -> enable(s, id));
	}

	public Uni<LargeLanguageModel> enable(Mutiny.Session s, long id) {
		return findById(s, id)
			.flatMap(largeLanguageModel -> {

				if (largeLanguageModel == null) {
					return Uni
						.createFrom()
						.failure(new NotFoundException(
							"LargeLanguageModel not found for id " + id));
				}

				TenantBinding tenantBinding = largeLanguageModel.getTenantBinding();

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

							largeLanguageModel.setTenantBinding(tb);
							tb.setLargeLanguageModel(largeLanguageModel);

							return s
								.persist(tb)
								.map(t -> largeLanguageModel)
								.call(s::flush);

						});

				}

				return Uni.createFrom().item(largeLanguageModel);

			});
	}

	public Uni<LargeLanguageModel> fetchCurrent() {
		return sessionFactory.withTransaction((s, t) -> s
			.createNamedQuery(LargeLanguageModel.FETCH_CURRENT, LargeLanguageModel.class)
			.getSingleResult());
	}

	public Uni<LargeLanguageModel> fetchCurrent(String tenantId) {
		return sessionFactory.withTransaction(tenantId, (s, t) -> s
			.createNamedQuery(LargeLanguageModel.FETCH_CURRENT, LargeLanguageModel.class)
			.getSingleResult());
	}

	public Uni<BucketLargeLanguageModel> fetchCurrentLLMAndBucket(String tenantId) {

		String queryString = "SELECT tb " +
			"FROM TenantBinding tb " +
			"LEFT JOIN FETCH tb.largeLanguageModel llm " +
			"LEFT JOIN FETCH tb.bucket b ";

		return sessionFactory.withTransaction(tenantId, (s, t) -> s
			.createQuery(queryString, TenantBinding.class)
			.getSingleResult()
			.map(tenantBinding ->
				new BucketLargeLanguageModel(
					tenantBinding.getBucket(), tenantBinding.getLargeLanguageModel())
			));
	}

	@Override
	public Class<LargeLanguageModel> getEntityClass() {
		return LargeLanguageModel.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{LargeLanguageModel_.NAME, LargeLanguageModel_.DESCRIPTION};
	}

}
