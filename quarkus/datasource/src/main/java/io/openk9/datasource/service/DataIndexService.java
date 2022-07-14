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

import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DataIndexService extends BaseK9EntityService<DataIndex, DataIndexDTO> {
	DataIndexService(DataIndexMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable) {

		Map<String, Object> params = new HashMap<>();

		params.put("dataIndexId", dataIndexId);

		String query =
			"select dt " +
			"from DataIndex dataIndex " +
			"join dataIndex.docTypes dt" +
			"where dataIndex.id = :dataIndexId ";

		query = createPageableQuery(pageable, params, query, "dt");

		Sort sort = createSort("dt", pageable.getSortBy().name());

		PanacheQuery<DocType> docTypePanacheQuery =
			DataIndex
				.find(query, sort, params)
				.page(0, pageable.getLimit());

		Uni<Long> countQuery =
			DocType.count("from DataIndex dataIndex join dataIndex.docTypes where dataIndex.id = ?1", dataIndexId);

		return createPage(
			pageable.getLimit(), docTypePanacheQuery, countQuery);
	}

	public Uni<Void> addDocType(long dataIndexId, long docTypeId) {
		 return findById(dataIndexId)
			 .flatMap(dataIndex ->
				 docTypeService.findById(docTypeId)
					 .flatMap(docType -> {
						 dataIndex.addDocType(docType);
						 return persist(dataIndex);
					 })
			 )
			 .replaceWithVoid();
	}

	public Uni<Void> removeDocType(long dataIndexId, long docTypeId) {
		 return findById(dataIndexId)
			 .flatMap(dataIndex ->
				 docTypeService.findById(docTypeId)
					 .flatMap(docType -> {
						 dataIndex.removeDocType(docType);
						 return persist(dataIndex);
					 })
			 )
			 .replaceWithVoid();
	}

	@Inject
	DocTypeService docTypeService;

	@Override
	public Class<DataIndex> getEntityClass() {
		return DataIndex.class;
	}
}
