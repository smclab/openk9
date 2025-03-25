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

package io.openk9.datasource.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

import java.io.IOException;
import java.util.Set;
import jakarta.inject.Inject;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.model.init.Languages;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.LanguageService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.BDDMockito;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.search.aggregations.AggregationBuilder;
import org.opensearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearcherSuggestionsGrpcTest {


	private static final String ENTITY_NAME_PREFIX = "SearcherSuggestionsGrpcTest - ";

	private static final String BUCKET_NAME_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String DOC_TYPE_FIELD_ONE_NAME = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_TWO_NAME = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String SCHEMA_NAME = "public";
	private static final String SUGGESTION_ONE_NAME = ENTITY_NAME_PREFIX + "Suggestion category 1";
	private static final String SUGGESTION_TWO_NAME = ENTITY_NAME_PREFIX + "Suggestion category 2";
	private static final String VIRTUAL_HOST = "test.openk9.local";

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	LanguageService languageService;

	@GrpcClient
	Searcher searcher;

	@InjectSpy(convertScopes = true)
	RestHighLevelClient openSearchClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@InjectMock
	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@Test
	@Order(1)
	void init() {
		Long suggestionCategoriesCount = allSuggestionCategoryCount();

		//create
		createDocTypeFieldOne();
		createDocTypeFieldTwo();
		createSuggestionCategoryOneWithDocTypeFieldOne();
		createSuggestionCategoryTwoWithDocTypeFieldTwo();

		//bind
		bindBucketDefaultToSuggestionCategoryOne();
		bindBucketDefaultToSuggestionCategoryTwo();
		bindBucketDefaultToDatasource();
		bindBucketDefaultToLanguage();

		assertEquals(
			suggestionCategoriesCount + 2,
			allSuggestionCategoryCount());
		assertEquals(
			getBucketDefault().getId(),
			getSuggestionCategoryOne().getBuckets().iterator().next().getId()
		);

		var dataIndex = datasourceService.getDataIndex(getDatasourceDefault().getId())
			.await().indefinitely();

		try {
			var createIndexRequest = new CreateIndexRequest(dataIndex.getIndexName());

			openSearchClient.indices()
				.create(createIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Test
	@Order(2)
	void should_aggregate_with_doc_type_field_one_only() {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME)
					.build()));

		var suggestionCategoryOne = getSuggestionCategoryOne();
		var docTypeField = suggestionCategoryOne.getDocTypeField();

		var request = QueryParserRequest.newBuilder()
			.setVirtualHost(VIRTUAL_HOST)
			.setSuggestionCategoryId(suggestionCategoryOne.getId())
			.build();

		searcher.suggestionsQueryParser(request)
			.await()
			.indefinitely();

		then(openSearchClient)
			.should(atLeast(1))
			.searchAsync(
				argThat(
					searchRequest -> {
						var aggregatorFactories = searchRequest
							.source()
							.aggregations()
							.getAggregatorFactories();

						for (AggregationBuilder builder : aggregatorFactories) {
							var sources =
								((CompositeAggregationBuilder) builder).sources();

							assertEquals(1, sources.size());
							assertEquals(
								docTypeField.getName(),
								sources.getFirst().name()
							);
						}

						return true;
					}
				),
				any(),
				any()
			);

	}

	@Test
	@Order(3)
	void tearDown() {
		var docTypeFieldOne = getDocTypeFieldOne();
		var docTypeFieldTwo = getDocTypeFieldTwo();
		var suggestionCategoryOne = getSuggestionCategoryOne();
		var suggestionCategoryTwo = getSuggestionCategoryTwo();

		unbindBucketDefaultToSuggestionCategoryOne();
		unbindBucketDefaultToSuggestionCategoryTwo();
		unbindBucketDefaultToDatasource();
		unbindBucketDefaultToLanguage();

		suggestionCategoryService.unsetDocTypeField(suggestionCategoryOne.getId())
			.await().indefinitely();

		suggestionCategoryService.deleteById(suggestionCategoryOne.getId())
			.await().indefinitely();

		suggestionCategoryService.deleteById(suggestionCategoryTwo.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldOne.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldTwo.getId())
			.await().indefinitely();
	}

	private Long allSuggestionCategoryCount() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.count()
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToDatasource() {
		var bucketId = getBucketDefault().getId();
		var datasourceId = getDatasourceDefault().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addDatasource(bucketId, datasourceId)
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToLanguage() {
		var bucketId = getBucketDefault().getId();
		var languageId = getLanguageItalian().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.bindLanguage(bucketId, languageId)
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToSuggestionCategoryOne() {
		var suggestionCategory = getSuggestionCategoryOne();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.addSuggestionCategory(
					bucket.getId(), suggestionCategory.getId())
		)
		.await()
		.indefinitely();
	}

	private void bindBucketDefaultToSuggestionCategoryTwo() {
		var suggestionCategory = getSuggestionCategoryTwo();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addSuggestionCategory(
						bucket.getId(), suggestionCategory.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketOneToSuggestionCategoryOne() {
		var suggestionCategoryOne = getSuggestionCategoryOne();
		var bucketOne = getBucketOne();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addSuggestionCategory(
						bucketOne.getId(), suggestionCategoryOne.getId())
			)
			.await()
			.indefinitely();
	}

	private void createBucketOne() {
		var suggestionCategoryOneId = getSuggestionCategoryOne().getId();

		var dto = BucketWithListsDTO.builder()
			.name(BUCKET_NAME_ONE)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.suggestionCategoryIds(Set.of(suggestionCategoryOneId))
			.build();

		bucketService.create(dto)
			.await()
			.indefinitely();
	}

	private void createBucketOneFromDefaultBucket() {
		var suggestionCategoryOneId = getSuggestionCategoryOne().getId();
		var bucketDefault = getBucketDefault();

		var dto = BucketWithListsDTO.builder()
			.name(BUCKET_NAME_ONE)
			.refreshOnDate(bucketDefault.getRefreshOnDate())
			.refreshOnQuery(bucketDefault.getRefreshOnQuery())
			.refreshOnTab(bucketDefault.getRefreshOnTab())
			.refreshOnSuggestionCategory(
				bucketDefault.getRefreshOnSuggestionCategory())
			.retrieveType(bucketDefault.getRetrieveType())
			.suggestionCategoryIds(Set.of(suggestionCategoryOneId))
			.queryAnalysisId(
				bucketDefault.getQueryAnalysis().getId())
			.searchConfigId(
				bucketDefault.getSearchConfig().getId())
			.build();

		bucketService.create(dto)
			.await()
			.indefinitely();
	}

	private void createDocTypeFieldOne() {
		var dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_ONE_NAME)
			.fieldName(DOC_TYPE_FIELD_ONE_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.KEYWORD)
			.build();

		docTypeFieldService.create(dto)
			.await()
			.indefinitely();
	}

	private void createDocTypeFieldTwo() {
		var dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_TWO_NAME)
			.fieldName(DOC_TYPE_FIELD_TWO_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.KEYWORD)
			.build();

		docTypeFieldService.create(dto)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryOneWithDocTypeFieldOne() {
		var docTypeFieldId = getDocTypeFieldOne().getId();

		var dto = SuggestionCategoryWithDocTypeFieldDTO.builder()
			.name(SUGGESTION_ONE_NAME)
			.docTypeFieldId(docTypeFieldId)
			.priority(100f)
			.build();

		suggestionCategoryService.create(dto)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryTwoWithDocTypeFieldTwo() {
		var docTypeFieldId = getDocTypeFieldTwo().getId();

		var dto = SuggestionCategoryWithDocTypeFieldDTO.builder()
			.name(SUGGESTION_TWO_NAME)
			.docTypeFieldId(docTypeFieldId)
			.priority(100f)
			.build();

		suggestionCategoryService.create(dto)
			.await()
			.indefinitely();
	}

	private Bucket getBucketDefault() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, io.openk9.datasource.model.init.Bucket.INSTANCE.getName())
						.call(bucket ->
							Mutiny.fetch(bucket.getSuggestionCategories()))
						.call(bucket ->
							Mutiny.fetch(bucket.getAvailableLanguages()))
						.call(bucket ->
							Mutiny.fetch(bucket.getDefaultLanguage()))
						.call(bucket ->
							Mutiny.fetch(bucket.getDatasources()))
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, BUCKET_NAME_ONE)
			)
			.await()
			.indefinitely();
	}

	private Datasource getDatasourceDefault() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, Initializer.INIT_DATASOURCE_CONNECTION)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					docTypeFieldService.findByName(s, DOC_TYPE_FIELD_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					docTypeFieldService.findByName(s, DOC_TYPE_FIELD_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private Language getLanguageItalian() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				languageService.findByName(s, Languages.ITALIAN.getName())
		)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_ONE_NAME)
						.call(suggestionCategory ->
							Mutiny.fetch(suggestionCategory.getDocTypeField()))
						.call(suggestionCategory ->
							Mutiny.fetch(suggestionCategory.getBuckets()))
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_TWO_NAME)
						.call(suggestionCategory ->
							Mutiny.fetch(suggestionCategory.getDocTypeField()))
						.call(suggestionCategory ->
							Mutiny.fetch(suggestionCategory.getBuckets()))
			)
			.await()
			.indefinitely();
	}

	private void unbindBucketDefaultToDatasource() {
		var datasource = getDatasourceDefault();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.removeDatasource(
					bucket.getId(), datasource.getId())
		).await().indefinitely();
	}

	private void unbindBucketDefaultToLanguage() {
		var language = getLanguageItalian();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.removeLanguage(
					bucket.getId(), language.getId())
		).await().indefinitely();
	}

	private void unbindBucketDefaultToSuggestionCategoryOne() {
		var suggestionCategory = getSuggestionCategoryOne();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.removeSuggestionCategory(
					bucket.getId(), suggestionCategory.getId())
		).await().indefinitely();
	}

	private void unbindBucketDefaultToSuggestionCategoryTwo() {
		var suggestionCategory = getSuggestionCategoryTwo();
		var bucket = getBucketDefault();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.removeSuggestionCategory(
					bucket.getId(), suggestionCategory.getId())
		).await().indefinitely();
	}
}
