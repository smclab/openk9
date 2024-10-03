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
import io.openk9.datasource.graphql.dto.BucketWithListsDTO;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.LanguageService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
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
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class BucketGraphqlResource {

	@Query
	public Uni<Connection<Bucket>> getBuckets(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return bucketService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<List<Bucket>> getUnboundBucketsByDatasource(long datasourceId) {
		return bucketService.findUnboundBucketsByDatasource(datasourceId);
	}

	public Uni<Connection<Datasource>> datasources(
		@Source Bucket bucket,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return bucketService.getDatasourcesConnection(
			bucket.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<SuggestionCategory>> suggestionCategories(
		@Source Bucket bucket,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return bucketService.getSuggestionCategoriesConnection(
			bucket.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<Language>> languages(
		@Source Bucket bucket,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return bucketService.getLanguagesConnection(
			bucket.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<Tab>> tabs(
		@Source Bucket bucket,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return bucketService.getTabs(
			bucket.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Connection<Sorting>> sortings(
		@Source Bucket bucket,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return bucketService.getSortings(
			bucket.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<Language> language(@Source Bucket bucket) {
		return bucketService.getLanguage(bucket.getId());
	}

	public Uni<QueryAnalysis> queryAnalysis(@Source Bucket bucket) {
		return bucketService.getQueryAnalysis(bucket.getId());
	}

	public Uni<Long> getDocCount(@Source Bucket bucket){
		return bucketService.getDocCountFromBucket(bucket.getId());
	}

	public Uni<Long> getIndexCount(@Source Bucket bucket){
		return bucketService.getCountIndexFromBucket(bucket.getId());
	}

	public Uni<List<CatResponse>> getCatIndices(@Source Bucket bucket) {
		return bucketService.get_catIndices(bucket.getId());
	}

	@Query
	public Uni<Bucket> getBucket(@Id long id) {
		return bucketService.findById(id);
	}

	@Query
	public  Uni<Bucket> getEnabledBucket() {
		return bucketService.getCurrentBucket(request.host());
	}

	public Uni<Response<Bucket>> patchBucket(@Id long id, BucketDTO bucketDTO) {
		return bucketService.getValidator().patch(id, bucketDTO);
	}

	public Uni<Response<Bucket>> updateBucket(@Id long id, BucketDTO bucketDTO) {
		return bucketService.getValidator().update(id, bucketDTO);
	}

	public Uni<Response<Bucket>> createBucket(BucketDTO bucketDTO) {
		return bucketService.getValidator().create(bucketDTO);
	}

	@Mutation
	public Uni<Bucket> enableBucket(@Id long id) {
		return bucketService.enableTenant(id);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Tab>> addTabToBucket(
		@Id long id, @Id long tabId) {
		return bucketService.addTabToBucket(id, tabId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Tab>> removeTabFromBucket(
		@Id long id, @Id long tabId) {
		return bucketService.removeTabFromBucket(id, tabId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Sorting>> addSortingToBucket(
		@Id long id, @Id long sortingId) {
		return bucketService.addSortingToBucket(id, sortingId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Sorting>> removeSortingFromBucket(
		@Id long id, @Id long sortingId) {
		return bucketService.removeSortingFromBucket(id, sortingId);
	}

	@Mutation
	public Uni<Response<Bucket>> bucket(
		@Id Long id, BucketDTO bucketDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createBucket(bucketDTO);
		} else {
			return patch
				? patchBucket(id, bucketDTO)
				: updateBucket(id, bucketDTO);
		}

	}

	@Mutation
	public Uni<Response<Bucket>> bucketWithLists(
		@Id Long id, BucketWithListsDTO bucketWithListsDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createBucket(bucketWithListsDTO);
		} else {
			return patch
				? patchBucket(id, bucketWithListsDTO)
				: updateBucket(id, bucketWithListsDTO);
		}

	}

	@Mutation
	public Uni<Bucket> deleteBucket(@Id long bucketId) {
		return bucketService.deleteById(bucketId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Datasource>> addDatasourceToBucket(@Id long bucketId, @Id long datasourceId) {
		return bucketService.addDatasource(bucketId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Datasource>> removeDatasourceFromBucket(@Id long bucketId, @Id long datasourceId) {
		return bucketService.removeDatasource(bucketId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, SuggestionCategory>> addSuggestionCategoryToBucket(@Id long bucketId, @Id long suggestionCategoryId) {
		return bucketService.addSuggestionCategory(bucketId, suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, SuggestionCategory>> removeSuggestionCategoryFromBucket(@Id long bucketId, @Id long suggestionCategoryId) {
		return bucketService.removeSuggestionCategory(bucketId, suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Language>> addLanguageToBucket(@Id long bucketId, @Id long languageId) {
		return bucketService.addLanguage(bucketId, languageId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Language>> removeLanguageFromBucket(@Id long bucketId, @Id long languageId) {
		return bucketService.removeLanguage(bucketId, languageId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, QueryAnalysis>> bindQueryAnalysisToBucket(
		@Id long bucketId, @Id long queryAnalysisId) {
		return bucketService.bindQueryAnalysis(bucketId, queryAnalysisId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, QueryAnalysis>> unbindQueryAnalysisFromBucket(
		@Id long bucketId) {
		return bucketService.unbindQueryAnalysis(bucketId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Language>> bindLanguageToBucket(
		@Id long bucketId, @Id long languageId) {
		return bucketService.bindLanguage(bucketId, languageId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, Language>> unbindLanguageFromBucket(
		@Id long bucketId) {
		return bucketService.unbindLanguage(bucketId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, SearchConfig>> bindSearchConfigToBucket(
		@Id long bucketId, @Id long searchConfigId) {
		return bucketService.bindSearchConfig(bucketId, searchConfigId);
	}

	@Mutation
	public Uni<Tuple2<Bucket, SearchConfig>> unbindSearchConfigFromBucket(
		@Id long bucketId) {
		return bucketService.unbindSearchConfig(bucketId);
	}

	@Subscription
	public Multi<Bucket> bucketCreated() {
		return bucketService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Bucket> bucketDeleted() {
		return bucketService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Bucket> bucketUpdated() {
		return bucketService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	BucketService bucketService;

	@Inject
	LanguageService languageService;

	@Context
	HttpServerRequest request;

}