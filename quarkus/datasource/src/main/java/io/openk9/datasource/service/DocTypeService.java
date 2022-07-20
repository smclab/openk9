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

import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.mapper.DocTypeMapper;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType, DocTypeDTO> {
	 DocTypeService(DocTypeMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable) {
		 return getDocTypeFields(docTypeId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[] { docTypeId },
			"docTypeFields", DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { docTypeId },
			"docTypeFields", DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Tuple2<DocType, DocTypeField>> addDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {

		DocTypeField docTypeField =
			docTypeFieldMapper.create(docTypeFieldDTO);

		return findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> Mutiny.fetch(docType.getDocTypeFields()).flatMap(docTypeFields -> {
				if (docTypeFields.add(docTypeField)) {
					docType.setDocTypeFields(docTypeFields);
					return persist(docType)
						.map(dt -> Tuple2.of(dt, docTypeField));
				}
				return Uni.createFrom().nullItem();
			}));
	}

	public Uni<Tuple2<DocType, Long>> removeDocTypeField(long id, long docTypeFieldId) {
		return findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> Mutiny.fetch(docType.getDocTypeFields()).flatMap(docTypeFields -> {
				if (!docTypeFields.removeIf(docTypeField -> docTypeField.getId() == docTypeFieldId)) {
					return Uni.createFrom().failure(() -> new IllegalArgumentException(
						"DocTypeField not found with id " + docTypeFieldId));
				}
				docType.setDocTypeFields(docTypeFields);
				return persist(docType)
					.map(dt -> Tuple2.of(dt, docTypeFieldId));
			}));
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeFieldMapper docTypeFieldMapper;

	@Override
	public Class<DocType> getEntityClass() {
		return DocType.class;
	}
}
