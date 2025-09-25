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

package io.openk9.datasource.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.PluginDriver_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SearchConfig_;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.mapper.SearcherMapper;
import io.openk9.searcher.grpc.QueryParserRequest;

import com.google.protobuf.ByteString;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

public abstract class BaseSearchService {

	private static final JsonObject EMPTY_JSON = new JsonObject(Map.of());
	private static final Logger log = Logger.getLogger(BaseSearchService.class);
	@Inject
	Instance<QueryParser> queryParserInstance;
	@Inject
	SearcherMapper searcherMapper;
	@Inject
	Mutiny.SessionFactory sf;
	@Inject
	TenantRegistry tenantRegistry;

	public static JsonObject getQueryParserConfig(Bucket bucket, QueryParserType tokenType) {

		SearchConfig searchConfig = bucket.getSearchConfig();

		if (searchConfig == null) {
			return EMPTY_JSON;
		}

		return searchConfig
			.getQueryParserConfigs()
			.stream()
			.filter(queryParserConfig -> queryParserConfig
				.getType().equals(tokenType))
			.findFirst()
			.map(SearcherService::toJsonObject)
			.orElse(EMPTY_JSON);
	}

	public static JsonObject toJsonObject(QueryParserConfig queryParserConfig) {

		String jsonConfig = queryParserConfig.getJsonConfig();

		if (jsonConfig == null) {
			Logger
				.getLogger(SearcherService.class)
				.warn("jsonConfig is null for queryParserConfig.type: " + queryParserConfig.getType());
			return EMPTY_JSON;
		}

		try {
			return new JsonObject(jsonConfig);
		}
		catch (DecodeException e) {
			Logger
				.getLogger(SearcherService.class)
				.warn("jsonConfig is not a valid json for queryParserConfig.type: " + queryParserConfig.getType() + " jsonConfig: " + jsonConfig);
			return EMPTY_JSON;
		}

	}

	protected static ByteString searchSourceBuilderToOutput(
		SearchSourceBuilder searchSourceBuilder) {
		ByteString.Output outputStream = ByteString.newOutput();

		try (
			XContentBuilder builder =
				searchSourceBuilder.toXContent(
					new XContentBuilder(
						XContentType.JSON.xContent(), new OutputStreamStreamOutput(outputStream)),
					ToXContent.EMPTY_PARAMS)) {
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return outputStream.toByteString();
	}

	protected Uni<BoolQueryBuilder> createBoolQuery(
		Map<QueryParserType, List<ParserSearchToken>> tokenGroup, TenantWithBucket tenantWithBucket,
		JWT jwt, Map<String, List<String>> extraParams, String language) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolean hasToken = tokenGroup != null && !tokenGroup.isEmpty();

		List<Uni<Void>> queryParserUnis = new ArrayList<>();

		if (hasToken) {

			for (Map.Entry<QueryParserType, List<ParserSearchToken>> entry : tokenGroup.entrySet()) {
				QueryParserType tokenType = entry.getKey();
				List<ParserSearchToken> parserSearchTokens = entry.getValue();

				for (QueryParser queryParser : queryParserInstance) {

					if (queryParser.isQueryParserGroup() &&
						queryParser.getType() == tokenType) {

						var queryParserUni = queryParser.apply(
							ParserContext
								.builder()
								.tokenTypeGroup(parserSearchTokens)
								.mutableQuery(boolQueryBuilder)
								.tenantWithBucket(tenantWithBucket)
								.queryParserConfig(
									getQueryParserConfig(
										tenantWithBucket.getBucket(),
										tokenType
									)
								)
								.jwt(jwt)
								.extraParams(extraParams)
								.language(language)
								.build()
						);
						queryParserUnis.add(queryParserUni);
					}
				}
			}

		}

		List<ParserSearchToken> parserSearchTokens = null;

		for (QueryParser queryParser : queryParserInstance) {
			if (!queryParser.isQueryParserGroup()) {

				if (parserSearchTokens == null) {
					if (hasToken) {
						parserSearchTokens = tokenGroup
							.values()
							.stream()
							.flatMap(Collection::stream)
							.collect(Collectors.toList());
					}
					else {
						parserSearchTokens = new ArrayList<>();
					}
				}

				var queryParserUni = queryParser.apply(
					ParserContext
						.builder()
						.tokenTypeGroup(parserSearchTokens)
						.mutableQuery(boolQueryBuilder)
						.tenantWithBucket(tenantWithBucket)
						.queryParserConfig(
							getQueryParserConfig(
								tenantWithBucket.getBucket(),
								queryParser.getType()
							)
						)
						.jwt(jwt)
						.extraParams(extraParams)
						.build()
				);

				queryParserUnis.add(queryParserUni);

			}
		}

		return Uni.join().all(queryParserUnis)
			.usingConcurrencyOf(1)
			.andCollectFailures()
			.map(voids -> boolQueryBuilder);
	}

	protected Map<QueryParserType, List<ParserSearchToken>> createTokenGroup(
		QueryParserRequest request) {

		return request
			.getSearchQueryList()
			.stream()
			.map(searcherMapper::toParserSearchToken)
			.collect(Collectors.groupingBy(token ->
				QueryParserType.valueOf(token.getTokenType())));

	}

	protected void customizeCriteriaBuilder(
		String virtualHost, CriteriaBuilder criteriaBuilder,
		CriteriaQuery<Bucket> criteriaQuery, boolean suggestion,
		long suggestionCategoryId) {

		Root<Bucket> tenantRoot = criteriaQuery.from(Bucket.class);

		Join<Bucket, TenantBinding> tenantBindingJoin =
			tenantRoot.join(Bucket_.tenantBinding);

		tenantRoot
			.fetch(Bucket_.searchConfig, JoinType.LEFT)
			.fetch(SearchConfig_.queryParserConfigs, JoinType.LEFT);

		tenantRoot.fetch(Bucket_.defaultLanguage, JoinType.LEFT);

		tenantRoot.fetch(Bucket_.availableLanguages, JoinType.LEFT);

		tenantRoot.fetch(Bucket_.autocorrection, JoinType.LEFT);

		Predicate conjunction = criteriaBuilder.conjunction();

		if (suggestion) {

			Fetch<Bucket, SuggestionCategory> suggestionCategoryFetch =
				tenantRoot.fetch(Bucket_.suggestionCategories);

			Fetch<SuggestionCategory, DocTypeField> categoryDocTypeFieldFetch =
				suggestionCategoryFetch
					.fetch(SuggestionCategory_.docTypeField);

			categoryDocTypeFieldFetch
				.fetch(DocTypeField_.parentDocTypeField, JoinType.LEFT);

			if (suggestionCategoryId > 0) {

				conjunction = criteriaBuilder.and(
					conjunction,
					criteriaBuilder.equal(
						((Path<SuggestionCategory>)suggestionCategoryFetch)
							.get(SuggestionCategory_.id),
						suggestionCategoryId)
				);
			}

		}

		Fetch<Bucket, Datasource> datasourceRoot =
			tenantRoot.fetch(Bucket_.datasources);

		datasourceRoot
			.fetch(Datasource_.pluginDriver)
			.fetch(PluginDriver_.aclMappings, JoinType.LEFT);

		Fetch<Datasource, DataIndex> dataIndexRoot =
			datasourceRoot.fetch(Datasource_.dataIndex);

		Fetch<DataIndex, DocType> docTypeFetch =
			dataIndexRoot.fetch(DataIndex_.docTypes, JoinType.LEFT);

		docTypeFetch
			.fetch(DocType_.docTypeFields, JoinType.LEFT)
			.fetch(DocTypeField_.parentDocTypeField, JoinType.LEFT);

		conjunction = criteriaBuilder.and(
			conjunction,
			criteriaBuilder.equal(
				tenantBindingJoin.get(TenantBinding_.virtualHost),
				virtualHost
			)
		);

		criteriaQuery.where(conjunction);

	}


	protected Uni<TenantWithBucket> getTenantAndFetchRelations(
		String virtualHost, boolean suggestion, long suggestionCategoryId) {

		return tenantRegistry.getTenantByVirtualHost(virtualHost)
			.flatMap(tenant -> sf
				.withTransaction(tenant.schemaName(), (s, t) -> {

					CriteriaBuilder criteriaBuilder = sf.getCriteriaBuilder();

					CriteriaQuery<Bucket> criteriaQuery =
						criteriaBuilder.createQuery(Bucket.class);

					customizeCriteriaBuilder(
						virtualHost, criteriaBuilder, criteriaQuery, suggestion,
						suggestionCategoryId);

					return s
						.createQuery(criteriaQuery)
						.getSingleResultOrNull();
					}
				)
				.map(bucket -> {
					if (bucket == null) {
						log.warn("It's not possible to find a valid active bucket.");
					}
					return new TenantWithBucket(tenant, bucket);
				})
			);

	}

}
