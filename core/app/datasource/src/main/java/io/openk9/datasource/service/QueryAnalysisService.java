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

import java.util.HashSet;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.QueryAnalysisMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.base.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.request.QueryAnalysisWithListsDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

;

@ApplicationScoped
public class QueryAnalysisService extends BaseK9EntityService<QueryAnalysis, QueryAnalysisDTO> {
	 QueryAnalysisService(QueryAnalysisMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Uni<QueryAnalysis> create(QueryAnalysisDTO dto) {
		if (dto instanceof QueryAnalysisWithListsDTO withListsDTO) {

			var transientQueryAnalysis = mapper.create(withListsDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientQueryAnalysis)
					.flatMap(queryAnalysis -> {

						//UniBuilder to prevent empty unis
						UniJoin.Builder<QueryAnalysis> builder = Uni.join().builder();
						builder.add(Uni.createFrom().item(queryAnalysis));

						//annotators
						var annotatorsIds = withListsDTO.getAnnotatorsIds();

						if (annotatorsIds != null && !annotatorsIds.isEmpty()) {

							var annotatorUni = s.find(Annotator.class, annotatorsIds.toArray())
								.flatMap(annotators -> {
									queryAnalysis.setAnnotators(new HashSet<>(annotators));
									return Uni.createFrom().item(queryAnalysis);
								});

							builder.add(annotatorUni);

						}

						//rules
						var rulesIds = withListsDTO.getRulesIds();

						if (rulesIds != null && !rulesIds.isEmpty()) {

							var ruleUni = s.find(Rule.class, rulesIds.toArray())
								.flatMap(rules -> {
									queryAnalysis.setRules(new HashSet<>(rules));
									return Uni.createFrom().item(queryAnalysis);
								});

							builder.add(ruleUni);
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(queryAnalysis));
					})
			);
		}
		return super.create(dto);
	}

	@Override
	public Uni<QueryAnalysis> patch(long queryAnalysisId, QueryAnalysisDTO dto) {
		if (dto instanceof QueryAnalysisWithListsDTO withListsDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, queryAnalysisId)
					.call(queryAnalysis ->
						Mutiny.fetch(queryAnalysis.getAnnotators()))
					.call(queryAnalysis ->
						Mutiny.fetch(queryAnalysis.getRules()))
					.flatMap(queryAnalysis -> {
						var newStateQueryAnalysis =
							mapper.patch(queryAnalysis, withListsDTO);

						//UniBuilder to prevent empty unis
						UniJoin.Builder<QueryAnalysis> builder = Uni.join().builder();
						builder.add(Uni.createFrom().item(newStateQueryAnalysis));

						//annotators
						var annotatorsIds = withListsDTO.getAnnotatorsIds();

						if (annotatorsIds != null) {

							var annotatorUni =
								s.find(Annotator.class, annotatorsIds.toArray())
									.flatMap(annotators -> {
										newStateQueryAnalysis.getAnnotators().clear();
										newStateQueryAnalysis.setAnnotators(
											new HashSet<>(annotators));
										return Uni.createFrom().item(newStateQueryAnalysis);
									});

							builder.add(annotatorUni);
						}

						//rules
						var rulesIds = withListsDTO.getRulesIds();

						if (rulesIds != null) {

							var ruleUni = s.find(Rule.class, rulesIds.toArray())
								.flatMap(rules -> {
									newStateQueryAnalysis.getRules().clear();
									newStateQueryAnalysis.setRules(new HashSet<>(rules));
									return Uni.createFrom().item(newStateQueryAnalysis);
								});

							builder.add(ruleUni);
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(newStateQueryAnalysis));
					})
			);
		}
		return super.patch(queryAnalysisId, dto);
	}

	@Override
	public Uni<QueryAnalysis> update(long queryAnalysisId, QueryAnalysisDTO dto) {
		if (dto instanceof QueryAnalysisWithListsDTO withListsDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, queryAnalysisId)
					.call(queryAnalysis ->
						Mutiny.fetch(queryAnalysis.getAnnotators()))
					.call(queryAnalysis ->
						Mutiny.fetch(queryAnalysis.getRules()))
					.flatMap(queryAnalysis -> {
						var newStateQueryAnalysis =
							mapper.update(queryAnalysis, withListsDTO);

						//UniBuilder to prevent empty unis
						UniJoin.Builder<QueryAnalysis> builder = Uni.join().builder();
						builder.add(Uni.createFrom().item(queryAnalysis));

						//annotators
						var annotatorsIds = withListsDTO.getAnnotatorsIds();
						newStateQueryAnalysis.getAnnotators().clear();

						if (annotatorsIds != null) {

							var annotatorUni =
								s.find(Annotator.class, annotatorsIds.toArray())
									.flatMap(annotators -> {
										newStateQueryAnalysis.setAnnotators(
											new HashSet<>(annotators));
										return Uni.createFrom().item(newStateQueryAnalysis);
									});

							builder.add(annotatorUni);
						}

						//rules
						var rulesIds = withListsDTO.getRulesIds();
						newStateQueryAnalysis.getRules().clear();

						if (rulesIds != null) {

							var ruleUni = s.find(Rule.class, rulesIds.toArray())
								.flatMap(rules -> {
									newStateQueryAnalysis.setRules(new HashSet<>(rules));
									return Uni.createFrom().item(newStateQueryAnalysis);
								});

							builder.add(ruleUni);
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(newStateQueryAnalysis));
					})
			);
		}
		return super.update(queryAnalysisId, dto);
	}

	@Override
	public Class<QueryAnalysis> getEntityClass() {
		return QueryAnalysis.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
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

	public Uni<Tuple2<QueryAnalysis, Rule>> addRuleToQueryAnalysis(
		long id, long ruleId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(queryAnalysis -> ruleService.findById(s, ruleId)
				.onItem()
				.ifNotNull()
				.transformToUni(rule ->
					s.fetch(queryAnalysis.getRules())
						.onItem()
						.ifNotNull()
						.transformToUni(rules -> {

							if (rules.add(rule)) {

								queryAnalysis.setRules(rules);

								return persist(s, queryAnalysis)
									.map(newSC -> Tuple2.of(newSC, rule));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<QueryAnalysis, Rule>> removeRuleToQueryAnalysis(
		long id, long ruleId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(queryAnalysis -> s.fetch(queryAnalysis.getRules())
				.onItem()
				.ifNotNull()
				.transformToUni(rules -> {

					if (queryAnalysis.removeRule(rules, ruleId)) {

						return persist(s, queryAnalysis)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<QueryAnalysis, Annotator>> addAnnotatorToQueryAnalysis(long id, long annotatorId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> annotatorService.findById(s, annotatorId)
						.onItem()
						.ifNotNull()
						.transformToUni(annotator ->
								s.fetch(queryAnalysis.getAnnotators())
										.onItem()
										.ifNotNull()
										.transformToUni(annotators -> {

											if (annotators.add(annotator)) {

												queryAnalysis.setAnnotators(annotators);

												return persist(s, queryAnalysis)
														.map(newSC -> Tuple2.of(newSC, annotator));
											}

											return Uni.createFrom().nullItem();

										})
						)
				));
	}

	public Uni<Tuple2<QueryAnalysis, Annotator>> removeAnnotatorToQueryAnalysis(long id, long annotatorId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> s.fetch(queryAnalysis.getAnnotators())
						.onItem()
						.ifNotNull()
						.transformToUni(annotators -> {

							if (queryAnalysis.removeAnnotators(annotators, annotatorId)) {

								return persist(s, queryAnalysis)
										.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})));
	}

	@Inject
	AnnotatorService annotatorService;

	@Inject
	Logger logger;

	@Inject
	RuleService ruleService;




}
