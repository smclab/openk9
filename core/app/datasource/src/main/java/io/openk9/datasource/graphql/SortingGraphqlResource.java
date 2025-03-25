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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.dto.base.SortingDTO;
import io.openk9.datasource.service.SortingService;
import io.openk9.datasource.service.TranslationService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class SortingGraphqlResource {

	@Query
	public Uni<Sorting> getSorting(@Id long id) {
		return sortingService.findById(id);
	}

	@Query
	public Uni<Connection<Sorting>> getTotalSortings(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return sortingService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<DocTypeField> docTypeField(@Source Sorting sorting) {
		return sortingService.getDocTypeField(sorting);
	}

	@Mutation
	public Uni<Tuple2<Sorting, DocTypeField>> bindDocTypeFieldToSorting(
		@Id long sortingId, @Id long docTypeFieldId) {
		return sortingService.bindDocTypeFieldToSorting(sortingId, docTypeFieldId);
	}

	@Mutation
	public Uni<Tuple2<Sorting, DocTypeField>> unbindDocTypeFieldFromSorting(
		@Id long id, @Id long docTypeFieldId) {
		return sortingService.unbindDocTypeFieldFromSorting(id, docTypeFieldId);
	}

	@Mutation
	public Uni<Response<Sorting>> sorting(
		@Id Long id, SortingDTO sortingDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createSorting(sortingDTO);
		} else {
			return patch
				? sorting(id, sortingDTO)
				: updateSorting(id, sortingDTO);
		}

	}

	public Uni<Response<Sorting>> createSorting(SortingDTO sortingDTO) {
		return sortingService.getValidator().create(sortingDTO);
	}

	public Uni<Response<Sorting>> sorting(@Id long id, SortingDTO sortingDTO) {
		return sortingService.getValidator().patch(id, sortingDTO);
	}

	public Uni<Response<Sorting>> updateSorting(@Id long id, SortingDTO sortingDTO) {
		return sortingService.getValidator().update(id, sortingDTO);
	}

	public Uni<Connection<DocTypeField>> docTypeFieldsNotInSorting(
		@Source Sorting sorting,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return sortingService.getDocTypeFieldsNotInSorting(
			sorting.getId(), after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInSorting(
		@Id long sortingId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return sortingService.getDocTypeFieldsNotInSorting(
			sortingId, after, before, first, last, searchText, sortByList);
	}

	@Mutation
	public Uni<Tuple2<String, String>> addSortingTranslation(
		@Id @Name("sortingId") long sortingId,
		String language, String key, String value) {

		return translationService
			.addTranslation(Sorting.class, sortingId, language, key, value)
			.map((__) -> Tuple2.of("ok", null));
	}

	@Mutation
	public Uni<Tuple2<String, String>> deleteSortingTranslation(
		@Id @Name("sortingId") long sortingId,
		String language, String key) {

		return translationService
			.deleteTranslation(Sorting.class, sortingId, language, key)
			.map((__) -> Tuple2.of("ok", null));
	}

	@Inject
	SortingService sortingService;

	@Inject
	TranslationService translationService;
}
