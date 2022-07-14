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

import io.openk9.datasource.model.EntityIndex;
import io.openk9.datasource.model.dto.EntityIndexDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.EntityIndexService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class EntityIndexGraphqlResource {

	@Query
	public Uni<Page<EntityIndex>> getEntityIndices(
		Pageable pageable) {
		return entityIndexService.findAllPaginated(
			pageable == null ? Pageable.DEFAULT : pageable
		);
	}

	@Query
	public Uni<EntityIndex> getEntityIndex(long id) {
		return entityIndexService.findById(id);
	}

	@Mutation
	public Uni<EntityIndex> patchEntityIndex(long id, EntityIndexDTO entityIndexDTO) {
		return entityIndexService.patch(id, entityIndexDTO);
	}

	@Mutation
	public Uni<EntityIndex> updateEntityIndex(long id, EntityIndexDTO entityIndexDTO) {
		return entityIndexService.update(id, entityIndexDTO);
	}

	@Mutation
	public Uni<EntityIndex> createEntityIndex(EntityIndexDTO entityIndexDTO) {
		return entityIndexService.persist(entityIndexDTO);
	}

	@Mutation
	public Uni<EntityIndex> deleteEntityIndex(long entityIndexId) {
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