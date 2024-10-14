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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.BucketWithListsDTO;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.mapper.BucketMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.NotFoundException;
import io.smallrye.mutiny.groups.UniJoin;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class BucketService extends BaseK9EntityService<Bucket, BucketDTO> {
	 BucketService(BucketMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Bucket> create(BucketDTO bucketDTO) {
		if ( bucketDTO instanceof BucketWithListsDTO bucketWithListsDTO) {
			var transientBucket = mapper.create(bucketWithListsDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientBucket)
					.flatMap(bucket -> {

						//UniBuilder to prevent empty unis
						UniJoin.Builder<Void> builder = Uni.join().builder();
						builder.add(Uni.createFrom().voidItem());

						//Datasource
						var datasourceIds = bucketWithListsDTO.getDatasourceIds();

						if (datasourceIds != null && !datasourceIds.isEmpty()) {

							var datasourceUni =
								s.createQuery(
									"SELECT d FROM Datasource d JOIN FETCH d.buckets WHERE d.id in (:datasourceIds)",
									Datasource.class)
									.setParameter("datasourceIds", datasourceIds)
									.getResultList()
									.flatMap(datasources -> {
										datasources.forEach(datasource ->
											datasource.getBuckets().add(bucket)
										);

										return s.persistAll(datasources.toArray());
									});

							builder.add(datasourceUni);
						}

						//Suggestion Category
						var suggestionCategoryIds =
							bucketWithListsDTO.getSuggestionCategoryIds();


						if (suggestionCategoryIds != null && !suggestionCategoryIds.isEmpty()) {

							var suggestionCategoryUni = s.find(
								SuggestionCategory.class, suggestionCategoryIds.toArray())
								.flatMap(suggestionCategories -> {
										suggestionCategories.forEach(suggestionCategory ->
											suggestionCategory.setBucket(bucket));
										return s.persistAll(suggestionCategories.toArray());
									}
								);

							builder.add(suggestionCategoryUni);

							var suggestionCategories =
								suggestionCategoryIds.stream()
									.map(suggestionId ->
										s.getReference(SuggestionCategory.class, suggestionId))
									.collect(Collectors.toSet());

							bucket.setSuggestionCategories(suggestionCategories);
						}

						//Tab
						var tabIds = bucketWithListsDTO.getTabIds();

						if (tabIds != null && !tabIds.isEmpty()) {

							var tabs = tabIds.stream()
								.map(tabId -> s.getReference(Tab.class, tabId))
								.collect(Collectors.toList());

							bucket.setTabs(tabs);

							builder.add(s.persist(bucket));
						}

						return builder.joinAll()
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(bucket));
					}));
		}

		return super.create(bucketDTO);
	}

	public Uni<Bucket> patch(long bucketId, BucketDTO bucketDTO) {
		if ( bucketDTO instanceof BucketWithListsDTO bucketWithListsDTO ) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, bucketId)
					.call(bucket -> Mutiny.fetch(bucket.getDatasources()))
					.call(bucket -> Mutiny.fetch(bucket.getSuggestionCategories()))
					.call(bucket -> Mutiny.fetch(bucket.getTabs()))
					.flatMap(bucket -> {
						var newStateBucket = mapper.patch(bucket, bucketWithListsDTO);

						//UniBuilder to prevent empty unis
						UniJoin.Builder<Void> builder = Uni.join().builder();

						//Datasource
						var datasourceIds = bucketWithListsDTO.getDatasourceIds();

						if (datasourceIds != null && !datasourceIds.isEmpty()) {

							//Iterate over the old datasources to remove the bucket from their list of buckets
							var oldDatasources = newStateBucket.getDatasources();

							oldDatasources.forEach(oldDatasource -> {
								var bucketsSetUni = Mutiny.fetch(oldDatasource.getBuckets())
									.flatMap(buckets -> {
										buckets.remove(bucket);
										return Uni.createFrom().item(buckets);
									})
									.replaceWithVoid();

								builder.add(bucketsSetUni);
							});

							var oldDatasourceUni = s.persistAll(oldDatasources.toArray());

							builder.add(oldDatasourceUni);

							newStateBucket.getDatasources().clear();

							var datasourceUni =
								s.createQuery(
									"SELECT d FROM Datasource d JOIN FETCH d.buckets WHERE d.id in (:datasourceIds)",
									Datasource.class)
									.setParameter("datasourceIds", datasourceIds)
									.getResultList()
									.flatMap(datasources -> {
										datasources.forEach(datasource ->
											datasource.getBuckets().add(newStateBucket)
										);

										newStateBucket.setDatasources(new HashSet<>(datasources));

										return s.persistAll(datasources.toArray());
									});

							builder.add(datasourceUni);
						}

						//Suggestion Category
						var suggestionCategoryIds =
							bucketWithListsDTO.getSuggestionCategoryIds();

						if (suggestionCategoryIds != null && !suggestionCategoryIds.isEmpty()) {

							//Iterate over the old suggestionCategory to remove the bucket associated with them
							var oldSuggestionCategories =
								newStateBucket.getSuggestionCategories();

							oldSuggestionCategories.forEach(oldCategory ->
								oldCategory.setBucket(null));

							var oldSuggestionCategoryUni =
								s.persistAll(oldSuggestionCategories.toArray());

							builder.add(oldSuggestionCategoryUni);

							var suggestionCategoryUni = s.find(
								SuggestionCategory.class, suggestionCategoryIds.toArray())
								.flatMap(suggestionCategories -> {
										suggestionCategories.forEach(suggestionCategory ->
											suggestionCategory.setBucket(newStateBucket));
										return s.persistAll(suggestionCategories.toArray());
									}
								);

							builder.add(suggestionCategoryUni);

							var suggestionCategories =
								suggestionCategoryIds.stream()
									.map(suggestionId ->
										s.getReference(SuggestionCategory.class, suggestionId))
									.collect(Collectors.toSet());

							newStateBucket.getSuggestionCategories().clear();
							newStateBucket.setSuggestionCategories(suggestionCategories);
						}

						//Tab
						var tabIds = bucketWithListsDTO.getTabIds();

						if (tabIds != null && !tabIds.isEmpty()) {
							var tabs = tabIds.stream()
								.map(tabId -> s.getReference(Tab.class, tabId))
								.collect(Collectors.toList());

							newStateBucket.getTabs().clear();
							newStateBucket.setTabs(tabs);

							builder.add(s.persist(newStateBucket));
						}

						return builder.joinAll()
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(newStateBucket));
					})
			);
		}

		return super.patch(bucketId, bucketDTO);
	}

	public Uni<Bucket> update(long bucketId, BucketDTO bucketDTO) {
		if ( bucketDTO instanceof BucketWithListsDTO bucketWithListsDTO ) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, bucketId)
					.call(bucket -> Mutiny.fetch(bucket.getDatasources()))
					.call(bucket -> Mutiny.fetch(bucket.getSuggestionCategories()))
					.call(bucket -> Mutiny.fetch(bucket.getTabs()))
					.flatMap(bucket -> {
						var newStateBucket = mapper.update(bucket, bucketWithListsDTO);

						//UniBuilder to prevent empty unis
						UniJoin.Builder<Void> builder = Uni.join().builder();

						//Datasource
						var datasourceIds = bucketWithListsDTO.getDatasourceIds();

						//Iterate over the old datasources to remove the bucket from their list of buckets
						var oldDatasources = newStateBucket.getDatasources();

						oldDatasources.forEach(oldDatasource -> {
							var bucketsSetUni = Mutiny.fetch(oldDatasource.getBuckets())
								.flatMap(buckets -> {
									buckets.remove(bucket);
									return Uni.createFrom().item(buckets);
								})
								.replaceWithVoid();

							builder.add(bucketsSetUni);
						});

						var oldDatasourceUni = s.persistAll(oldDatasources.toArray());

						builder.add(oldDatasourceUni);

						newStateBucket.getDatasources().clear();

						if (datasourceIds != null) {
							var datasourceUni =
								s.createQuery(
									"SELECT d FROM Datasource d JOIN FETCH d.buckets WHERE d.id in (:datasourceIds)",
									Datasource.class)
									.setParameter("datasourceIds", datasourceIds)
									.getResultList()
									.flatMap(datasources -> {
										datasources.forEach(datasource ->
											datasource.getBuckets().add(newStateBucket)
										);

										newStateBucket.setDatasources(new HashSet<>(datasources));

										return s.persistAll(datasources.toArray());
									});

							builder.add(datasourceUni);
						}

						//Suggestion Category
						var suggestionCategoryIds =
							bucketWithListsDTO.getSuggestionCategoryIds();

						//Iterate over the old suggestionCategory to remove the bucket associated with them
						var oldSuggestionCategories =
							newStateBucket.getSuggestionCategories();

						oldSuggestionCategories.forEach(oldCategory ->
							oldCategory.setBucket(null));

						var oldSuggestionCategoryUni =
							s.persistAll(oldSuggestionCategories.toArray());

						builder.add(oldSuggestionCategoryUni);

						newStateBucket.getSuggestionCategories().clear();

						if (suggestionCategoryIds != null) {
							var suggestionCategoryUni = s.find(
								SuggestionCategory.class, suggestionCategoryIds.toArray())
								.flatMap(suggestionCategories -> {
										suggestionCategories.forEach(suggestionCategory ->
											suggestionCategory.setBucket(newStateBucket));
										return s.persistAll(suggestionCategories.toArray());
									}
								);

							builder.add(suggestionCategoryUni);

							var suggestionCategories =
								suggestionCategoryIds.stream()
									.map(suggestionId ->
										s.getReference(SuggestionCategory.class, suggestionId))
									.collect(Collectors.toSet());

							newStateBucket.getSuggestionCategories().clear();
							newStateBucket.setSuggestionCategories(suggestionCategories);
						}

						//Tab
						var tabIds = bucketWithListsDTO.getTabIds();
						newStateBucket.getTabs().clear();

						if (tabIds != null) {
							var tabs = tabIds.stream()
								.map(tabId -> s.getReference(Tab.class, tabId))
								.collect(Collectors.toList());

							newStateBucket.getTabs().clear();
							newStateBucket.setTabs(tabs);

							builder.add(s.persist(newStateBucket));
						}

						return builder.joinAll()
							.andCollectFailures()
							.onFailure()
							.invoke(throwable -> logger.error(throwable))
							.flatMap(__ -> s.merge(newStateBucket));
					})
			);
		}

		return super.update(bucketId, bucketDTO);
	}

	public Uni<List<Bucket>> findUnboundBucketsByDatasource(long datasourceId) {
		return sessionFactory.withTransaction(s -> {
			String queryString = "SELECT bucket.* from bucket " +
				"WHERE bucket.id not in ( " +
				"SELECT datasource_buckets.buckets_id FROM datasource_buckets " +
				"WHERE datasource_buckets.datasource_id = (:datasourceId))";

			return s.createNativeQuery(queryString, Bucket.class)
				.setParameter("datasourceId", datasourceId)
				.getResultList();
		});
	}

	public Uni<List<Bucket>> findUnboundBucketsBySuggestionCategory(long suggestionCategoryId) {
		return sessionFactory.withTransaction(s -> {
			String queryString = "SELECT b from Bucket b " +
				"WHERE b.id not in ( " +
				"SELECT sc.bucket.id FROM SuggestionCategory sc " +
				"WHERE sc.id = (:suggestionCategoryId))";

			return s.createQuery(queryString, Bucket.class)
				.setParameter("suggestionCategoryId", suggestionCategoryId)
				.getResultList();
		});
	}

	public Uni<List<Bucket>> findUnboundBucketsByTab(long tabId) {
		return sessionFactory.withTransaction(s -> {
			String queryString = "SELECT bucket.* from bucket " +
				"WHERE bucket.id not in ( " +
				"SELECT buckets_tabs.buckets_id FROM buckets_tabs " +
				"WHERE buckets_tabs.tabs_id = (:tabId))";

			return s.createNativeQuery(queryString, Bucket.class)
				.setParameter("tabId", tabId)
				.getResultList();
		});
	}

	public Uni<QueryAnalysis> getQueryAnalysis(long bucketId) {
		return sessionFactory.withTransaction(s -> findById(s, bucketId)
			.flatMap(bucket -> s.fetch(bucket.getQueryAnalysis())));
	}

	public Uni<Connection<Datasource>> getDatasourcesConnection(
		long bucketId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			bucketId, Bucket_.DATASOURCES, Datasource.class,
			datasourceService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Page<Datasource>> getDatasources(long bucketId, Pageable pageable, Filter filter) {
		return getDatasources(new Long[] {bucketId}, pageable, filter);
	}

	public Uni<Page<Datasource>> getDatasources(
		long bucketId, Pageable pageable, String searchText) {
		return getDatasources(new Long[] {bucketId}, pageable, searchText);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] bucketIds, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			bucketIds, Bucket_.DATASOURCES, Datasource.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] bucketIds, Pageable pageable, Filter filter) {

		 return findAllPaginatedJoin(
			 bucketIds, Bucket_.DATASOURCES, Datasource.class,
			 pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			 pageable.getBeforeId(), filter);
	}

	public Uni<Connection<Language>> getLanguagesConnection(
		long bucketId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			bucketId, Bucket_.AVAILABLE_LANGUAGES, Language.class,
			languageService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Page<Language>> getLanguages(long bucketId, Pageable pageable, Filter filter) {
		return getLanguages(new Long[] {bucketId}, pageable, filter);
	}

	public Uni<Page<Language>> getLanguages(
		long bucketId, Pageable pageable, String searchText) {
		return getLanguages(new Long[] {bucketId}, pageable, searchText);
	}

	public Uni<Page<Language>> getLanguages(
		Long[] bucketIds, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			bucketIds, Bucket_.AVAILABLE_LANGUAGES, Language.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<Language>> getLanguages(
		Long[] bucketIds, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			bucketIds, Bucket_.AVAILABLE_LANGUAGES, Language.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), filter);
	}

	public Uni<Connection<Tab>> getTabs(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Bucket_.TABS, Tab.class,
			tabService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Connection<Sorting>> getSortings(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Bucket_.SORTINGS, Sorting.class,
			sortingService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}


	public Uni<Connection<SuggestionCategory>> getSuggestionCategoriesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, Bucket_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			suggestionCategoryService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long bucketId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{bucketId}, Bucket_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long bucketId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{bucketId}, Bucket_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), filter);
	}

	public Uni<Language> getLanguage(Bucket bucket) {
		return sessionFactory.withTransaction(
			s -> s.fetch(bucket.getDefaultLanguage()));
	}

	public Uni<Language> getLanguage(long bucketId) {
		return sessionFactory.withTransaction(s -> findById(s, bucketId)
			.flatMap(b -> s.fetch(b.getDefaultLanguage())));
	}

	public Uni<Tuple2<Bucket, Datasource>> removeDatasource(long bucketId, long datasourceId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> datasourceService.findById(s, datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> s.fetch(datasource.getBuckets()).flatMap(buckets -> {
					if (buckets.remove(bucket)) {
						datasource.setBuckets(buckets);
						return persist(s, datasource).map((newD) -> Tuple2.of(bucket, newD));
					}
					return Uni.createFrom().nullItem();
				}))));
	}

	public Uni<Tuple2<Bucket, Datasource>> addDatasource(long bucketId, long datasourceId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> datasourceService.findById(s, datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> s.fetch(datasource.getBuckets()).flatMap(buckets -> {
					if (buckets.add(bucket)) {
						datasource.setBuckets(buckets);
						return persist(s, datasource).map(newD -> Tuple2.of(bucket, newD));
					}
					return Uni.createFrom().nullItem();
				}))));

	}

	public Uni<Tuple2<Bucket, Tab>> addTabToBucket(long id, long tabId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> tabService.findById(s, tabId)
				.onItem()
				.ifNotNull()
				.transformToUni(tab ->
					s.fetch(bucket.getTabs())
						.onItem()
						.ifNotNull()
						.transformToUni(tabs -> {

							if (tabs.add(tab)) {

								bucket.setTabs(tabs);

								return persist(s, bucket)
									.map(newSC -> Tuple2.of(newSC, tab));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, Tab>> removeTabFromBucket(long id, long tabId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> s.fetch(bucket.getTabs())
				.onItem()
				.ifNotNull()
				.transformToUni(tabs -> {

					if (bucket.removeTab(tabs, tabId)) {

						return persist(s, bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, Sorting>> addSortingToBucket(long id, long sortingId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> sortingService.findById(s, sortingId)
				.onItem()
				.ifNotNull()
				.transformToUni(sorting ->
					s.fetch(bucket.getSortings())
						.onItem()
						.ifNotNull()
						.transformToUni(sortings -> {

							if (sortings.add(sorting)) {

								bucket.setSortings(sortings);

								return persist(s, bucket)
									.map(newSC -> Tuple2.of(newSC, sorting));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, Sorting>> removeSortingFromBucket(long id, long sortingId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> s.fetch(bucket.getSortings())
				.onItem()
				.ifNotNull()
				.transformToUni(sortings -> {

					if (bucket.removeSorting(sortings, sortingId)) {

						return persist(s, bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, SuggestionCategory>> addSuggestionCategory(long bucketId, long suggestionCategoryId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> suggestionCategoryService.findById(s, suggestionCategoryId)
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategory ->
					s.fetch(bucket.getSuggestionCategories())
						.onItem()
						.ifNotNull()
						.transformToUni(suggestionCategories -> {

							if (bucket.addSuggestionCategory(
								suggestionCategories, suggestionCategory)) {

								return persist(s, bucket)
									.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, SuggestionCategory>> removeSuggestionCategory(long bucketId, long suggestionCategoryId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> s.fetch(bucket.getSuggestionCategories())
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategories -> {

					if (bucket.removeSuggestionCategory(
						suggestionCategories, suggestionCategoryId)) {

						return persist(s, bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, Language>> addLanguage(long bucketId, long languageId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> languageService.findById(s, languageId)
				.onItem()
				.ifNotNull()
				.transformToUni(language ->
					s.fetch(bucket.getAvailableLanguages())
						.onItem()
						.ifNotNull()
						.transformToUni(languages -> {

							if (bucket.addLanguage(
								languages, language)) {

								return persist(s, bucket)
									.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, Language>> removeLanguage(long bucketId, long languageId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> s.fetch(bucket.getAvailableLanguages())
				.onItem()
				.ifNotNull()
				.transformToUni(languages -> {

					if (bucket.removeLanguage(
						languages, languageId)) {

						return persist(s, bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, QueryAnalysis>> bindQueryAnalysis(long bucketId, long queryAnalysisId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> queryAnalysisService.findById(s, queryAnalysisId)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> {
					bucket.setQueryAnalysis(queryAnalysis);
					return persist(s, bucket).map(t -> Tuple2.of(t, queryAnalysis));
				})));
	}

	public Uni<Tuple2<Bucket, QueryAnalysis>> unbindQueryAnalysis(long bucketId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> {
				bucket.setQueryAnalysis(null);
				return persist(s, bucket).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Tuple2<Bucket, Language>> bindLanguage(long bucketId, long languageId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> languageService.findById(s, languageId)
				.onItem()
				.ifNotNull()
				.transformToUni(language -> {
					bucket.setDefaultLanguage(language);
					return persist(s, bucket).map(t -> Tuple2.of(t, language));
				})));
	}

	public Uni<Tuple2<Bucket, Language>> unbindLanguage(long bucketId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> {
				bucket.setDefaultLanguage(null);
				return persist(s, bucket).map(t -> Tuple2.of(t, null));
			}));
	}


	public Uni<Tuple2<Bucket, SearchConfig>> bindSearchConfig(long bucketId, long searchConfigId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> searchConfigService.findById(s, searchConfigId)
				.onItem()
				.ifNotNull()
				.transformToUni(searchConfig -> {
					bucket.setSearchConfig(searchConfig);
					return persist(s, bucket).map(t -> Tuple2.of(t, searchConfig));
				})));
	}

	public Uni<Tuple2<Bucket, SearchConfig>> unbindSearchConfig(long bucketId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> {
				bucket.setSearchConfig(null);
				return persist(s, bucket).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Bucket> enableTenant(Mutiny.Session s, long id) {
		return findById(s, id)
			.flatMap(bucket -> {

				if (bucket == null) {
					return Uni
						.createFrom()
						.failure(new NotFoundException("Bucket not found for id " + id));
				}

				TenantBinding bucketBinding = bucket.getTenantBinding();

				if (bucketBinding == null) {
					CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();

					CriteriaQuery<TenantBinding> query =
						criteriaBuilder.createQuery(TenantBinding.class);

					query.from(TenantBinding.class);

					return s
						.createQuery(query)
						.getSingleResultOrNull()
						.flatMap(tb -> {

							if (tb == null) {
								return Uni
									.createFrom()
									.failure(new NotFoundException(
										"Tenant binding not found create one first"));
							}

							bucket.setTenantBinding(tb);
							tb.setBucket(bucket);

							return s
								.persist(tb)
								.map(t -> bucket)
								.call(s::flush);

						});

				}

				return Uni.createFrom().item(bucket);

			});
	}

	public Uni<Bucket> enableTenant(long id) {
		return sessionFactory.withTransaction((s, t) -> enableTenant(s, id));
	}

	public Uni<Long> getDocCountFromBucket(Long bucketId) {
		return consumeExistedIndexNames(
			bucketId, l -> consumeExistedIndexNames(indexService::indexCount, 0L, l));
	}

	public Uni<List<CatResponse>> get_catIndices(Long bucketId){
		 return consumeExistedIndexNames(
			 bucketId, l -> consumeExistedIndexNames(indexService::get_catIndices, List.of(), l));
	}

	public Uni<Long> getCountIndexFromBucket(Long bucketId) {
		return consumeExistedIndexNames(
			bucketId, list ->
				Uni
					.createFrom()
					.item(
						list
							.stream()
							.filter(io.smallrye.mutiny.tuples.Tuple2::getItem1)
							.count()
					)
		);
	}

	private <T> Uni<T> consumeExistedIndexNames(
		Long bucketId,
		Function<
			List<io.smallrye.mutiny.tuples.Tuple2<Boolean, String>>,
			Uni<? extends T>> mapper) {

		return sessionFactory.withTransaction(s -> getDataIndexNames(bucketId, s))
			.flatMap(indexService::getExistsAndIndexNames)
			.flatMap(mapper);

	}

	private static <T> Uni<T> consumeExistedIndexNames(
		Function<List<String>, Uni<T>> mapper, T defaultValue,
		List<io.smallrye.mutiny.tuples.Tuple2<Boolean, String>> listT) {

		List<String> existIndexName = new ArrayList<>();

		for (io.smallrye.mutiny.tuples.Tuple2<Boolean, String> t2 : listT) {
			if (t2.getItem1()) {
				existIndexName.add(t2.getItem2());
			}
		}

		if (existIndexName.isEmpty()) {
			return Uni.createFrom().item(defaultValue);
		}

		return mapper.apply(existIndexName);
	}

	private Uni<List<String>> getDataIndexNames(Long bucketId, Mutiny.Session s) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);

		Root<Bucket> bucketRoot = criteriaQuery.from(Bucket.class);

		Join<Bucket, Datasource> datasourceJoin = bucketRoot.join(Bucket_.datasources);

		Join<Datasource, DataIndex> dataIndexJoin =
			datasourceJoin.join(Datasource_.dataIndex);

		criteriaQuery.select(dataIndexJoin.get(DataIndex_.name));
		criteriaQuery.where(cb.equal(bucketRoot.get(Bucket_.id), bucketId));

		criteriaQuery.distinct(true);

		return s.createQuery(criteriaQuery).getResultList();
	}


	@Override
	public String[] getSearchFields() {
		return new String[] {Bucket_.NAME, Bucket_.DESCRIPTION};
	}

	@Override
	public Class<Bucket> getEntityClass() {
		return Bucket.class;
	}

	public Uni<Bucket> getCurrentBucket(String host) {
		 return sessionFactory.withTransaction(session -> session
			 .createNamedQuery(Bucket.CURRENT_NAMED_QUERY, Bucket.class)
			 .setParameter(TenantBinding_.VIRTUAL_HOST, host)
			 .getSingleResult()
		 );
	}

	@Inject
	DatasourceService datasourceService;

	 @Inject
	 LanguageService languageService;

	 @Inject
	IndexService indexService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	QueryAnalysisService queryAnalysisService;

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	TabService tabService;

	@Inject
	SortingService sortingService;


	@Inject
	Logger logger;
}
