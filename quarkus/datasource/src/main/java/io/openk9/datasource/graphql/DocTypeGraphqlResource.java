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

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeGraphqlResource {

	@Query
	public Uni<Page<DocType>> getDocTypes(
		Filter filter, Pageable pageable) {
		return docTypeService.findAllPaginated(
			pageable == null ? Pageable.DEFAULT : pageable, filter
		);
	}

	@Query
	public Uni<Page<DocTypeField>> docTypeFields(
		@Source DocType docType,
		Pageable pageable, Filter filter) {
		return docTypeService.getDocTypeFields(
			docType.getId(),
			pageable == null ? Pageable.DEFAULT : pageable, filter);
	}

	@Query
	public Uni<DocType> getDocType(long id) {
		return docTypeService.findById(id);
	}

	@Mutation
	public Uni<DocType> patchDocType(long id, DocTypeDTO docTypeDTO) {
		return docTypeService.patch(id, docTypeDTO);
	}

	@Mutation
	public Uni<DocType> updateDocType(long id, DocTypeDTO docTypeDTO) {
		return docTypeService.update(id, docTypeDTO);
	}

	@Mutation
	public Uni<DocType> createDocType(DocTypeDTO docTypeDTO) {
		return docTypeService.persist(docTypeDTO);
	}

	@Mutation
	public Uni<DocType> deleteDocType(long docTypeId) {
		return docTypeService.deleteById(docTypeId);
	}

	@Mutation
	public Uni<Tuple2<DocType, DocTypeField>> createDocTypeField(
		long docTypeId, DocTypeFieldDTO docTypeFieldDTO) {
		return docTypeService.addDocTypeField(docTypeId, docTypeFieldDTO);
	}

	@Mutation
	public Uni<Tuple2<DocType, Long>> removeDocTypeField(
		long docTypeId, long docTypeFieldId) {
		return docTypeService.removeDocTypeField(docTypeId, docTypeFieldId);
	}

	@Subscription
	public Multi<DocType> docTypeCreated() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocType> docTypeDeleted() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocType> docTypeUpdated() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DocTypeService docTypeService;

}