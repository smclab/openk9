package io.openk9.datasource.searcher;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.searcher.queryanalysis.Grammar;
import io.openk9.datasource.searcher.queryanalysis.GrammarProvider;
import io.openk9.datasource.searcher.queryanalysis.Parse;
import io.openk9.datasource.searcher.queryanalysis.SemanticType;
import io.openk9.datasource.searcher.queryanalysis.SemanticTypes;
import io.openk9.datasource.searcher.suggestions.SuggestionsUtil;
import io.openk9.datasource.searcher.util.JWT;
import io.openk9.datasource.searcher.util.Tuple;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.openk9.datasource.util.UniActionListener;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.grpc.QueryAnalysisRequest;
import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.grpc.QueryAnalysisSearchToken;
import io.openk9.searcher.grpc.QueryAnalysisToken;
import io.openk9.searcher.grpc.QueryAnalysisTokens;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.SearchTokenRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.grpc.Sort;
import io.openk9.searcher.grpc.Suggestions;
import io.openk9.searcher.grpc.SuggestionsResponse;
import io.openk9.searcher.grpc.TokenType;
import io.openk9.searcher.grpc.Value;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jboss.logging.Logger;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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


			return QuarkusCacheUtil.getAsync(
					cache,
					new CompositeCacheKey(request.getVirtualHost(), "getTenantAndFetchRelations"),
					getTenantAndFetchRelations(request.getVirtualHost(), false, 0)
				)
				.map(tenant -> {

					if (tenant == null) {
						return QueryParserResponse
							.newBuilder()
							.build();
					}

					Map<String, List<String>> extraParams = _getExtraParams(request.getExtraMap());

					BoolQueryBuilder boolQueryBuilder =
						createBoolQuery(tokenGroup, tenant, JWT.of(request.getJwt()), extraParams);

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
							.filter(docTypeField -> !docTypeField.isI18N())
							.toList();

					applySort(
						docTypeFieldList, request.getSortList(), request.getSortAfterKey(),
						searchSourceBuilder);

					String language = getLanguage(request, tenant);

					applyHighlightAndIncludeExclude(searchSourceBuilder, docTypeFieldList, language);

					List<SearchTokenRequest> searchQuery =
						request.getSearchQueryList();

					SearchConfig searchConfig = tenant.getSearchConfig();

					applyMinScore(searchSourceBuilder, searchQuery, searchConfig);

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

			return QuarkusCacheUtil.getAsync(
					cache,
					new CompositeCacheKey(
						request.getVirtualHost(),
						"getTenantAndFetchRelations",
						request.getSuggestionCategoryId()),
					getTenantAndFetchRelations(
						request.getVirtualHost(), true, request.getSuggestionCategoryId())
				)
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

					Map<String, List<String>> extraParams = _getExtraParams(request.getExtraMap());

					BoolQueryBuilder boolQueryBuilder =
						createBoolQuery(tokenGroup, tenant, JWT.of(request.getJwt()), extraParams);

					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

					searchSourceBuilder.query(boolQueryBuilder);

					List<CompositeValuesSourceBuilder<?>> compositeValuesSourceBuilders =
						new ArrayList<>();

					String suggestKeyword = request.getSuggestKeyword();

					List<DocTypeField> docTypeFieldList =
						Utils
							.getDocTypeFieldsFrom(tenant)
							.filter(docTypeField -> !docTypeField.isI18N())
							.toList();

					String language = getLanguage(request, tenant);

					List<Tuple2<Long, DocTypeField>> suggestionDocTypeFields = new ArrayList<>();

					for (SuggestionCategory suggestionCategory : suggestionCategories) {
						Set<DocTypeField> docTypeFields = suggestionCategory.getDocTypeFields();
						for (DocTypeField docTypeField : docTypeFields) {
							if (docTypeField.isI18N()) {
								DocTypeField field = getI18nDocTypeField(docTypeField, language, docTypeFieldList);
								if (field != null) {
									suggestionDocTypeFields.add(
										Tuple2.of(suggestionCategory.getId(), field));
								}
							}
							else if (docTypeField.isKeyword()) {
								suggestionDocTypeFields.add(
									Tuple2.of(suggestionCategory.getId(), docTypeField));
							}
						}
					}

					if (!suggestionCategories.isEmpty()) {

						for (Tuple2<Long, DocTypeField> tuple2 : suggestionDocTypeFields) {
							DocTypeField docTypeField = tuple2.getItem2();
							String name = docTypeField.getPath();
							compositeValuesSourceBuilders.add(
								new TermsValuesSourceBuilder(name)
									.field(name)
									.missingBucket(true));
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

							String[] fields = suggestionDocTypeFields
								.stream()
								.map(Tuple2::getItem2)
								.map(DocTypeField::getParentDocTypeField)
								.filter(dtf -> dtf != null && dtf.getFieldType() == FieldType.TEXT)
								.map(DocTypeField::getPath)
								.distinct()
								.toArray(String[]::new);

							if (fields.length > 0) {

								BoolQueryBuilder builder = QueryBuilders.boolQuery();

								for (String field : fields) {
									builder.should(
										QueryBuilders.matchPhrasePrefixQuery(field, suggestKeyword)
									);
								}

								FilterAggregationBuilder suggestions =
									AggregationBuilders
										.filter("suggestions", builder)
										.subAggregation(compositeAggregation);

								searchSourceBuilder.aggregation(suggestions);

							}
						}
						else {
							searchSourceBuilder.aggregation(
								compositeAggregation);
						}

					}

					searchSourceBuilder.from(0);
					searchSourceBuilder.size(0);

					SearchConfig searchConfig = tenant.getSearchConfig();

					if (searchConfig != null && searchConfig.isMinScoreSuggestions()) {
						searchSourceBuilder.minScore(searchConfig.getMinScore());
					}

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
								suggestionDocTypeFields
									.stream()
									.collect(
										Collectors.toMap(
											t -> t.getItem2().getPath(),
											Tuple2::getItem1
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

	private static DocTypeField getI18nDocTypeField(DocTypeField docTypeField, String language, List<DocTypeField> docTypeFieldList) {
		DocTypeField docTypeFieldBase = null;

		for (DocTypeField e : docTypeFieldList) {

			if (e.isKeyword() && e.getPath().startsWith(docTypeField.getPath())) {
				if (e.getPath().contains(language)) {
					return e;
				}
				else if (e.getPath().contains(".base")) {
					docTypeFieldBase = e;
				}
			}
		}

		return docTypeFieldBase;

	}

	@Override
	public Uni<QueryAnalysisResponse> queryAnalysis(QueryAnalysisRequest request) {

		String searchText = request.getSearchText();

		Uni<Grammar> grammarUni =
			grammarProvider.getOrCreateGrammar(request.getVirtualHost());

		return grammarUni.map(grammar -> {

			Map<Integer, ? extends Utils.TokenIndex> tokenIndexMap =
				Utils.toTokenIndexMap(searchText);

			Map<Tuple<Integer>, Map<String, Object>> chart =
				_getRequestTokensMap(tokenIndexMap, request.getTokensList());

			List<Parse> parses = grammar.parseInput(request.getSearchText());

			if (logger.isDebugEnabled()) {

				JsonArray reduce = parses
					.stream()
					.map(Parse::toJson)
					.reduce(
						new JsonArray(),
						JsonArray::add, (a, b) -> b);

				logger.debug(reduce.toString());

			}

			List<SemanticsPos> list = new ArrayList<>();

			for (Map.Entry<Tuple<Integer>, Map<String, Object>> e : chart.entrySet()) {
				list.add(SemanticsPos.of(e.getKey(), e.getValue()));
			}

			for (int i = parses.size() - 1; i >= 0; i--) {
				SemanticTypes semanticTypes =
					parses.get(i).getSemantics().apply();

				List<SemanticType> semanticTypeList =
					semanticTypes.getSemanticTypes();

				for (SemanticType maps : semanticTypeList) {
					for (Map<String, Object> map : maps) {
						Object tokenType = map.get("tokenType");
						if (tokenType != null && !tokenType.equals("TOKEN")) {
							list.add(SemanticsPos.of(maps.getPos(), map));
						}
					}
				}
			}

			logger.debug(list.toString());

			list.sort(SemanticsPos::compareTo);

			logger.debug("Sorted list: " + list);

			Set<SemanticsPos> set = new TreeSet<>(
				SemanticsPos.TOKEN_TYPE_VALUE_SCORE_COMPARATOR);

			set.addAll(list);

			logger.debug("Set: " + set);

			Set<SemanticsPos> scoreOrderedSet = set.stream().sorted(SemanticsPos::compareTo).collect(
				Collectors.toCollection(LinkedHashSet::new));

			scoreOrderedSet.addAll(set);

			logger.debug("scoreOrderedSet: " + set);

			List<QueryAnalysisTokens> result = new ArrayList<>(set.size());

			Map<Tuple<Integer>, List<Map<String, Object>>> collect =
				scoreOrderedSet
					.stream()
					.collect(
						Collectors.groupingBy(
							SemanticsPos::getPos,
							Collectors.mapping(
								SemanticsPos::getSemantics,
								Collectors.toList())
						)
					);

			for (Map.Entry<Tuple<Integer>, List<Map<String, Object>>> entry :
				collect.entrySet()) {

				Integer startPos =
					entry.getKey().getOrDefault(0, -1);

				if (startPos < 0) {
					continue;
				}

				Utils.TokenIndex startTokenIndex =
					tokenIndexMap.get(startPos);

				if (startTokenIndex == null) {
					continue;
				}

				Integer endPos =
					entry.getKey().getOrDefault(1, -1);

				if (endPos >= 0 && (endPos - startPos) > 1) {

					Utils.TokenIndex endTokenIndex =
						tokenIndexMap.get(endPos - 1);

					result.add(
						QueryAnalysisTokens.newBuilder()
							.setText(
								searchText.substring(
									startTokenIndex.getStartIndex(),
									endTokenIndex.getEndIndex()))
								.setStart(startTokenIndex.getStartIndex())
							.setEnd(endTokenIndex.getEndIndex())
							.addAllTokens(_toAnalysisTokens(entry.getValue()))
							.addPos(startTokenIndex.getPos())
							.addPos(endTokenIndex.getPos())
							.build()
					);

				}
				else {
					result.add(
						QueryAnalysisTokens.newBuilder()
							.setText(
								searchText.substring(
									startTokenIndex.getStartIndex(),
									startTokenIndex.getEndIndex()))
							.setStart(startTokenIndex.getStartIndex())
							.setEnd(startTokenIndex.getEndIndex())
							.addAllTokens(_toAnalysisTokens(entry.getValue()))
							.addPos(startTokenIndex.getPos())
							.build()
					);
				}

			}

			return QueryAnalysisResponse.newBuilder()
				.setSearchText(searchText)
				.addAllAnalysis(result)
				.build();

		})
		.onFailure()
		.recoverWithItem(() -> QueryAnalysisResponse.newBuilder().build());

	}

	private List<QueryAnalysisSearchToken> _toAnalysisTokens(
		List<Map<String, Object>> list) {

		List<QueryAnalysisSearchToken> result = new ArrayList<>(list.size());

		for (Map<String, Object> map : list) {
			QueryAnalysisSearchToken.Builder builder =
				QueryAnalysisSearchToken.newBuilder();

			if (map.containsKey("tokenType")) {
				builder.setTokenType(
					TokenType.valueOf((String)map.get("tokenType")));
			}

			if (map.containsKey("value")) {
				builder.setValue((String)map.get("value"));
			}

			if (map.containsKey("score")) {
				builder.setScore((Float)map.get("score"));
			}

			if (map.containsKey("keywordKey")) {
				builder.setKeywordKey((String)map.get("keywordKey"));
			}

			if (map.containsKey("keywordName")) {
				builder.setKeywordName((String)map.get("keywordName"));
			}

			if (map.containsKey("entityType")) {
				builder.setEntityType((String)map.get("entityType"));
			}

			if (map.containsKey("entityName")) {
				builder.setEntityName((String)map.get("entityName"));
			}

			if (map.containsKey("tenantId")) {
				builder.setTenantId((String)map.get("tenantId"));
			}

			if (map.containsKey("label")) {
				builder.setLabel((String)map.get("label"));
			}

			result.add(builder.build());
		}

		return result;
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

	private Map<Tuple<Integer>, Map<String, Object>> _getRequestTokensMap(
		Map<Integer, ? extends Utils.TokenIndex> tokenIndexMap,
		List<QueryAnalysisToken> requestTokens) {

		Map<Tuple<Integer>, Map<String, Object>> result = new HashMap<>();

		for (Map.Entry<Integer, ? extends Utils.TokenIndex> e :
			tokenIndexMap.entrySet()) {
			Integer pos = e.getKey();
			Utils.TokenIndex tokenIndex = e.getValue();
			for (QueryAnalysisToken requestToken : requestTokens) {
				if (requestToken.getStart() == tokenIndex.getStartIndex()
					&& requestToken.getEnd() == tokenIndex.getEndIndex()) {
					result.put(
						Tuple.of(pos, pos + 1),
						_queryAnalysisTokenToMap(requestToken.getToken())
					);
				}
			}
		}

		return result;
	}

	private Map<String, Object> _queryAnalysisTokenToMap(
		QueryAnalysisSearchToken token) {
		Map<String, Object> map = new HashMap<>();
		map.put("value", token.getValue());
		map.put("entityName", token.getEntityName());
		map.put("tokenType", token.getTokenType().name());
		map.put("entityType", token.getEntityType());
		map.put("keywordKey", token.getKeywordKey());
		map.put("keywordName", token.getKeywordName());
		return map;
	}


	private static void applyHighlightAndIncludeExclude(
		SearchSourceBuilder searchSourceBuilder,
		List<DocTypeField> docTypeFieldList, String language) {
		Set<String> includes = new HashSet<>();
		Set<String> excludes = new HashSet<>();
		Set<HighlightBuilder.Field> highlightFields = new HashSet<>();
		Map<DocTypeField, Tuple2<Set<DocTypeField>, Set<DocTypeField>>> i18nMap = new HashMap<>();

		for (DocTypeField docTypeField : docTypeFieldList) {
			DocTypeField i18nParent = getI18nParent(docTypeField);
			if (i18nParent != null) {

				i18nMap.compute(i18nParent, (k, v) -> {
					String fieldName = docTypeField.getPath();
					if (v == null) {
						v = Tuple2.of(new HashSet<>(), new HashSet<>());
					}

					if (fieldName.contains("." + language)){
						v.getItem2().add(docTypeField);
					}
					else if (fieldName.contains(".base")) {
						v.getItem1().add(docTypeField);
					}

					return v;
				});

			}
			else {
				String name = docTypeField.getPath();
				if (docTypeField.isDefaultExclude()) {
					excludes.add(name);
				}
				else {
					includes.add(name);
					if (docTypeField.isSearchableAndText()) {
						highlightFields.add(new HighlightBuilder.Field(name));
					}
				}
			}
		}

		for (Map.Entry<DocTypeField, Tuple2<Set<DocTypeField>, Set<DocTypeField>>> entry
			: i18nMap.entrySet()) {

			Tuple2<Set<DocTypeField>, Set<DocTypeField>> tuple = entry.getValue();
			Set<DocTypeField> docTypeFields =
				!tuple.getItem2().isEmpty() ? tuple.getItem2() : tuple.getItem1();

			for (DocTypeField docTypeField : docTypeFields) {
				String name = docTypeField.getPath();
				if (docTypeField.isDefaultExclude()) {
					excludes.add(name);
				}
				else {
					includes.add(name);
					if (docTypeField.isSearchableAndText()) {
						highlightFields.add(new HighlightBuilder.Field(name));
					}
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

	}

	private static DocTypeField getI18nParent(DocTypeField docTypeField) {
		if (docTypeField == null) {
			return null;
		}

		DocTypeField parent = docTypeField.getParentDocTypeField();
		return parent != null &&
				(parent.getFieldType() != null && parent.getFieldType() == FieldType.I18N) ?
				parent : getI18nParent(parent);
	}

	private static void applyMinScore(
		SearchSourceBuilder searchSourceBuilder,
		List<SearchTokenRequest> searchQuery, SearchConfig searchConfig) {

		if (searchConfig != null && searchConfig.isMinScoreSearch()) {
			searchSourceBuilder.minScore(searchConfig.getMinScore());
		}
		else if (
			!searchQuery.isEmpty() && searchQuery
				.stream()
				.anyMatch(st -> !st.getFilter())) {


			if (searchConfig != null && searchConfig.getMinScore() != null) {
				searchSourceBuilder.minScore(searchConfig.getMinScore());
			}
			else {
				searchSourceBuilder.minScore(0.5f);
			}

		}
	}

	private void applySort(
		List<DocTypeField> docTypeFieldList, List<Sort> sortList,
		String sortAfterKey, SearchSourceBuilder searchSourceBuilder) {

		if (sortList == null || sortList.isEmpty()) {
			return;
		}

		List<String> docTypeFieldNameSortable =
			docTypeFieldList
				.stream()
				.filter(DocTypeField::isSortable)
				.map(DocTypeField::getPath)
				.toList();

		if (docTypeFieldNameSortable.isEmpty()) {
			logger.warn("No sortable doc type field found");
			return;
		}

		for (Sort sort : sortList) {

			String field = sort.getField();

			if (!docTypeFieldNameSortable.contains(field)) {
				logger.warn("Field " + field + " is not sortable");
				continue;
			}

			FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(field);

			Map<String, String> extrasMap = sort.getExtrasMap();

			String sortValue = extrasMap.get("sort");

			if (sortValue != null && (sortValue.equalsIgnoreCase("asc") || sortValue.equalsIgnoreCase("desc"))) {
				fieldSortBuilder.order(SortOrder.fromString(sortValue));
			}

			String missingValue = extrasMap.get("missing");

			if (missingValue != null && (missingValue.equals("_last") || missingValue.equals("_first"))) {
				fieldSortBuilder.missing(missingValue);
			}

			searchSourceBuilder.sort(fieldSortBuilder);

			if (sortAfterKey != null && !sortAfterKey.isBlank()) {
				byte[] decode = Base64.getDecoder().decode(sortAfterKey);
				JsonArray objects = new JsonArray(Buffer.buffer(decode));
				Object[] array = objects.getList().toArray();
				searchSourceBuilder.searchAfter(array);
			}

		}

	}

	private Map<String, List<String>> _getExtraParams(Map<String, Value> extraMap) {

		if (extraMap == null || extraMap.isEmpty()) {
			return Map.of();
		}

		Map<String, List<String>> extraParams = new HashMap<>(extraMap.size());

		for (Map.Entry<String, Value> kv : extraMap.entrySet()) {
			extraParams.put(kv.getKey(), new ArrayList<>(kv.getValue().getValueList()));
		}

		return extraParams;
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	GrammarProvider grammarProvider;

	@Inject
	Logger logger;

	@Inject
	@CacheName("searcher-service")
	Cache cache;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class SemanticsPos implements Comparable<SemanticsPos> {
		private Tuple<Integer> pos;
		private Map<String, Object> semantics;

		@Override
		public int compareTo(SemanticsPos o) {
			return SCORE_COMPARATOR.compare(this, o);
		}

		public static final Comparator<SemanticsPos>
			TOKEN_TYPE_VALUE_SCORE_COMPARATOR = new TokenTypeValueComparator();

		public static final Comparator<SemanticsPos>
			SCORE_COMPARATOR = new ScoreComparator();

	}

	public static class ScoreComparator implements Comparator<SemanticsPos> {
		@Override
		public int compare(SemanticsPos o1, SemanticsPos o2) {
			return _getScoreCompared(o1.getSemantics(), o2.getSemantics());
		}
	}

	public static class TokenTypeValueComparator implements Comparator<SemanticsPos> {

		@Override
		public int compare(
			SemanticsPos to1,
			SemanticsPos to2) {

			Map<String, Object> o1 = to1.semantics;
			Map<String, Object> o2 = to2.semantics;

			String tokenType1 =(String)o1.get("tokenType");
			String tokenType2 =(String)o2.get("tokenType");

			int res = tokenType1.compareTo(tokenType2);

			String value1 =(String)o1.get("value");
			String value2 =(String)o2.get("value");

			return res != 0 ? res : value1.compareTo(value2);

		}

	}

	private static int _getScoreCompared(
		Map<String, Object> o1, Map<String, Object> o2) {

		double scoreO1 = _toDouble(o1.getOrDefault("score", -1.0));
		double scoreO2 = _toDouble(o2.getOrDefault("score", -1.0));

		return -Double.compare(scoreO1, scoreO2);

	}

	private static double _toDouble(Object score) {
		if (score instanceof Double) {
			return (Double)score;
		}
		else if (score instanceof Float) {
			return ((Float)score).doubleValue();
		}
		else {
			return -1.0;
		}
	}

	private static String _getLanguage(QueryParserRequest request, Bucket tenant) {
		String requestLanguage = request.getLanguage();
		if (requestLanguage != null && !requestLanguage.isBlank()) {
			for (Language available : tenant.getAvailableLanguages()) {
				if (available.getValue().equals(requestLanguage)) {
					return requestLanguage;
				}
			}
		}

		Language defaultLanguage = tenant.getDefaultLanguage();
		if (defaultLanguage != null) {
			return defaultLanguage.getValue();
		}

		return Language.NONE;
	}

}