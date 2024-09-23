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
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.openk9.datasource.service.VectorIndexService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class VectorIndexGraphqlResource {

	@Inject
	VectorIndexService service;

	@Query
	public Uni<Connection<VectorIndex>> getVectorIndices(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return service.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<VectorIndex> getVectorIndex(@Id long id) {
		return service.findById(id);
	}

	@Mutation
	public Uni<Response<VectorIndex>> vectorIndex(
		@Id Long id, VectorIndexDTO vectorIndexDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createVectorIndex(vectorIndexDTO);
		}
		else {
			return patch
				? patchVectorIndex(id, vectorIndexDTO)
				: updateVectorIndex(id, vectorIndexDTO);
		}

	}

	@Mutation
	public Uni<VectorIndex> deleteVectorIndex(@Id long vectorIndexId) {
		return service.deleteById(vectorIndexId);
	}

	@Subscription
	public Multi<VectorIndex> vectorIndexCreated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<VectorIndex> vectorIndexDeleted() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<VectorIndex> vectorIndexUpdated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	protected Uni<Response<VectorIndex>> patchVectorIndex(
		@Id long id, VectorIndexDTO vectorIndexDTO) {

		return service.getValidator().patch(id, vectorIndexDTO);
	}

	protected Uni<Response<VectorIndex>> updateVectorIndex(
		@Id long id, VectorIndexDTO vectorIndexDTO) {

		return service.getValidator().update(id, vectorIndexDTO);
	}

	protected Uni<Response<VectorIndex>> createVectorIndex(
		VectorIndexDTO vectorIndexDTO) {

		return service.getValidator().create(vectorIndexDTO);
	}

}
