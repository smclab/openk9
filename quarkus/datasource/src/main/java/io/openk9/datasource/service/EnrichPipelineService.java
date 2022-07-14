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

import io.openk9.datasource.mapper.EnrichPipelineMapper;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EnrichPipelineService extends BaseK9EntityService<EnrichPipeline, EnrichPipelineDTO> {
	 EnrichPipelineService(EnrichPipelineMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<EnrichItem>> getEnrichItems(long enrichPipelineId, Pageable pageable) {

		Map<String, Object> params = new HashMap<>();

		params.put("enrichPipelineId", enrichPipelineId);

		String query =
			"select ei " +
			"from EnrichPipeline enrichPipeline " +
			"join enrichPipeline.enrichItems ei " +
			"where enrichPipeline.id = :enrichPipelineId ";

		query = createPageableQuery(pageable, params, query, "ei");

		Sort sort = createSort("ei", pageable.getSortBy().name());

		 PanacheQuery<EnrichItem> docTypePanacheQuery =
			 EnrichPipeline
				.find(query, sort, params)
				.page(0, pageable.getLimit());

		 Uni<Long> countQuery =
			 EnrichPipeline
				.count("from EnrichPipeline ep join ep.enrichItems where ep.id = ?1", enrichPipelineId);

		return createPage(
			pageable.getLimit(), docTypePanacheQuery, countQuery);
	}

	public Uni<Void> addEnrichItem(long enrichPipelineId, long enrichItemId) {
		return findById(enrichPipelineId)
			.flatMap(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.flatMap(enrichItem -> {
						enrichPipeline.addEnrichItem(enrichItem);
						return persist(enrichPipeline);
					}))
			.replaceWithVoid();
	}

	public Uni<Void> removeEnrichItem(long enrichPipelineId, long enrichItemId) {
		return findById(enrichPipelineId)
			.flatMap(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.flatMap(enrichItem -> {
						enrichPipeline.removeEnrichItem(enrichItem);
						return persist(enrichPipeline);
					}))
			.replaceWithVoid();
	}

	@Inject
	EnrichItemService enrichItemService;

	@Override
	public Class<EnrichPipeline> getEntityClass() {
		return EnrichPipeline.class;
	}
}
