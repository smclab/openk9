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

import io.openk9.datasource.mapper.DocTypeMapper;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType> {
	 DocTypeService(DocTypeMapper mapper) {
		patchMapper = mapper;
	}

	public Uni<Collection<DocTypeField>> getDocTypeFields(long docTypeId) {
		 return findById(docTypeId)
			 .flatMap(docType -> Mutiny.fetch(docType.getDocTypeFields()));
	}

	public Uni<DocTypeField> addDocTypeField(long id, DocTypeField docTypeField) {

		docTypeField.id = null;

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

}
