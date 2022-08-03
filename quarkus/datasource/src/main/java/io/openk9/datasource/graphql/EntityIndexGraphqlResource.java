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
import io.openk9.datasource.model.EntityIndex;
import io.openk9.datasource.model.dto.EntityIndexDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.EntityIndexService;
import io.openk9.datasource.service.util.K9EntityEvent;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class EntityIndexGraphqlResource {

	@Query
	public Uni<Connection<EntityIndex>> getEntityIndices(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return entityIndexService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<EntityIndex> getEntityIndex(@Id long id) {
		return entityIndexService.findById(id);
	}

	public Uni<Response<EntityIndex>> patchEntityIndex(@Id long id, EntityIndexDTO entityIndexDTO) {
		return entityIndexService.getValidator().patch(id, entityIndexDTO);
	}

	public Uni<Response<EntityIndex>> updateEntityIndex(@Id long id, EntityIndexDTO entityIndexDTO) {
		return entityIndexService.getValidator().update(id, entityIndexDTO);
	}

	public Uni<Response<EntityIndex>> createEntityIndex(EntityIndexDTO entityIndexDTO) {
		return entityIndexService.getValidator().create(entityIndexDTO);
	}

	@Mutation
	public Uni<Response<EntityIndex>> entityIndex(
		@Id Long id, EntityIndexDTO entityIndexDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createEntityIndex(entityIndexDTO);
		} else {
			return patch
				? patchEntityIndex(id, entityIndexDTO)
				: updateEntityIndex(id, entityIndexDTO);
		}

	}

	@Mutation
	public Uni<EntityIndex> deleteEntityIndex(@Id long entityIndexId) {
		return entityIndexService.deleteById(entityIndexId);
	}

	@Subscription
	public Multi<EntityIndex> entityIndexCreated() {
		return entityIndexService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EntityIndex> entityIndexDeleted() {
		return entityIndexService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EntityIndex> entityIndexUpdated() {
		return entityIndexService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	EntityIndexService entityIndexService;

}