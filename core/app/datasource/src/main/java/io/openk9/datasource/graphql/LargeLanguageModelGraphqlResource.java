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
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.dto.LargeLanguageModelDTO;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.datasource.service.util.K9EntityEvent;
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

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class LargeLanguageModelGraphqlResource {

	@Inject
	LargeLanguageModelService service;

	@Query
	public Uni<Connection<LargeLanguageModel>> getLargeLanguageModels(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return service.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<LargeLanguageModel> getLargeLanguageModel(@Id long id) {
		return service.findById(id);
	}

	@Mutation
	public Uni<LargeLanguageModel> enableLargeLanguageModel(@Id long id) {
		return service.enable(id);
	}

	@Mutation
	public Uni<Response<LargeLanguageModel>> largeLanguageModel(
		@Id Long id,
		LargeLanguageModelDTO largeLanguageModelDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createLargeLanguageModel(largeLanguageModelDTO);
		}
		else {
			return patch
				? patchLargeLanguageModel(id, largeLanguageModelDTO)
				: updateLargeLanguageModel(id, largeLanguageModelDTO);
		}

	}

	@Mutation
	public Uni<LargeLanguageModel> deleteLargeLanguageModel(@Id long largeLanguageModelId) {
		return service.deleteById(largeLanguageModelId);
	}

	@Subscription
	public Multi<LargeLanguageModel> largeLanguageModelCreated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<LargeLanguageModel> largeLanguageModelDeleted() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<LargeLanguageModel> largeLanguageModelUpdated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	protected Uni<Response<LargeLanguageModel>> patchLargeLanguageModel(
		@Id long id, LargeLanguageModelDTO largeLanguageModelDTO) {

		return service.getValidator().patch(id, largeLanguageModelDTO);
	}

	protected Uni<Response<LargeLanguageModel>> updateLargeLanguageModel(
		@Id long id, LargeLanguageModelDTO largeLanguageModelDTO) {

		return service.getValidator().update(id, largeLanguageModelDTO);
	}

	protected Uni<Response<LargeLanguageModel>> createLargeLanguageModel(
		LargeLanguageModelDTO largeLanguageModelDTO) {

		return service.getValidator().create(largeLanguageModelDTO);
	}

}
