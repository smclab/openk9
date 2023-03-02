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
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.mapper.BucketMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.client.RestHighLevelClient;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

;

@ApplicationScoped
public class BucketService extends BaseK9EntityService<Bucket, BucketDTO> {
	 BucketService(BucketMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<QueryAnalysis> getQueryAnalysis(long bucketId) {
		return withTransaction(s -> findById(bucketId)
			.flatMap(bucket -> Mutiny2.fetch(s, bucket.getQueryAnalysis())));
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

	public Uni<Connection<Tab>> getTabs(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Bucket_.TABS, Tab.class,
			tabService.getSearchFields(), after, before, first,
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

	public Uni<Tuple2<Bucket, Datasource>> removeDatasource(long bucketId, long datasourceId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> datasourceService.findById(datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> Mutiny2.fetch(s, datasource.getBuckets()).flatMap(buckets -> {
					if (buckets.remove(bucket)) {
						datasource.setBuckets(buckets);
						return persist(datasource).map((newD) -> Tuple2.of(bucket, newD));
					}
					return Uni.createFrom().nullItem();
				}))));
	}

	public Uni<Tuple2<Bucket, Datasource>> addDatasource(long bucketId, long datasourceId) {

		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> datasourceService.findById(datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> Mutiny2.fetch(s, datasource.getBuckets()).flatMap(buckets -> {
					if (buckets.add(bucket)) {
						datasource.setBuckets(buckets);
						return persist(datasource).map(newD -> Tuple2.of(bucket, newD));
					}
					return Uni.createFrom().nullItem();
				}))));

	}

	public Uni<Tuple2<Bucket, Tab>> addTabToBucket(
		long id, long tabId) {

		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> tabService.findById(tabId)
				.onItem()
				.ifNotNull()
				.transformToUni(tab ->
					Mutiny2.fetch(s, bucket.getTabs())
						.onItem()
						.ifNotNull()
						.transformToUni(tabs -> {

							if (tabs.add(tab)) {

								bucket.setTabs(tabs);

								return persist(bucket)
									.map(newSC -> Tuple2.of(newSC, tab));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, Tab>> removeTabFromBucket(
		long id, long tabId) {
		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> Mutiny2.fetch(s, bucket.getTabs())
				.onItem()
				.ifNotNull()
				.transformToUni(tabs -> {

					if (bucket.removeTab(tabs, tabId)) {

						return persist(bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, SuggestionCategory>> addSuggestionCategory(long bucketId, long suggestionCategoryId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> suggestionCategoryService.findById(suggestionCategoryId)
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategory ->
					Mutiny2.fetch(s, bucket.getSuggestionCategories())
						.onItem()
						.ifNotNull()
						.transformToUni(suggestionCategories -> {

							if (bucket.addSuggestionCategory(
								suggestionCategories, suggestionCategory)) {

								return persist(bucket)
									.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Bucket, SuggestionCategory>> removeSuggestionCategory(long bucketId, long suggestionCategoryId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> Mutiny2.fetch(s, bucket.getSuggestionCategories())
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategories -> {

					if (bucket.removeSuggestionCategory(
						suggestionCategories, suggestionCategoryId)) {

						return persist(bucket)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Bucket, QueryAnalysis>> bindQueryAnalysis(long bucketId, long queryAnalysisId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> queryAnalysisService.findById(queryAnalysisId)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> {
					bucket.setQueryAnalysis(queryAnalysis);
					return persist(bucket).map(t -> Tuple2.of(t, queryAnalysis));
				})));
	}

	public Uni<Tuple2<Bucket, QueryAnalysis>> unbindQueryAnalysis(long bucketId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> {
				bucket.setQueryAnalysis(null);
				return persist(bucket).map(t -> Tuple2.of(t, null));
			}));
	}


	public Uni<Tuple2<Bucket, SearchConfig>> bindSearchConfig(long bucketId, long searchConfigId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> searchConfigService.findById(searchConfigId)
				.onItem()
				.ifNotNull()
				.transformToUni(searchConfig -> {
					bucket.setSearchConfig(searchConfig);
					return persist(bucket).map(t -> Tuple2.of(t, searchConfig));
				})));
	}

	public Uni<Tuple2<Bucket, SearchConfig>> unbindSearchConfig(long bucketId) {
		return withTransaction((s, tr) -> findById(bucketId)
			.onItem()
			.ifNotNull()
			.transformToUni(bucket -> {
				bucket.setSearchConfig(null);
				return persist(bucket).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Bucket> enableTenant(long id) {
		return withTransaction(s -> s
			.find(Bucket.class, id)
			.flatMap(bucket -> {

				if (bucket == null) {
					return Uni
						.createFrom()
						.failure(new NotFoundException("Tenant not found for id " + id));
				}

				TenantBinding bucketBinding = bucket.getTenantBinding();

				if (bucketBinding == null) {
					CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

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
									.failure(new NotFoundException("Tenant binding not found create one first"));
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

			}));
	}

	public Uni<Long> getDocCountFromBucket(Long bucketId) {
		 return consumeExistedIndexNames(bucketId, indexService::indexCount, 0L);
	}

	public Uni<List<CatResponse>> get_catIndices(Long bucketId){
		 return consumeExistedIndexNames(bucketId, indexService::get_catIndices, List.of());
	}

	public Uni<Long> getCountIndexFromBucket(Long bucketId) {
		return withTransaction(s ->
			getDataIndexNames(bucketId, s)
				.flatMap(this::getExistsAndIndexNames)
				.map(list -> list.stream().filter(io.smallrye.mutiny.tuples.Tuple2::getItem1).count())
		);
	}

	private <T> Uni<T> consumeExistedIndexNames(
		Long bucketId, Function<List<String>, Uni<T>> mapper, T defaultValue) {

		return withTransaction(s ->
			getDataIndexNames(bucketId, s)
				.flatMap(indexNames ->
					getExistsAndIndexNames(indexNames)
						.flatMap(listT -> {

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

						})
				)
		);

	}

	private Uni<List<io.smallrye.mutiny.tuples.Tuple2<Boolean, String>>> getExistsAndIndexNames(
		List<String> indexnames) {

		 List<Uni<io.smallrye.mutiny.tuples.Tuple2<Boolean, String>>> existIndexNames =
			new ArrayList<>(indexnames.size());

		for (String indexname : indexnames) {
			Uni<io.smallrye.mutiny.tuples.Tuple2<Boolean, String>> existIndexName =
				indexService
					.indexExist(indexname)
					.onItemOrFailure()
					.transform((exist, t) -> t == null && exist)
					.map(exist -> io.smallrye.mutiny.tuples.Tuple2.of(exist, indexname));
			existIndexNames.add(existIndexName);
		}
		return Uni
			.join()
			.all(existIndexNames)
			.andCollectFailures();

	}

	private Uni<List<String>> getDataIndexNames(Long bucketId, Mutiny.Session s) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
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

	@Inject
	DatasourceService datasourceService;

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
	RestHighLevelClient client;

}
