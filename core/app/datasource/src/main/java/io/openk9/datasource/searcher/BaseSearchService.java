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

import com.google.protobuf.ByteString;
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
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SearchConfig_;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.mapper.SearcherMapper;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseSearchService {

	protected Uni<TenantResponse> getTenant(String virtualHost) {
		return tenantManager.findTenant(TenantRequest.newBuilder()
			.setVirtualHost(virtualHost)
			.build());
	}

	protected Uni<Bucket> getTenantAndFetchRelations(
		String virtualHost, boolean suggestion, long suggestionCategoryId) {

		return getTenant(virtualHost)
			.flatMap(tenantResponse -> sf
				.withTransaction(tenantResponse.getSchemaName(), (s, t) -> {

					CriteriaBuilder criteriaBuilder = sf.getCriteriaBuilder();

					CriteriaQuery<Bucket> criteriaQuery =
						criteriaBuilder.createQuery(Bucket.class);

					customizeCriteriaBuilder(
						virtualHost, criteriaBuilder, criteriaQuery, suggestion,
						suggestionCategoryId);

					return s
						.createQuery(criteriaQuery)
						.getSingleResultOrNull();
				}));


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

		Predicate disjunction = criteriaBuilder.conjunction();

		List<Expression<Boolean>> expressions = disjunction.getExpressions();

		if (suggestion) {

			Fetch<Bucket, SuggestionCategory> suggestionCategoryFetch =
				tenantRoot.fetch(Bucket_.suggestionCategories);

			Fetch<SuggestionCategory, DocTypeField> categoryDocTypeFieldFetch =
				suggestionCategoryFetch
					.fetch(SuggestionCategory_.docTypeFields);

			categoryDocTypeFieldFetch
				.fetch(DocTypeField_.parentDocTypeField, JoinType.LEFT);

			if (suggestionCategoryId > 0) {

				expressions.add(
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

		dataIndexRoot.fetch(DataIndex_.vectorIndex, JoinType.LEFT);

		expressions.add(
			criteriaBuilder.equal(
				tenantBindingJoin.get(TenantBinding_.virtualHost),
				virtualHost
			)
		);

		criteriaQuery.where(disjunction);

		criteriaQuery.distinct(true);

	}

	protected Uni<BoolQueryBuilder> createBoolQuery(
		Map<String, List<ParserSearchToken>> tokenGroup, Bucket bucket,
		JWT jwt, Map<String, List<String>> extraParams, String language) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolean hasToken = tokenGroup != null && !tokenGroup.isEmpty();

		List<Uni<Void>> queryParserUnis = new ArrayList<>();

		if (hasToken) {

			for (Map.Entry<String, List<ParserSearchToken>> entry : tokenGroup.entrySet()) {
				String tokenType = entry.getKey();
				List<ParserSearchToken> parserSearchTokens =
					entry.getValue();
				for (QueryParser queryParser : queryParserInstance) {
					if (queryParser.isQueryParserGroup() &&
						queryParser.getType().equals(tokenType)) {
						var queryParserUni = queryParser.apply(
							ParserContext
								.builder()
								.tokenTypeGroup(parserSearchTokens)
								.mutableQuery(boolQueryBuilder)
								.currentTenant(bucket)
								.queryParserConfig(getQueryParserConfig(bucket, tokenType))
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
						.currentTenant(bucket)
						.queryParserConfig(
							getQueryParserConfig(
								bucket, queryParser.getType()))
						.jwt(jwt)
						.extraParams(extraParams)
						.build()
				);

				queryParserUnis.add(queryParserUni);

			}
		}

		return Uni.join().all(queryParserUnis).andCollectFailures().map(voids -> boolQueryBuilder);
	}

	protected Map<String, List<ParserSearchToken>> createTokenGroup(
		QueryParserRequest request) {

		Map<String, List<ParserSearchToken>> tokenGroup =
			request
				.getSearchQueryList()
				.stream()
				.map(searcherMapper::toParserSearchToken)
				.collect(
					Collectors.groupingBy(ParserSearchToken::getTokenType));

		return tokenGroup;

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

	public static JsonObject getQueryParserConfig(Bucket bucket, String tokenType) {

		SearchConfig searchConfig = bucket.getSearchConfig();

		if (searchConfig == null) {
			return EMPTY_JSON;
		}

		return searchConfig
			.getQueryParserConfigs()
			.stream()
			.filter(queryParserConfig -> queryParserConfig.getType().equals(
				tokenType))
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

	@Inject
	Mutiny.SessionFactory sf;

	@Inject
	Instance<QueryParser> queryParserInstance;

	@Inject
	SearcherMapper searcherMapper;

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	private static final JsonObject EMPTY_JSON = new JsonObject(Map.of());

}
