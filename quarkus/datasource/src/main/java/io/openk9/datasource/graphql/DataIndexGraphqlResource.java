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
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.DataIndexService;
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
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DataIndexGraphqlResource {

	@Query
	public Uni<Connection<DataIndex>> getDataIndices(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return dataIndexService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Page<DocType>> docTypes(
		@Source DataIndex dataIndex, Pageable pageable, String searchText) {
		return dataIndexService.getDocTypes(
			dataIndex.getId(), pageable == null ? Pageable.DEFAULT : pageable, searchText);
	}

	@Query
	public Uni<DataIndex> getDataIndex(long id) {
		return dataIndexService.findById(id);
	}

	public Uni<Response<DataIndex>> patchDataIndex(long id, DataIndexDTO dataIndexDTO) {
		return dataIndexService.getValidator().patch(id, dataIndexDTO);
	}

	public Uni<Response<DataIndex>> updateDataIndex(long id, DataIndexDTO dataIndexDTO) {
		return dataIndexService.getValidator().update(id, dataIndexDTO);
	}

	public Uni<Response<DataIndex>> createDataIndex(DataIndexDTO dataIndexDTO) {
		return dataIndexService.getValidator().create(dataIndexDTO);
	}

	@Mutation
	public Uni<Response<DataIndex>> dataIndex(
		Long id, DataIndexDTO dataIndexDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createDataIndex(dataIndexDTO);
		} else {
			return patch
				? patchDataIndex(id, dataIndexDTO)
				: updateDataIndex(id, dataIndexDTO);
		}

	}

	@Mutation
	public Uni<DataIndex> deleteDataIndex(long dataIndexId) {
		return dataIndexService.deleteById(dataIndexId);
	}

	@Mutation
	public Uni<Tuple2<DataIndex, DocType>> addDocTypeToDataIndex(long dataIndexId, long docTypeId) {
		return dataIndexService.addDocType(dataIndexId, docTypeId);
	}

	@Mutation
	public Uni<Tuple2<DataIndex, DocType>> removeDocTypeFromDataIndex(long dataIndexId, long docTypeId) {
		return dataIndexService.removeDocType(dataIndexId, docTypeId);
	}

	@Subscription
	public Multi<DataIndex> dataIndexCreated() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DataIndex> dataIndexDeleted() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DataIndex> dataIndexUpdated() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DataIndexService dataIndexService;

}