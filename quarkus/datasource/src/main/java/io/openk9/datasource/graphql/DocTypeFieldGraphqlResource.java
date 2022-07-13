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

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeFieldGraphqlResource {

	@Query
	public Uni<List<DocTypeField>> getDocTypeFields(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") String sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return docTypeFieldService.findAll(limit, offset, sortBy, sortType);
	}

	@Query
	public Uni<DocTypeField> getDocTypeField(long id) {
		return docTypeFieldService.findById(id);
	}

	@Mutation
	public Uni<DocTypeField> patchDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {
		return docTypeFieldService.patch(id, docTypeFieldDTO);
	}

	@Mutation
	public Uni<DocTypeField> updateDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {
		return docTypeFieldService.update(id, docTypeFieldDTO);
	}

	@Mutation
	public Uni<DocTypeField> createDocTypeField(DocTypeFieldDTO docTypeFieldDTO) {
		return docTypeFieldService.persist(docTypeFieldDTO);
	}

	@Mutation
	public Uni<DocTypeField> deleteDocTypeField(long docTypeFieldId) {
		return docTypeFieldService.deleteById(docTypeFieldId);
	}

	@Subscription
	public Multi<DocTypeField> docTypeFieldCreated() {
		return docTypeFieldService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocTypeField> docTypeFieldDeleted() {
		return docTypeFieldService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocTypeField> docTypeFieldUpdated() {
		return docTypeFieldService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

}