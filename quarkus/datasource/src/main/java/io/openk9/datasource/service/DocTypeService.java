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
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType, DocTypeDTO> {
	 DocTypeService(DocTypeMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable) {

		 PanacheQuery<DocTypeField> docTypePanacheQuery =
			DataIndex
				.find(
					"#DocType.getDocTypeFields",
					Sort.by(pageable.getSortBy(), pageable.getSortType().getDirection()),
					docTypeId)
				.page(pageable.getLimit(), pageable.getOffset());

		return createPage(
			pageable.getLimit(), pageable.getOffset(), docTypePanacheQuery);
	}

	public Uni<DocTypeField> addDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {

		DocTypeField docTypeField =
			docTypeFieldMapper.create(docTypeFieldDTO);

		return findById(id)
			.flatMap(docType -> {
				docType.addDocTypeField(docTypeField);
				return persist(docType).replaceWith(() -> docTypeField);
			});
	}

	public Uni<Void> removeDocTypeField(long id, long docTypeFieldId) {
		return findById(id)
			.flatMap(docType -> {
				if (!docType.removeDocTypeField(docTypeFieldId)) {
					return Uni.createFrom().failure(() -> new IllegalArgumentException(
						"DocTypeField not found with id " + docTypeFieldId));
				}
				return persist(docType);
			})
			.replaceWithVoid();
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeFieldMapper docTypeFieldMapper;

}
