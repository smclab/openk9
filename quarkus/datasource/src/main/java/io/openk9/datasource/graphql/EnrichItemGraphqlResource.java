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

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.dto.EnrichItemDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.service.EnrichItemService;
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
public class EnrichItemGraphqlResource {

	@Query
	public Uni<Page<EnrichItem>> getEnrichItems(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") String sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return enrichItemService.findAllPaginated(limit, offset, sortBy, sortType);
	}

	@Query
	public Uni<EnrichItem> getEnrichItem(long id) {
		return enrichItemService.findById(id);
	}

	@Mutation
	public Uni<EnrichItem> patchEnrichItem(long id, EnrichItemDTO enrichItemDTO) {
		return enrichItemService.patch(id, enrichItemDTO);
	}

	@Mutation
	public Uni<EnrichItem> updateEnrichItem(long id, EnrichItemDTO enrichItemDTO) {
		return enrichItemService.update(id, enrichItemDTO);
	}

	@Mutation
	public Uni<EnrichItem> createEnrichItem(EnrichItemDTO enrichItemDTO) {
		return enrichItemService.persist(enrichItemDTO);
	}

	@Mutation
	public Uni<EnrichItem> deleteEnrichItem(long enrichItemId) {
		return enrichItemService.deleteById(enrichItemId);
	}

	@Subscription
	public Multi<EnrichItem> enrichItemCreated() {
		return enrichItemService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EnrichItem> enrichItemDeleted() {
		return enrichItemService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EnrichItem> enrichItemUpdated() {
		return enrichItemService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	EnrichItemService enrichItemService;

}