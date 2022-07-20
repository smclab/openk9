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
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EnrichPipelineService extends BaseK9EntityService<EnrichPipeline, EnrichPipelineDTO> {
	 EnrichPipelineService(EnrichPipelineMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable) {
		 return getEnrichItems(enrichPipelineId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[] { enrichPipelineId },
			"enrichItems", EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable,
		Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { enrichPipelineId },
			"enrichItems", EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> addEnrichItem(long enrichPipelineId, long enrichItemId) {
		return findById(enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.onItem()
					.ifNotNull()
					.transformToUni(enrichItem -> Mutiny.fetch(enrichPipeline.getEnrichItems()).flatMap(enrichItems -> {
						if (enrichItems.add(enrichItem)) {
							enrichPipeline.setEnrichItems(enrichItems);
							return create(enrichPipeline).map(ep -> Tuple2.of(ep, enrichItem));
						} else {
							return Uni.createFrom().nullItem();
						}
					})));
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> removeEnrichItem(long enrichPipelineId, long enrichItemId) {
		return findById(enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.onItem()
					.ifNotNull()
					.transformToUni(enrichItem -> Mutiny.fetch(enrichPipeline.getEnrichItems()).flatMap(enrichItems -> {
						if (enrichItems.remove(enrichItem)) {
							enrichPipeline.setEnrichItems(enrichItems);
							return create(enrichPipeline).map(ep -> Tuple2.of(ep, enrichItem));
						}
						return Uni.createFrom().nullItem();
					})));
	}

	@Inject
	EnrichItemService enrichItemService;

	@Override
	public Class<EnrichPipeline> getEntityClass() {
		return EnrichPipeline.class;
	}
}
