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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.QueryAnalysisMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class QueryAnalysisService extends BaseK9EntityService<QueryAnalysis, QueryAnalysisDTO> {
	 QueryAnalysisService(QueryAnalysisMapper mapper) {
		 this.mapper = mapper;
	}


	public Uni<Connection<Annotator>> getAnnotators(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, QueryAnalysis_.ANNOTATORS, Annotator.class,
			annotatorService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Connection<Rule>> getRules(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, QueryAnalysis_.RULES, Rule.class,
			ruleService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	@Override
	public Class<QueryAnalysis> getEntityClass() {
		return QueryAnalysis.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
	}





	public Uni<Tuple2<QueryAnalysis, Rule>> addRuleToQueryAnalysis(
		long id, long ruleId) {

		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(queryAnalysis -> ruleService.findById(ruleId)
				.onItem()
				.ifNotNull()
				.transformToUni(rule ->
					Mutiny2.fetch(s, queryAnalysis.getRules())
						.onItem()
						.ifNotNull()
						.transformToUni(rules -> {

							if (rules.add(rule)) {

								queryAnalysis.setRules(rules);

								return persist(queryAnalysis)
									.map(newSC -> Tuple2.of(newSC, rule));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<QueryAnalysis, Rule>> removeRuleToQueryAnalysis(
		long id, long ruleId) {
		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(queryAnalysis -> Mutiny2.fetch(s, queryAnalysis.getRules())
				.onItem()
				.ifNotNull()
				.transformToUni(rules -> {

					if (queryAnalysis.removeRule(rules, ruleId)) {

						return persist(queryAnalysis)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<QueryAnalysis, Annotator>> addAnnotatorToQueryAnalysis(long id, long annotatorId) {
		return withTransaction((s, tr) -> findById(id)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> annotatorService.findById(annotatorId)
						.onItem()
						.ifNotNull()
						.transformToUni(annotator ->
								Mutiny2.fetch(s, queryAnalysis.getAnnotators())
										.onItem()
										.ifNotNull()
										.transformToUni(annotators -> {

											if (annotators.add(annotator)) {

												queryAnalysis.setAnnotators(annotators);

												return persist(queryAnalysis)
														.map(newSC -> Tuple2.of(newSC, annotator));
											}

											return Uni.createFrom().nullItem();

										})
						)
				));
	}

	public Uni<Tuple2<QueryAnalysis, Annotator>> removeAnnotatorToQueryAnalysis(long id, long annotatorId) {

		return withTransaction((s, tr) -> findById(id)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> Mutiny2.fetch(s, queryAnalysis.getAnnotators())
						.onItem()
						.ifNotNull()
						.transformToUni(annotators -> {

							if (queryAnalysis.removeAnnotators(annotators, annotatorId)) {

								return persist(queryAnalysis)
										.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})));
	}

	@Inject
	AnnotatorService annotatorService;

	@Inject
	RuleService ruleService;




}
