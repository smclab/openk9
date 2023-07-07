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

package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class DataIndexService
	extends BaseK9EntityService<DataIndex, DataIndexDTO> {

	DataIndexService(DataIndexMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{DataIndex_.NAME, DataIndex_.DESCRIPTION};
	}

	public Uni<Set<DocType>> getDocTypes(
		DataIndex dataIndex) {
		return withTransaction(s -> Mutiny2.fetch(s, dataIndex.getDocTypes()));
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable) {
		return getDocTypes(dataIndexId, pageable, Filter.DEFAULT);
	}

	public Uni<Connection<DocType>> getDocTypesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean not) {
		return findJoinConnection(
			id, DataIndex_.DOC_TYPES, DocType.class,
			docTypeService.getSearchFields(),
			after, before, first, last, searchText, sortByList, not);
	}

	public Uni<Long> getCountIndexDocuments(String name) {
		return indexService.indexCount(name);
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{dataIndexId}, DataIndex_.DOC_TYPES, DocType.class,
			pageable.getLimit(),
			pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{dataIndexId}, DataIndex_.DOC_TYPES, DocType.class,
			pageable.getLimit(),
			pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), filter);
	}

	public Uni<Tuple2<DataIndex, DocType>> addDocType(
		long dataIndexId, long docTypeId) {
		return withTransaction((s) -> findById(dataIndexId)
			.onItem()
			.ifNotNull()
			.transformToUni(dataIndex ->
				docTypeService.findById(docTypeId)
					.onItem()
					.ifNotNull()
					.transformToUni(
						docType -> Mutiny2.fetch(s, dataIndex.getDocTypes())
							.flatMap(dts -> {
								if (dts.add(docType)) {
									dataIndex.setDocTypes(dts);
									return create(dataIndex)
										.map(di -> Tuple2.of(di, docType));
								}
								return Uni.createFrom().nullItem();
							})
					)
			));
	}

	public Uni<Tuple2<DataIndex, DocType>> removeDocType(
		long dataIndexId, long docTypeId) {
		return withTransaction((s) -> findById(dataIndexId)
			.onItem()
			.ifNotNull()
			.transformToUni(dataIndex ->
				docTypeService.findById(docTypeId)
					.onItem()
					.ifNotNull()
					.transformToUni(
						docType -> Mutiny2.fetch(s, dataIndex.getDocTypes())
							.flatMap(dts -> {
								if (dts.remove(docType)) {
									dataIndex.setDocTypes(dts);
									return create(dataIndex)
										.map(di -> Tuple2.of(di, docType));
								}
								return Uni.createFrom().nullItem();
							})
					)
			));
	}

	@Override
	public Class<DataIndex> getEntityClass() {
		return DataIndex.class;
	}

	@Override
	public Uni<DataIndex> deleteById(long entityId) {
		return findById(entityId)
			.onItem()
			.transformToUni(dataIndex -> Uni.createFrom()
				.<AcknowledgedResponse>emitter(emitter -> {
					DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(dataIndex.getName());
					client.indices().deleteAsync(
						deleteIndexRequest,
						RequestOptions.DEFAULT,
						UniActionListener.of(emitter));
				})
			)
			.onItem()
			.transformToUni(acknowledgedResponse -> super.deleteById(entityId));
	}

	@Inject
	DocTypeService docTypeService;

	@Inject
	IndexService indexService;

	@Inject
	RestHighLevelClient client;

}
