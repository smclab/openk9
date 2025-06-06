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

import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.request.AnalyzerWithListsDTO;
import io.openk9.datasource.service.AnalyzerService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;

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

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class AnalyzerGraphqlResource {

	@Query
	public Uni<Connection<Analyzer>> getAnalyzers(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _analyzerService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Analyzer> getAnalyzer(@Id long id) {
		return _analyzerService.findById(id);
	}

	@Query
	public Uni<List<Analyzer>> getUnboundAnalyzersByTokenFilter(long tokenFilterId) {
		return _analyzerService.findUnboundAnalyzersByTokenFilter(tokenFilterId);
	}

	@Query
	public Uni<List<Analyzer>> getUnboundAnalyzersByCharFilter(long charFilterId) {
		return _analyzerService.findUnboundAnalyzersByCharFilter(charFilterId);
	}

	public Uni<Connection<TokenFilter>> tokenFilters(
		@Source Analyzer analyzer,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return _analyzerService.getTokenFilters(
			analyzer.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Connection<CharFilter>> charFilters(
		@Source Analyzer analyzer,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return _analyzerService.getCharFilters(
			analyzer.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Tokenizer> tokenizer(@Source Analyzer analyzer) {
		return _analyzerService.getTokenizer(analyzer.getId());
	}

	@Mutation
	public Uni<Tuple2<Analyzer, Tokenizer>> bindTokenizerToAnalyzer(
		@Id long analyzerId, @Id long tokenizerId) {
		return _analyzerService.bindTokenizer(analyzerId, tokenizerId);
	}

	@Mutation
	public Uni<Tuple2<Analyzer, Tokenizer>> unbindTokenizerFromAnalyzer(
		@Id long analyzerId) {
		return _analyzerService.unbindTokenizer(analyzerId);
	}

	public Uni<Response<Analyzer>> patchAnalyzer(@Id long id, AnalyzerDTO analyzerDTO) {
		return _analyzerService.getValidator().patch(id, analyzerDTO);
	}

	public Uni<Response<Analyzer>> updateAnalyzer(@Id long id, AnalyzerDTO analyzerDTO) {
		return _analyzerService.getValidator().update(id, analyzerDTO);
	}

	public Uni<Response<Analyzer>> createAnalyzer(AnalyzerDTO analyzerDTO) {
		return _analyzerService.getValidator().create(analyzerDTO);
	}

	@Mutation
	public Uni<Tuple2<Analyzer, TokenFilter>> addTokenFilterToAnalyzer(
		@Id long id, @Id long tokenFilterId) {
		return _analyzerService.addTokenFilterToAnalyzer(id, tokenFilterId);
	}

	@Mutation
	public Uni<Analyzer> removeTokenFilterListFromAnalyzer(
		long analyzerId) {
		return _analyzerService.removeTokenFilterListFromAnalyzer(analyzerId);
	}

	@Mutation
	public Uni<Analyzer> removeCharFilterListFromAnalyzer(long analyzerId){
		return _analyzerService.removeCharFilterListFromAnalyzer(analyzerId);
	}

	@Mutation
	public Uni<Response<Analyzer>> analyzerWithLists(
			@Id Long id, AnalyzerWithListsDTO analyzerWithListsDTO,
			@DefaultValue("false") boolean patch) {

		return analyzer(id, analyzerWithListsDTO, patch);

	}

	@Mutation
	public Uni<Tuple2<Analyzer, CharFilter>> addCharFilterToAnalyzer(
		@Id long id, @Id long charFilterId) {
		return _analyzerService.addCharFilterToAnalyzer(id, charFilterId);
	}

	@Mutation
	public Uni<Tuple2<Analyzer, CharFilter>> removeCharFilterFromAnalyzer(
		@Id long id, @Id long charFilterId) {
		return _analyzerService.removeCharFilterFromAnalyzer(id, charFilterId);
	}


	@Mutation
	public Uni<Response<Analyzer>> analyzer(
		@Id Long id, AnalyzerDTO analyzerDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createAnalyzer(analyzerDTO);
		} else {
			return patch
				? patchAnalyzer(id, analyzerDTO)
				: updateAnalyzer(id, analyzerDTO);
		}

	}

	@Mutation
	public Uni<Tuple2<Analyzer, TokenFilter>> removeTokenFilterFromAnalyzer(
		@Id long id, @Id long tokenFilterId) {
		return _analyzerService.removeTokenFilterFromAnalyzer(id, tokenFilterId);
	}

	@Mutation
	public Uni<Analyzer> deleteAnalyzer(@Id long analyzerId) {
		return _analyzerService.deleteById(analyzerId);
	}


	@Subscription
	public Multi<Analyzer> analyzerCreated() {
		return _analyzerService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Analyzer> analyzerDeleted() {
		return _analyzerService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Analyzer> analyzerUpdated() {
		return _analyzerService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	AnalyzerService _analyzerService;
}
