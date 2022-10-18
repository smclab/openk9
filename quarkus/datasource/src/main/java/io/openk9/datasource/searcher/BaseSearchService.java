package io.openk9.datasource.searcher;

import com.google.protobuf.ByteString;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SearchConfig_;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.Tenant_;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.dto.ParserSearchToken;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.mapper.SearcherMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.io.stream.OutputStreamStreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseSearchService {

	protected Uni<Tenant> getTenantAndFetchRelations(
		String virtualHost, boolean suggestion, long suggestionCategoryId) {

		return sf
			.openStatelessSession()
			.flatMap(s -> {

				CriteriaBuilder criteriaBuilder = sf.getCriteriaBuilder();

				CriteriaQuery<Tenant> criteriaQuery =
					criteriaBuilder.createQuery(Tenant.class);

				customizeCriteriaBuilder(
					virtualHost, criteriaBuilder, criteriaQuery, suggestion,
					suggestionCategoryId);

				return s
					.createQuery(criteriaQuery)
					.setCacheable(true)
					.getSingleResultOrNull()
					.eventually(s::close);
			});

	}

	protected void customizeCriteriaBuilder(
		String virtualHost, CriteriaBuilder criteriaBuilder,
		CriteriaQuery<Tenant> criteriaQuery, boolean suggestion,
		long suggestionCategoryId) {

		Root<Tenant> tenantRoot = criteriaQuery.from(Tenant.class);

		tenantRoot
			.fetch(Tenant_.searchConfig, JoinType.LEFT)
			.fetch(SearchConfig_.queryParserConfigs, JoinType.LEFT);

		if (suggestion) {

			if (suggestionCategoryId > 0) {

				SetJoin<Tenant, SuggestionCategory> join =
					tenantRoot.join(
						Tenant_.suggestionCategories, JoinType.LEFT);

				join.on(
					criteriaBuilder.equal(
						join.get(SuggestionCategory_.id),
						suggestionCategoryId)
				);

			}

			tenantRoot
				.fetch(Tenant_.suggestionCategories, JoinType.LEFT)
				.fetch(SuggestionCategory_.docTypeFields, JoinType.LEFT);

		}

		Fetch<Tenant, Datasource> datasourceRoot =
			tenantRoot.fetch(Tenant_.datasources, JoinType.LEFT);

		Fetch<Datasource, DataIndex> dataIndexRoot =
			datasourceRoot.fetch(Datasource_.dataIndex, JoinType.LEFT);

		Fetch<DataIndex, DocType> docTypeFetch =
			dataIndexRoot.fetch(DataIndex_.docTypes, JoinType.LEFT);

		docTypeFetch.fetch(DocType_.docTypeFields, JoinType.LEFT);

		criteriaQuery.where(
			criteriaBuilder.equal(
				tenantRoot.get(Tenant_.virtualHost), virtualHost));

		criteriaQuery.distinct(true);

	}

	protected BoolQueryBuilder createBoolQuery(
		Map<String, List<ParserSearchToken>> tokenGroup, Tenant tenant) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (Map.Entry<String, List<ParserSearchToken>> entry : tokenGroup.entrySet()) {
			String tokenType = entry.getKey();
			List<ParserSearchToken> parserSearchTokens =
				entry.getValue();
			for (QueryParser queryParser : queryParserInstance) {
				if (queryParser.isQueryParserGroup() &&
					queryParser.getType().equals(tokenType)) {
					queryParser.accept(
						ParserContext
							.builder()
							.tokenTypeGroup(parserSearchTokens)
							.mutableQuery(boolQueryBuilder)
							.currentTenant(tenant)
							.queryParserConfig(
								getQueryParserConfig(
									tenant, tokenType))
							.build()
					);
				}
			}
		}

		List<ParserSearchToken> parserSearchTokens = null;

		for (QueryParser queryParser : queryParserInstance) {
			if (!queryParser.isQueryParserGroup()) {

				if (parserSearchTokens == null) {
					parserSearchTokens = tokenGroup
						.values()
						.stream()
						.flatMap(Collection::stream)
						.toList();
				}

				queryParser.accept(
					ParserContext
						.builder()
						.tokenTypeGroup(parserSearchTokens)
						.mutableQuery(boolQueryBuilder)
						.currentTenant(tenant)
						.queryParserConfig(
							getQueryParserConfig(
								tenant, queryParser.getType()))
						.build()
				);

			}
		}

		return boolQueryBuilder;
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

		String suggestKeyword = request.getSuggestKeyword();

		if (StringUtils.isNotBlank(suggestKeyword)) {

			List<ParserSearchToken> textTokens = tokenGroup.computeIfAbsent(
				ParserSearchToken.TEXT, k -> new ArrayList<>(1));

			textTokens.add(ParserSearchToken.ofText(suggestKeyword));

		}

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

	public static JsonObject getQueryParserConfig(Tenant tenant, String tokenType) {

		SearchConfig searchConfig = tenant.getSearchConfig();

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

	private static final JsonObject EMPTY_JSON = new JsonObject(Map.of());

}
