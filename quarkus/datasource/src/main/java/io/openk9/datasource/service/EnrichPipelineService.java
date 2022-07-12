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
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class EnrichPipelineService extends BaseK9EntityService<EnrichPipeline> {
	 EnrichPipelineService(EnrichPipelineMapper mapper) {
		patchMapper = mapper;
	}

	public Uni<Collection<EnrichItem>> getEnrichItems(long enrichPipelineId) {
		return findById(enrichPipelineId)
			.flatMap(pipeline -> Mutiny.fetch(pipeline.getEnrichItems()));
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

}
