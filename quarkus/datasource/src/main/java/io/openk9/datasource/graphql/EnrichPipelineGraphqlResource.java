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

package io.openk9.datasource.graphql;

import io.openk9.datasource.graphql.util.Response;
import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.resource.util.K9Column;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class EnrichPipelineGraphqlResource {

	@Query
	public Uni<Page<EnrichPipeline>> getEnrichPipelines(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") K9Column sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return enrichPipelineService.findAllPaginated(
			limit, offset, sortBy.name(), sortType);
	}

	@Query
	public Uni<Page<EnrichItem>> getEnrichItemsFromEnrichPipelineId(
		@Name("enrichPipelineId") long enrichPipelineId,
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") K9Column sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return enrichPipelineService.getEnrichItems(
			enrichPipelineId, Pageable.of(limit, offset, sortBy, sortType));
	}

	@Query
	public Uni<EnrichPipeline> getEnrichPipeline(long id) {
		return enrichPipelineService.findById(id);
	}

	@Mutation
	public Uni<EnrichPipeline> patchEnrichPipeline(long id, EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.patch(id, enrichPipelineDTO);
	}

	@Mutation
	public Uni<EnrichPipeline> updateEnrichPipeline(long id, EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.update(id, enrichPipelineDTO);
	}

	@Mutation
	public Uni<EnrichPipeline> createEnrichPipeline(EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.persist(enrichPipelineDTO);
	}

	@Mutation
	public Uni<EnrichPipeline> deleteEnrichPipeline(long enrichPipelineId) {
		return enrichPipelineService.deleteById(enrichPipelineId);
	}

	@Mutation
	public Uni<Response> addEnrichItemToEnrichPipeline(long enrichPipelineId, long enrichItemId) {
		return enrichPipelineService.addEnrichItem(
				enrichPipelineId, enrichItemId)
			.replaceWith(
				() -> Response.of("Enrich item added to enrich pipeline"));
	}

	@Mutation
	public Uni<Response> removeEnrichItemToEnrichPipeline(long enrichPipelineId, long enrichItemId) {
		return enrichPipelineService.removeEnrichItem(enrichPipelineId, enrichItemId)
			.replaceWith(
				() -> Response.of("Enrich item removed from enrich pipeline"));
	}

	@Subscription
	public Multi<EnrichPipeline> enrichPipelineCreated() {
		return enrichPipelineService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EnrichPipeline> enrichPipelineDeleted() {
		return enrichPipelineService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EnrichPipeline> enrichPipelineUpdated() {
		return enrichPipelineService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	EnrichPipelineService enrichPipelineService;

}