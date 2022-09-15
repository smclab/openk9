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
import io.openk9.datasource.model.StopWord;
import io.openk9.datasource.model.dto.StopWordDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.StopWordService;
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
public class StopWordGraphqlResource {

	@Query
	public Uni<Connection<StopWord>> getStopWords(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return stopWordService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<StopWord> getStopWord(@Id long id) {
		return stopWordService.findById(id);
	}

	public Uni<Response<StopWord>> patchStopWord(@Id long id, StopWordDTO stopWordDTO) {
		return stopWordService.getValidator().patch(id, stopWordDTO);
	}

	public Uni<Response<StopWord>> updateStopWord(@Id long id, StopWordDTO stopWordDTO) {
		return stopWordService.getValidator().update(id, stopWordDTO);
	}

	public Uni<Response<StopWord>> createStopWord(StopWordDTO stopWordDTO) {
		return stopWordService.getValidator().create(stopWordDTO);
	}

	@Mutation
	public Uni<Response<StopWord>> stopWord(
		@Id Long id, StopWordDTO stopWordDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createStopWord(stopWordDTO);
		} else {
			return patch
				? patchStopWord(id, stopWordDTO)
				: updateStopWord(id, stopWordDTO);
		}

	}

	@Mutation
	public Uni<StopWord> deleteStopWord(@Id long stopWordId) {
		return stopWordService.deleteById(stopWordId);
	}

	@Subscription
	public Multi<StopWord> stopWordCreated() {
		return stopWordService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<StopWord> stopWordDeleted() {
		return stopWordService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<StopWord> stopWordUpdated() {
		return stopWordService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	StopWordService stopWordService;

}