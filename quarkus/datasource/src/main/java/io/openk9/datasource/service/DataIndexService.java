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
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class DataIndexService extends BaseK9EntityService<DataIndex> {
	 DataIndexService(DataIndexMapper mapper) {
		patchMapper = mapper;
	}

	public Uni<Collection<DocType>> getDocTypes(long dataIndexId) {
		 return findById(dataIndexId)
				 .flatMap(dataIndex -> Mutiny.fetch(dataIndex.getDocTypes()));
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

}
