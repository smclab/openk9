package io.openk9.datasource.searcher;

import com.google.protobuf.ByteString;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.searcher.suggestions.SuggestionsUtil;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.datasource.tenant.TenantResolver;
import io.openk9.datasource.util.UniActionListener;
import io.openk9.searcher.dto.ParserSearchToken;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.SearchTokenRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.grpc.Suggestions;
import io.openk9.searcher.grpc.SuggestionsResponse;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@GrpcService
public class SearcherService extends BaseSearchService implements Searcher {
	@Override
	@ActivateRequestContext
	public Uni<QueryParserResponse> queryParser(QueryParserRequest request) {

		return Uni.createFrom().deferred(() -> {

			Map<String, List<ParserSearchToken>> tokenGroup =
				createTokenGroup(request);

			if (tokenGroup.isEmpty()) {
				return Uni
					.createFrom()
					.item(
						QueryParserResponse
							.newBuilder()
							.setQuery(ByteString.EMPTY)
							.build()
					);
			}

			return getTenantAndFetchRelations(request.getVirtualHost(), false, 0)
				.map(tenant -> {

					if (tenant == null) {
						return QueryParserResponse
							.newBuilder()
							.build();
					}

					BoolQueryBuilder boolQueryBuilder =
						createBoolQuery(tokenGroup, tenant);

					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

					searchSourceBuilder.trackTotalHits(true);

					searchSourceBuilder.query(boolQueryBuilder);

					if (request.getRangeCount() == 2) {
						searchSourceBuilder.from(request.getRange(0));
						searchSourceBuilder.size(request.getRange(1));
					}

					List<DocTypeField> docTypeFieldList =
						Utils
							.getDocTypeFieldsFrom(tenant)
							.toList();

					List<String> includes = new ArrayList<>();
					List<String> excludes = new ArrayList<>();
					Set<HighlightBuilder.Field> highlightFields = new HashSet<>();

					for (DocTypeField docTypeField : docTypeFieldList) {
						String name = docTypeField.getName();
						if (docTypeField.isDefaultExclude()) {
							excludes.add(name);
						}
						else {
							includes.add(name);
							if (docTypeField.isText()) {
								highlightFields.add(new HighlightBuilder.Field(name));
							}
						}

					}

					HighlightBuilder highlightBuilder = new HighlightBuilder();

					highlightBuilder.forceSource(true);

					highlightBuilder.tagsSchema("default");

					highlightBuilder.fields().addAll(highlightFields);

					searchSourceBuilder.highlighter(highlightBuilder);

					searchSourceBuilder.fetchSource(
						includes.toArray(String[]::new),
						excludes.toArray(String[]::new)
					);

					List<SearchTokenRequest> searchQuery =
						request.getSearchQueryList();

					if (!searchQuery.isEmpty() && searchQuery
						.stream()
						.anyMatch(st -> !st.getFilter())) {

						SearchConfig searchConfig = tenant.getSearchConfig();

						if (searchConfig != null && searchConfig.getMinScore() != null) {
							searchSourceBuilder.minScore(searchConfig.getMinScore());
						}
						else {
							searchSourceBuilder.minScore(0.5f);
						}

					}

					String[] indexNames =
						tenant
							.getDatasources()
							.stream()
							.map(Datasource::getDataIndex)
							.map(DataIndex::getName)
							.distinct()
							.toArray(String[]::new);


					return QueryParserResponse
						.newBuilder()
						.setQuery(searchSourceBuilderToOutput(searchSourceBuilder))
						.addAllIndexName(List.of(indexNames))
						.build();

				});

		});

	}

	@Override
	public Uni<SuggestionsResponse> suggestionsQueryParser(QueryParserRequest request) {
		return Uni.createFrom().deferred(() -> {

			Map<String, List<ParserSearchToken>> tokenGroup =
				createTokenGroup(request);

			if (tokenGroup.isEmpty()) {
				return Uni
					.createFrom()
					.item(
						SuggestionsResponse
							.newBuilder()
							.build()
					);
			}

			return getTenantAndFetchRelations(
				request.getVirtualHost(), true, request.getSuggestionCategoryId())
				.flatMap(tenant -> {

					if (tenant == null) {
						return Uni.createFrom().item(
							SuggestionsResponse
								.newBuilder()
								.build()
						);
					}

					Set<SuggestionCategory> suggestionCategories =
						tenant.getSuggestionCategories();

					if (suggestionCategories == null || suggestionCategories.isEmpty()) {
						return Uni.createFrom().item(
							SuggestionsResponse
								.newBuilder()
								.build()
						);
					}

					BoolQueryBuilder boolQueryBuilder =
						createBoolQuery(tokenGroup, tenant);

					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

					searchSourceBuilder.query(boolQueryBuilder);

					List<CompositeValuesSourceBuilder<?>> compositeValuesSourceBuilders =
						new ArrayList<>();

					String suggestKeyword = request.getSuggestKeyword();

					if (!suggestionCategories.isEmpty()) {

						for (SuggestionCategory suggestionCategory : suggestionCategories) {
							for (DocTypeField docTypeField : suggestionCategory.getDocTypeFields()) {
								String name = docTypeField.getName();
								compositeValuesSourceBuilders.add(
									new TermsValuesSourceBuilder(name)
										.field(name)
										.missingBucket(true));
							}
						}

						CompositeAggregationBuilder compositeAggregation =
							AggregationBuilders.composite(
								"composite", compositeValuesSourceBuilders);

						String afterKey = request.getAfterKey();

						if (StringUtils.isNotBlank(afterKey)) {
							byte[] afterKeyDecoded =
								Base64.getDecoder().decode(afterKey);

							Map<String, Object> map =
								new JsonObject(
									new String(afterKeyDecoded)).getMap();

							compositeAggregation.aggregateAfter(map);

						}

						if (request.getRangeCount() == 2) {
							int size = request.getRange(1);
							compositeAggregation.size(size);
						}

						if (
							request.getSuggestionCategoryId() != 0 &&
							StringUtils.isNotBlank(suggestKeyword)) {

							SuggestionCategory suggestionCategory =
								suggestionCategories.iterator().next();

							QueryBuilder matchQueryBuilder =
								QueryBuilders.termsQuery(
									suggestionCategory.getName(),
									suggestKeyword.toLowerCase());

							FilterAggregationBuilder suggestions =
								AggregationBuilders
									.filter("suggestions", matchQueryBuilder)
									.subAggregation(compositeAggregation);

							searchSourceBuilder.aggregation(suggestions);
						}
						else {
							searchSourceBuilder.aggregation(
								compositeAggregation);
						}

					}

					searchSourceBuilder.from(0);
					searchSourceBuilder.size(0);

					searchSourceBuilder.highlighter(null);

					String[] indexNames =
						tenant
							.getDatasources()
							.stream()
							.map(Datasource::getDataIndex)
							.map(DataIndex::getName)
							.distinct()
							.toArray(String[]::new);

					SearchRequest searchRequest =
						new SearchRequest(indexNames, searchSourceBuilder);

					Uni<SearchResponse> searchResponseUni = _search(searchRequest);

					return searchResponseUni.map(searchResponse -> {

							Aggregations aggregations =
								searchResponse.getAggregations();

							if (aggregations == null) {
								return SuggestionsResponse
									.newBuilder()
									.build();
							}

							CompositeAggregation responseCompositeAggregation =
								_getCompositeAggregation(searchResponse);

							if (responseCompositeAggregation == null) {
								return SuggestionsResponse
									.newBuilder()
									.build();
							}

							Map<String, Long> fieldNameCategoryIdMap =
								suggestionCategories
									.stream()
									.flatMap(e -> e
										.getDocTypeFields()
										.stream()
										.map(dtf ->
											Map.entry(
												dtf.getName(),
												e.getId()
											)
										)
									)
									.collect(
										Collectors.toMap(
											Map.Entry::getKey,
											Map.Entry::getValue
										)
									);

							List<? extends CompositeAggregation.Bucket> buckets =
								responseCompositeAggregation.getBuckets();

							LinkedList<Suggestions> suggestions =
								new LinkedList<>();

							BiConsumer<String, Suggestions> addSuggestions;

							if (StringUtils.isNotBlank(suggestKeyword)) {
								addSuggestions = (key, sugg) -> {

									if (!suggestions.contains(sugg)) {
										if (containsIgnoreCase(key, suggestKeyword)) {
											suggestions.addFirst(sugg);
										}
									}
								};
							}
							else {
								addSuggestions = (key, sugg) -> {
									if (!suggestions.contains(sugg)) {
										suggestions.add(sugg);
									}
								};
							}

							for (CompositeAggregation.Bucket bucket : buckets) {

								Map<String, Object> keys = new HashMap<>(bucket.getKey());

								for (Map.Entry<String, Object> entry : keys.entrySet()) {

									String key = entry.getKey();
									String value = (String)entry.getValue();

									Long suggestionCategoryId =
										fieldNameCategoryIdMap.get(key);

									if (value == null) {
										continue;
									}

									long docCount = bucket.getDocCount();

									if ("documentTypes".equals(key.replace(".keyword", ""))) {
										addSuggestions.accept(
											value,
											SuggestionsUtil.docType(
												value,
												suggestionCategoryId,
												docCount)
										);
									}
									else {
										addSuggestions.accept(
											value,
											SuggestionsUtil.text(
												value, suggestionCategoryId,
												key, docCount
											)
										);
									}
								}

							}

							Map<String, Object> map =
								responseCompositeAggregation.afterKey();
							String newAfterKey = "";

							if (map != null) {
								newAfterKey = Json.encode(map);
								newAfterKey = Base64.getEncoder().encodeToString(
									newAfterKey.getBytes(StandardCharsets.UTF_8));
							}

							return SuggestionsResponse
								.newBuilder()
								.addAllResult(suggestions)
								.setAfterKey(newAfterKey)
								.build();

					});

				});

		});
	}

	private Uni<SearchResponse> _search(SearchRequest searchRequest) {
		return Uni
			.createFrom()
			.emitter(sink -> client.searchAsync(
				searchRequest, RequestOptions.DEFAULT,
				UniActionListener.of(sink)));
	}

	private CompositeAggregation _getCompositeAggregation(
		SearchResponse searchResponse) {
		Aggregations aggregations = searchResponse.getAggregations();

		ParsedFilter suggestions = aggregations.get("suggestions");

		if (suggestions != null) {
			return suggestions.getAggregations().get("composite");
		}
		else {
			return aggregations.get("composite");
		}
	}

	private static boolean containsIgnoreCase(String str, String searchStr) {
		if (str == null || searchStr == null) {
			return false;
		}

		final int length = searchStr.length();
		if (length == 0) {
			return true;
		}
		for (int i = str.length() - length; i >= 0; i--) {
			if (str.regionMatches(true, i, searchStr, 0, length)) {
				return true;
			}
		}
		return false;
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	TenantResolver tenantResolver;

}