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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.service.QueryAnalysisService;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class QueryAnalysisGraphqlResource {

	@Query
	public Uni<Connection<QueryAnalysis>> getQueryAnalyses(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return queryAnalysisService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<QueryAnalysis> getQueryAnalysis(@Id long id) {
		return queryAnalysisService.findById(id);
	}


	public Uni<Connection<Annotator>> annotators(
		@Source QueryAnalysis queryAnalysis,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return queryAnalysisService.getAnnotators(
			queryAnalysis.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Connection<Rule>> rules(
		@Source QueryAnalysis queryAnalysis,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return queryAnalysisService.getRules(
			queryAnalysis.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Response<QueryAnalysis>> patchQueryAnalysis(@Id long id, QueryAnalysisDTO queryAnalysisDTO) {
		return queryAnalysisService.getValidator().patch(id, queryAnalysisDTO);
	}

	public Uni<Response<QueryAnalysis>> updateQueryAnalysis(@Id long id, QueryAnalysisDTO queryAnalysisDTO) {
		return queryAnalysisService.getValidator().update(id, queryAnalysisDTO);
	}

	public Uni<Response<QueryAnalysis>> createQueryAnalysis(QueryAnalysisDTO queryAnalysisDTO) {
		return queryAnalysisService.getValidator().create(queryAnalysisDTO);
	}

	@Mutation
	public Uni<Tuple2<QueryAnalysis, Rule>> addRuleToQueryAnalysis(
		@Id long id, @Id long ruleId) {
		return queryAnalysisService.addRuleToQueryAnalysis(id, ruleId);
	}

	@Mutation
	public Uni<Tuple2<QueryAnalysis, Rule>> removeRuleFromQueryAnalysis(
		@Id long id, @Id long ruleId) {
		return queryAnalysisService.removeRuleToQueryAnalysis(id, ruleId);
	}

	@Mutation
	public Uni<Tuple2<QueryAnalysis, Annotator>> addAnnotatorToQueryAnalysis(
			@Id long id, @Id long annotatorId) {
		return queryAnalysisService.addAnnotatorToQueryAnalysis(id, annotatorId);
	}

	@Mutation
	public Uni<Tuple2<QueryAnalysis, Annotator>> removeAnnotatorFromQueryAnalysis(
			@Id long id, @Id long annotatorId) {
		return queryAnalysisService.removeAnnotatorToQueryAnalysis(id, annotatorId);
	}



	@Mutation
	public Uni<Response<QueryAnalysis>> queryAnalysis(
		@Id Long id, QueryAnalysisDTO queryAnalysisDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createQueryAnalysis(queryAnalysisDTO);
		} else {
			return patch
				? patchQueryAnalysis(id, queryAnalysisDTO)
				: updateQueryAnalysis(id, queryAnalysisDTO);
		}

	}

	@Mutation
	public Uni<QueryAnalysis> deleteQueryAnalysis(@Id long queryAnalysisId) {
		return queryAnalysisService.deleteById(queryAnalysisId);
	}


	@Subscription
	public Multi<QueryAnalysis> queryAnalysisCreated() {
		return queryAnalysisService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<QueryAnalysis> queryAnalysisDeleted() {
		return queryAnalysisService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<QueryAnalysis> queryAnalysisUpdated() {
		return queryAnalysisService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	QueryAnalysisService queryAnalysisService;

}