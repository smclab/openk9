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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.validation.Response;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class EnrichPipelineGraphqlResource {

	@Query
	public Uni<Connection<EnrichPipeline>> getEnrichPipelines(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return enrichPipelineService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Connection<EnrichItem>> enrichItems(
		@Source EnrichPipeline enrichPipeline,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean not) {
		return enrichPipelineService.getEnrichItemsConnection(
			enrichPipeline.getId(), after, before, first, last,
			searchText, sortByList, not);
	}

	@Query
	public Uni<EnrichPipeline> getEnrichPipeline(@Id long id) {
		return enrichPipelineService.findById(id);
	}

	public Uni<Response<EnrichPipeline>> patchEnrichPipeline(@Id long id, EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.getValidator().patch(id, enrichPipelineDTO);
	}

	public Uni<Response<EnrichPipeline>> updateEnrichPipeline(@Id long id, EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.getValidator().update(id, enrichPipelineDTO);
	}

	public Uni<Response<EnrichPipeline>> createEnrichPipeline(EnrichPipelineDTO enrichPipelineDTO) {
		return enrichPipelineService.getValidator().create(enrichPipelineDTO);
	}

	@Mutation
	public Uni<Response<EnrichPipeline>> enrichPipeline(
		@Id Long id, EnrichPipelineDTO enrichPipelineDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createEnrichPipeline(enrichPipelineDTO);
		} else {
			return patch
				? patchEnrichPipeline(id, enrichPipelineDTO)
				: updateEnrichPipeline(id, enrichPipelineDTO);
		}

	}

	@Mutation
	public Uni<EnrichPipeline> sortEnrichItems(
		@Id long enrichPipelineId, List<Long> enrichItemIdList) {
		return enrichPipelineService.sortEnrichItems(
			enrichPipelineId, enrichItemIdList);
	}

	@Mutation
	public Uni<EnrichPipeline> deleteEnrichPipeline(@Id long enrichPipelineId) {
		return enrichPipelineService.deleteById(enrichPipelineId);
	}

	@Mutation
	public Uni<Tuple2<EnrichPipeline, EnrichItem>> addEnrichItemToEnrichPipeline(
		@Id long enrichPipelineId, @Id long enrichItemId,
		@DefaultValue("true") boolean tail) {
		return enrichPipelineService.addEnrichItem(
			enrichPipelineId, enrichItemId, tail);
	}

	@Mutation
	public Uni<Tuple2<EnrichPipeline, EnrichItem>> removeEnrichItemFromEnrichPipeline(@Id long enrichPipelineId, @Id long enrichItemId) {
		return enrichPipelineService.removeEnrichItem(enrichPipelineId, enrichItemId);
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