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
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.service.EmbeddingModelService;
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
public class EmbeddingModelGraphqlResource {

	@Inject
	EmbeddingModelService service;

	@Query
	public Uni<Connection<EmbeddingModel>> getEmbeddingModels(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return service.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<EmbeddingModel> getEmbeddingModel(@Id long id) {
		return service.findById(id);
	}

	@Mutation
	public Uni<EmbeddingModel> enableEmbeddingModel(@Id long id) {
		return service.enable(id);
	}

	@Mutation
	public Uni<Response<EmbeddingModel>> embeddingModel(
		@Id Long id, EmbeddingModelDTO embeddingModelDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createEmbeddingModel(embeddingModelDTO);
		}
		else {
			return patch
				? patchEmbeddingModel(id, embeddingModelDTO)
				: updateEmbeddingModel(id, embeddingModelDTO);
		}

	}

	@Mutation
	public Uni<EmbeddingModel> deleteEmbeddingModel(@Id long embeddingModelId) {
		return service.deleteById(embeddingModelId);
	}

	@Subscription
	public Multi<EmbeddingModel> embeddingModelCreated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EmbeddingModel> embeddingModelDeleted() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<EmbeddingModel> embeddingModelUpdated() {
		return service
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	protected Uni<Response<EmbeddingModel>> patchEmbeddingModel(
		@Id long id, EmbeddingModelDTO embeddingModelDTO) {

		return service.getValidator().patch(id, embeddingModelDTO);
	}

	protected Uni<Response<EmbeddingModel>> updateEmbeddingModel(
		@Id long id, EmbeddingModelDTO embeddingModelDTO) {

		return service.getValidator().update(id, embeddingModelDTO);
	}

	protected Uni<Response<EmbeddingModel>> createEmbeddingModel(
		EmbeddingModelDTO embeddingModelDTO) {

		return service.getValidator().create(embeddingModelDTO);
	}

}
