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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.openk9.datasource.service.RAGConfigurationService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class RAGConfigurationGraphqlResource {

	@Inject
	RAGConfigurationService service;

	@Mutation
	public Uni<RAGConfiguration> deleteRAGConfiguration(@Id long id) {
		return service.deleteById(id);
	}

	@Query
	public Uni<RAGConfiguration> getRagConfiguration(@Id long id) {
		return service.findById(id);
	}

	@Query
	public Uni<Connection<RAGConfiguration>> getRagConfigurations(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return service.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Description("""
		Retrieves a list of RAGConfiguration entities of the specified RAGType 
		that are not yet associated with the given Bucket.
		
		This query returns all RAGConfiguration instances that:
		- Have the specified RAGType.
		- Are not currently linked to the provided bucketId.
		
		Arguments:
		- `bucketId` (ID!): The ID of the Bucket for which to retrieve unbound RAGConfiguration entities.
		- `ragType` (RAGType!): The type of RAGConfiguration to filter by.
		
		Returns:
		- A list of RAGConfiguration entities that match the criteria.
		"""
	)
	@Query
	public Uni<List<RAGConfiguration>> getUnboundRAGConfigurationByBucket(
		@Id long bucketId, @NonNull RAGType ragType) {
		return service.findRAGConfigurationByBucket(bucketId, ragType);
	}

	@Mutation
	public Uni<Response<RAGConfiguration>> ragConfiguration(
		@Id Long id,
		RAGConfigurationDTO ragConfigurationDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createRAGConfiguration(ragConfigurationDTO);
		}
		else {
			return patch
				? patchRAGConfiguration(id, ragConfigurationDTO)
				: updateRAGConfiguration(id, ragConfigurationDTO);
		}
	}

	protected Uni<Response<RAGConfiguration>> createRAGConfiguration(
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().create(ragConfigurationDTO);
	}

	protected Uni<Response<RAGConfiguration>> patchRAGConfiguration(
		@Id long id,
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().patch(id, ragConfigurationDTO);
	}

	protected Uni<Response<RAGConfiguration>> updateRAGConfiguration(
		@Id long id,
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().update(id, ragConfigurationDTO);
	}
}
