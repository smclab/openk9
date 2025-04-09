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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.AnalyzerMapper;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.Analyzer_;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.CharFilter_;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.TokenFilter_;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.request.AnalyzerWithListsDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AnalyzerService extends BaseK9EntityService<Analyzer, AnalyzerDTO> {

	private static final Logger log = Logger.getLogger(AnalyzerService.class);

	@Inject
	CharFilterService _charFilterService;
	@Inject
	TokenFilterService _tokenFilterService;
	@Inject
	TokenizerService _tokenizerService;

	AnalyzerService(AnalyzerMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<Tuple2<Analyzer, CharFilter>> addCharFilterToAnalyzer(long id, long charFilterId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _charFilterService.findById(s, charFilterId)
				.onItem()
				.ifNotNull()
				.transformToUni(charFilter ->
					s.fetch(analyzer.getCharFilters())
						.onItem()
						.ifNotNull()
						.transformToUni(charFilters -> {

							if (charFilters.add(charFilter)) {

								analyzer.setCharFilters(charFilters);

								return persist(s, analyzer)
									.map(newSC -> Tuple2.of(newSC, charFilter));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Analyzer, TokenFilter>> addTokenFilterToAnalyzer(
		long id, long tokenFilterId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _tokenFilterService.findById(s, tokenFilterId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilter ->
					s.fetch(analyzer.getTokenFilters())
						.onItem()
						.ifNotNull()
						.transformToUni(tokenFilters -> {

							if (tokenFilters.add(tokenFilter)) {

								analyzer.setTokenFilters(tokenFilters);

								return persist(s, analyzer)
									.map(newSC -> Tuple2.of(newSC, tokenFilter));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Analyzer, Tokenizer>> bindTokenizer(long analyzerId, long tokenizerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _tokenizerService.findById(s, tokenizerId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenizer -> {
					analyzer.setTokenizer(tokenizer);
					return persist(s, analyzer).map(t -> Tuple2.of(t, tokenizer));
				})));
	}

	public Uni<Analyzer> create(AnalyzerDTO analyzerDTO) {
		if (analyzerDTO instanceof AnalyzerWithListsDTO analyzerWithListsDTO) {
			var transientAnalyzer = mapper.create(analyzerWithListsDTO);

			return sessionFactory.withTransaction((s, transaction) ->
					super.create(s, transientAnalyzer)
							.flatMap(analyzer -> {

								UniJoin.Builder<Void> builder = Uni.join().builder();
								builder.add(Uni.createFrom().voidItem());

								if (analyzerWithListsDTO.getTokenizerId() != null) {
									var tokenizer = s.getReference(
											Tokenizer.class,
											analyzerWithListsDTO.getTokenizerId()
									);
									analyzer.setTokenizer(tokenizer);
									builder.add(s.persist(analyzer));
								}

								var charFilterIds = analyzerWithListsDTO.getCharFilterIds();
								if (charFilterIds != null && !charFilterIds.isEmpty()) {
									for (Long charFilterId : charFilterIds) {
										builder.add(addCharFilterToAnalyzer(analyzer.getId(), charFilterId)
												.replaceWithVoid()
										);
									}
								}

								var tokenFilterIds = analyzerWithListsDTO.getTokenFilterIds();
								if (tokenFilterIds != null && !tokenFilterIds.isEmpty()) {
									for (Long tokenFilterId : tokenFilterIds) {
										builder.add(addTokenFilterToAnalyzer(analyzer.getId(), tokenFilterId)
												.replaceWithVoid());
									}
								}

								return builder.joinAll()
										.usingConcurrencyOf(1)
										.andCollectFailures()
										.onFailure()
										.invoke(log::error)
										.flatMap(__ -> s.merge(analyzer));
							})
			);
		}

		return super.create(analyzerDTO);
	}

	public Uni<List<Analyzer>> findUnboundAnalyzersByCharFilter(long charFilterId) {
		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Analyzer> criteriaQuery = cb.createQuery(Analyzer.class);
			Root<Analyzer> rootAnalyzer = criteriaQuery.from(Analyzer.class);

			criteriaQuery.select(rootAnalyzer);

			Subquery<Long> idsToExcludeQuery = criteriaQuery.subquery(Long.class);
			Root<Analyzer> rootAnalyzerToExclude = idsToExcludeQuery.from(Analyzer.class);

			Join<Analyzer, CharFilter> charFilterJoinToExclude =
				rootAnalyzerToExclude.join(Analyzer_.charFilters, JoinType.INNER);

			idsToExcludeQuery
				.select(rootAnalyzerToExclude.get(Analyzer_.id))
				.where(cb.equal(charFilterJoinToExclude.get(CharFilter_.id), charFilterId));

			criteriaQuery.where(
				cb.not(rootAnalyzer.get(Analyzer_.id).in(idsToExcludeQuery)));

			return s.createQuery(criteriaQuery).getResultList();
		});
	}

	public Uni<List<Analyzer>> findUnboundAnalyzersByTokenFilter(long tokenFilterId) {
		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Analyzer> criteriaQuery = cb.createQuery(Analyzer.class);
			Root<Analyzer> rootAnalyzer = criteriaQuery.from(Analyzer.class);

			criteriaQuery.select(rootAnalyzer);

			Subquery<Long> idsToExcludeQuery = criteriaQuery.subquery(Long.class);
			Root<Analyzer> rootAnalyzerToExclude = idsToExcludeQuery.from(Analyzer.class);

			Join<Analyzer, TokenFilter> tokenFilterJoinToExclude =
				rootAnalyzerToExclude.join(Analyzer_.tokenFilters, JoinType.INNER);

			idsToExcludeQuery
				.select(rootAnalyzerToExclude.get(Analyzer_.id))
				.where(cb.equal(tokenFilterJoinToExclude.get(TokenFilter_.id), tokenFilterId));

			criteriaQuery.where(
				cb.not(rootAnalyzer.get(Analyzer_.id).in(idsToExcludeQuery)));

			return s.createQuery(criteriaQuery).getResultList();
		});
	}

	public Uni<Connection<CharFilter>> getCharFilters(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Analyzer_.CHAR_FILTERS, CharFilter.class,
			_charFilterService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	@Override
	public Class<Analyzer> getEntityClass() {return Analyzer.class;}

	@Override
	public String[] getSearchFields() {
		return new String[] {Analyzer_.NAME, Analyzer_.DESCRIPTION, Analyzer_.TYPE};
	}

	public Uni<Connection<TokenFilter>> getTokenFilters(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Analyzer_.TOKEN_FILTERS, TokenFilter.class,
			_tokenFilterService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Tokenizer> getTokenizer(long analyzerId) {
		return sessionFactory.withTransaction(s -> findById(s, analyzerId)
				.flatMap(analyzer -> s.fetch(analyzer.getTokenizer())));
	}

	public Uni<Void> load(Analyzer analyzer) {
		return sessionFactory.withTransaction(s -> {

			List<Uni<?>> unis = new ArrayList<>();

			unis.add(s.fetch(analyzer.getTokenizer()));
			unis.add(s.fetch(analyzer.getCharFilters()));
			unis.add(s.fetch(analyzer.getTokenFilters()));

			return Uni.combine()
				.all()
				.unis(unis)
				.usingConcurrencyOf(1)
				.collectFailures()
				.discardItems();

		});
	}

	public Uni<Analyzer> patch(long analyzerId, AnalyzerDTO analyzerDTO) {
		if (analyzerDTO instanceof AnalyzerWithListsDTO analyzerWithListsDTO) {
			return sessionFactory.withTransaction((s, transaction) ->
					findById(s, analyzerId)
							.call(analyzer -> Mutiny.fetch(analyzer.getCharFilters()))
							.call(analyzer -> Mutiny.fetch(analyzer.getTokenFilters()))
							.flatMap(analyzer -> {
								var newStateAnalyzer = mapper.patch(analyzer, analyzerWithListsDTO);

								UniJoin.Builder<Void> builder = Uni.join().builder();
								builder.add(Uni.createFrom().voidItem());

								if (analyzerWithListsDTO.getTokenizerId() != null) {
									var tokenizer = s.getReference(
											Tokenizer.class,
											analyzerWithListsDTO.getTokenizerId()
									);
									analyzer.setTokenizer(tokenizer);
									builder.add(s.persist(analyzer));
								}

								var charFilterIds = analyzerWithListsDTO.getCharFilterIds();
								if (charFilterIds != null) {
									var oldCharFilters = newStateAnalyzer.getCharFilters();
									for (CharFilter oldCharFilter : oldCharFilters) {
										builder.add(removeCharFilterFromAnalyzer(analyzerId, oldCharFilter.getId())
												.replaceWithVoid());
									}

									for (Long charFilterId : charFilterIds) {
										builder.add(addCharFilterToAnalyzer(analyzerId, charFilterId)
												.replaceWithVoid());
									}
								}

								var tokenFilterIds = analyzerWithListsDTO.getTokenFilterIds();
								if (tokenFilterIds != null) {
									var oldTokenFilters = newStateAnalyzer.getTokenFilters();
									for (TokenFilter oldTokenFilter : oldTokenFilters) {
										builder.add(removeTokenFilterFromAnalyzer(analyzerId, oldTokenFilter.getId())
												.replaceWithVoid());
									}

									for (Long tokenFilterId : tokenFilterIds) {
										builder.add(addTokenFilterToAnalyzer(analyzerId, tokenFilterId)
												.replaceWithVoid());
									}
								}

								return builder.joinAll()
										.usingConcurrencyOf(1)
										.andCollectFailures()
										.onFailure()
										.invoke(log::error)
										.flatMap(__ -> s.merge(newStateAnalyzer));
							})
			);
		}

		return super.patch(analyzerId, analyzerDTO);
	}

	public Uni<Tuple2<Analyzer, CharFilter>> removeCharFilterFromAnalyzer(
		long id, long charFilterId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getCharFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(charFilters -> {

					if (analyzer.removeCharFilter(charFilters, charFilterId)) {

						return persist(s, analyzer)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Analyzer> removeCharFilterListFromAnalyzer(long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getCharFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(charFilters -> {

					if(!charFilters.isEmpty()){
						charFilters.clear();
						return persist(s, analyzer);
					};

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Analyzer> removeTokenFilterListFromAnalyzer(
		long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getTokenFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilters -> {

					if(!tokenFilters.isEmpty()){
						tokenFilters.clear();
						return persist(s, analyzer);
					};

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Analyzer, TokenFilter>> removeTokenFilterFromAnalyzer(
		long id, long tokenFilterId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getTokenFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilters -> {

					if (analyzer.removeTokenFilter(tokenFilters, tokenFilterId)) {

						return persist(s, analyzer)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Analyzer, Tokenizer>> unbindTokenizer(long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> {
				analyzer.setTokenizer(null);
				return persist(s, analyzer).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Analyzer> update(long analyzerId, AnalyzerDTO analyzerDTO) {

		if (analyzerDTO instanceof AnalyzerWithListsDTO analyzerWithListsDTO) {
			return sessionFactory.withTransaction((s, transaction) ->
					findById(s, analyzerId)
							.call(analyzer -> Mutiny.fetch(analyzer.getCharFilters()))
							.call(analyzer -> Mutiny.fetch(analyzer.getTokenFilters()))
							.flatMap(analyzer -> {
								var newStateAnalyzer = mapper.update(analyzer, analyzerWithListsDTO);

								UniJoin.Builder<Void> builder = Uni.join().builder();
								builder.add(Uni.createFrom().voidItem());

								Tokenizer tokenizer = null;
								if (analyzerWithListsDTO.getTokenizerId() != null) {
									tokenizer = s.getReference(
											Tokenizer.class,
											analyzerWithListsDTO.getTokenizerId()
									);
								}
								analyzer.setTokenizer(tokenizer);
								builder.add(s.persist(analyzer));

								var oldCharFilters = newStateAnalyzer.getCharFilters();
								for (CharFilter oldCharFilter : oldCharFilters) {
									builder.add(removeCharFilterFromAnalyzer(analyzerId, oldCharFilter.getId())
											.replaceWithVoid());
								}

								var charFilterIds = analyzerWithListsDTO.getCharFilterIds();
								if (charFilterIds != null) {
									for (Long charFilterId : charFilterIds) {
										builder.add(addCharFilterToAnalyzer(analyzerId, charFilterId)
												.replaceWithVoid());
									}
								}

								var oldTokenFilters = analyzer.getTokenFilters();
								for (TokenFilter oldTokenFilter : oldTokenFilters) {
									builder.add(removeTokenFilterFromAnalyzer(analyzerId, oldTokenFilter.getId())
											.replaceWithVoid());
								}

								var tokenFilterIds = analyzerWithListsDTO.getTokenFilterIds();
								if (tokenFilterIds != null) {
									for (Long tokenFilterId : tokenFilterIds) {
										builder.add(addTokenFilterToAnalyzer(analyzerId, tokenFilterId)
												.replaceWithVoid()
										);
									}
								}

								return builder.joinAll()
										.usingConcurrencyOf(1)
										.andCollectFailures()
										.onFailure()
										.invoke(log::error)
										.flatMap(__ -> s.merge(newStateAnalyzer));
							})
			);
		}

		return super.update(analyzerId, analyzerDTO);
	}
}
