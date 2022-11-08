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
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.AnnotatorDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.AnnotatorService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.validation.Response;
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
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class AnnotatorGraphqlResource {

	@Query
	public Uni<Connection<Annotator>> getAnnotators(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return annotatorService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Connection<DocTypeField>> docTypeFieldNotInAnnotator(
		@Source Annotator annotator,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return annotatorService.getDocTypeFieldsNotInAnnotator(
			annotator.getId(), after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldNotInAnnotator(
		@Id long annotatorId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return annotatorService.getDocTypeFieldsNotInAnnotator(
			annotatorId, after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Annotator> getAnnotator(@Id long id) {
		return annotatorService.findById(id);
	}

	public Uni<DocTypeField> docTypeField(@Source Annotator annotator) {
		return transactionInvoker.withTransaction(s -> Mutiny2.fetch(s, annotator.getDocTypeField()));
	}

	public Uni<Response<Annotator>> patchAnnotator(@Id long id, AnnotatorDTO annotatorDTO) {
		return annotatorService.getValidator().patch(id, annotatorDTO);
	}

	public Uni<Response<Annotator>> updateAnnotator(@Id long id, AnnotatorDTO annotatorDTO) {
		return annotatorService.getValidator().update(id, annotatorDTO);
	}

	public Uni<Response<Annotator>> createAnnotator(AnnotatorDTO annotatorDTO) {
		return annotatorService.getValidator().create(annotatorDTO);
	}

	@Mutation
	public Uni<Tuple2<Annotator, DocTypeField>> bindAnnotatorToDocTypeField(
		@Id long id, @Id long docTypeFieldId) {
		return annotatorService.bindDocTypeField(id, docTypeFieldId);
	}

	@Mutation
	public Uni<Tuple2<Annotator, DocTypeField>> unbindAnnotatorFromDocTypeField(
		@Id long id, @Id long docTypeFieldId) {
		return annotatorService.unbindDocTypeField(id, docTypeFieldId);
	}

	@Mutation
	public Uni<Response<Annotator>> annotator(
		@Id Long id, AnnotatorDTO annotatorDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createAnnotator(annotatorDTO);
		} else {
			return patch
				? patchAnnotator(id, annotatorDTO)
				: updateAnnotator(id, annotatorDTO);
		}

	}

	@Mutation
	public Uni<Annotator> deleteAnnotator(@Id long annotatorId) {
		return annotatorService.deleteById(annotatorId);
	}

	@Subscription
	public Multi<Annotator> annotatorCreated() {
		return annotatorService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Annotator> annotatorDeleted() {
		return annotatorService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Annotator> annotatorUpdated() {
		return annotatorService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	AnnotatorService annotatorService;

}