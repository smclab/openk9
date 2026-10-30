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

package io.openk9.datasource.searcher.parser.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Named("HybridQueryParser")
public class HybridQueryParser implements QueryParser {

	static final String CHUNK_TEXT = "chunkText";
	static final int DEFAULT_PAGINATION_DEPTH = 100;
	/**
	 * The default {@code index.max_result_window} of OpenSearch, above which
	 * it rejects the pagination depth.
	 */
	static final int MAX_PAGINATION_DEPTH = 10000;
	/**
	 * The page size OpenSearch applies when the request sets none.
	 */
	private static final int DEFAULT_PAGE_SIZE = 10;

	@Inject
	EmbeddingService embeddingService;

	/**
	 * Adds the textual branch of the hybrid query to the shared bool query,
	 * as the search path of the suggestions does.
	 * <p>
	 * The suggestions count only the documents the textual branch matches:
	 * the knn branch would cost an embedding call for each request of
	 * suggestions, to add at most {@code kNeighbors} documents per shard.
	 */
	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		var jsonConfig = parserContext.getQueryParserConfig();
		var parserSearchToken = parserContext.getTokenTypeGroup().iterator().next();

		var values = parserSearchToken.getValues().iterator();

		if (values.hasNext()) {
			var textQuery = toTextQuery(
				parserContext,
				parserSearchToken,
				jsonConfig,
				values.next(),
				TextQueryParser.getFuzziness(parserSearchToken, jsonConfig),
				TextQueryParser.getBoost(parserSearchToken, jsonConfig)
			);

			parserContext.getMutableQuery().must(
				OpenSearchUtils.toWrapperQueryBuilder(textQuery));
		}

		return Uni.createFrom().voidItem();
	}

	@Override
	public QueryParserType getType() {
		return QueryParserType.HYBRID;
	}

	/**
	 * Sets on the search source the hybrid query of the token, whose two
	 * branches both apply the base query of the context, that is the
	 * filters and the ACL built from the other tokens of the request.
	 * <p>
	 * A token without text sets the base query alone, so that the filters
	 * and the ACL still apply. A context without a base query fails: the
	 * search would run without the ACL.
	 */
	public Uni<SearchSourceBuilder> apply(
		ParserContext parserContext, SearchSourceBuilder searchSourceBuilder) {

		var jsonConfig = parserContext.getQueryParserConfig();
		var parserSearchToken = parserContext.getTokenTypeGroup().iterator().next();

		var tenant = parserContext.getTenantWithBucket().getTenant();
		var tenantId = tenant.schemaName();

		var kNeighbors = KnnQueryParser.getKNeighbors(parserSearchToken, jsonConfig);
		var boost = TextQueryParser.getBoost(parserSearchToken, jsonConfig);
		var fuzziness = TextQueryParser.getFuzziness(parserSearchToken, jsonConfig);

		var values = parserSearchToken.getValues().iterator();

		var baseQuery = Objects.requireNonNull(
			parserContext.getMutableQuery(),
			"A hybrid search needs the base query holding the ACL.");

		var paginationDepth = getPaginationDepth(jsonConfig, searchSourceBuilder);

		if (values.hasNext()) {
			var value = values.next();

			var textQuery = toTextQuery(
				parserContext,
				parserSearchToken,
				jsonConfig,
				value,
				fuzziness,
				boost
			);

			return embeddingService.getEmbeddedText(tenantId, value)
				.map(embeddedText -> KnnQueryParser.toKnnQuery(embeddedText, kNeighbors))
				.map(knnQuery -> toHybridQuery(
					baseQuery, textQuery, knnQuery, paginationDepth))
				.map(searchSourceBuilder::query);

		}

		return Uni.createFrom().item(searchSourceBuilder.query(baseQuery));
	}

	/**
	 * Composes the hybrid query as json, inside a single wrapper query.
	 * <p>
	 * OpenSearch accepts the hybrid query only at the top level, so the base
	 * query cannot wrap it: it is applied inside each branch instead. The
	 * base query comes from the legacy client and the branches from
	 * opensearch-java, whose typed deserializer rejects the legacy json:
	 * the two meet as json.
	 *
	 * @param baseQuery the filters and the ACL of the request
	 * @param textQuery the textual query of the token
	 * @param knnQuery  the knn query of the token, without filter
	 * @param paginationDepth the hits each branch collects per shard
	 * @return the wrapper query holding the hybrid query
	 */
	static WrapperQueryBuilder toHybridQuery(
		BoolQueryBuilder baseQuery, Query textQuery, Query knnQuery,
		int paginationDepth) {

		var queries = new JsonArray()
			.add(toTextBranch(baseQuery, textQuery))
			.add(toKnnBranch(baseQuery, knnQuery));

		var hybridQuery = new JsonObject()
			.put("hybrid", new JsonObject()
				.put("queries", queries)
				.put("pagination_depth", paginationDepth));

		return QueryBuilders.wrapperQuery(hybridQuery.encode());
	}

	/**
	 * Decides how many hits each branch of the hybrid query collects per
	 * shard, before the scores are normalized and combined.
	 * <p>
	 * OpenSearch requires the depth to serve any page after the first one,
	 * and normalizes the scores over the hits collected: the depth stays the
	 * same across the pages, so that the pages follow one another without
	 * duplicates or gaps. It grows only for the pages beyond it, and stops at
	 * the result window of OpenSearch.
	 * <p>
	 * Only the configuration sets the depth, unlike the other keys a token
	 * may override: it drives the cost of every page.
	 *
	 * @param searchSourceBuilder the search source holding the page requested
	 * @return the pagination depth of the hybrid query
	 */
	static int getPaginationDepth(
		JsonObject jsonConfig, SearchSourceBuilder searchSourceBuilder) {

		// the configuration may hold the depth as a number or as a string
		var paginationDepth = switch (jsonConfig.getValue("paginationDepth")) {
			case Number number -> number.intValue();
			case String string when !string.isBlank() ->
				Integer.parseInt(string.trim());
			case null, default -> DEFAULT_PAGINATION_DEPTH;
		};

		var from = Math.max(searchSourceBuilder.from(), 0);
		var size = searchSourceBuilder.size() < 0
			? DEFAULT_PAGE_SIZE
			: searchSourceBuilder.size();

		return Math.min(
			Math.max(paginationDepth, from + size), MAX_PAGINATION_DEPTH);
	}

	/**
	 * Builds the textual branch as the base query plus the textual query as
	 * a must clause, the same query a TEXT search runs.
	 *
	 * @param baseQuery the filters and the ACL of the request
	 * @param textQuery the textual query of the token
	 * @return the json of the textual branch
	 */
	static JsonObject toTextBranch(BoolQueryBuilder baseQuery, Query textQuery) {

		var textBranch = new JsonObject(baseQuery.toString());

		addClause(textBranch, "must", toJson(textQuery));

		return textBranch;
	}

	/**
	 * Builds the knn branch with the clauses that restrict the base query as
	 * the native filter of the knn query, so that the k neighbors are
	 * searched among the documents the request admits.
	 * <p>
	 * The scoring clauses of the base query, must and should, surround the
	 * knn query as in a KNN search, so that a document scores as it does
	 * there.
	 *
	 * @param baseQuery the filters and the ACL of the request
	 * @param knnQuery  the knn query of the token, without filter
	 * @return the json of the knn branch
	 */
	static JsonObject toKnnBranch(BoolQueryBuilder baseQuery, Query knnQuery) {

		var knnJson = toJson(knnQuery);

		var knnFilter = toKnnFilter(baseQuery);

		if (knnFilter.hasClauses()) {
			knnJson
				.getJsonObject("knn")
				.getJsonObject("vector")
				.put("filter", new JsonObject(knnFilter.toString()));
		}

		if (baseQuery.must().isEmpty() && baseQuery.should().isEmpty()) {
			return knnJson;
		}

		var scoringQuery = QueryBuilders.boolQuery()
			.minimumShouldMatch(baseQuery.minimumShouldMatch());

		baseQuery.must().forEach(scoringQuery::must);
		baseQuery.should().forEach(scoringQuery::should);

		var knnBranch = new JsonObject(scoringQuery.toString());

		addClause(knnBranch, "must", knnJson);

		return knnBranch;
	}

	/**
	 * Collects the clauses of the base query that restrict the documents:
	 * filter and must in filter context, must_not as they are.
	 * <p>
	 * The should clauses restrict only with a minimum should match: without
	 * it they would turn, alone in a filter, from an optional boost into a
	 * mandatory condition.
	 *
	 * @param baseQuery the filters and the ACL of the request
	 * @return the filter of the knn query, with no clause when nothing restricts
	 */
	static BoolQueryBuilder toKnnFilter(BoolQueryBuilder baseQuery) {

		var knnFilter = QueryBuilders.boolQuery();

		baseQuery.filter().forEach(knnFilter::filter);
		baseQuery.must().forEach(knnFilter::filter);
		baseQuery.mustNot().forEach(knnFilter::mustNot);

		if (baseQuery.minimumShouldMatch() != null) {
			baseQuery.should().forEach(knnFilter::should);
			knnFilter.minimumShouldMatch(baseQuery.minimumShouldMatch());
		}

		return knnFilter;
	}

	private static void addClause(
		JsonObject boolQuery, String occur, JsonObject clause) {

		var bool = boolQuery.getJsonObject("bool");

		var clauses = bool.getJsonArray(occur);

		if (clauses == null) {
			clauses = new JsonArray();
			bool.put(occur, clauses);
		}

		clauses.add(clause);
	}

	private static JsonObject toJson(Query query) {

		try (var os = new ByteArrayOutputStream()) {

			var generator = jakarta.json.Json.createGenerator(os);

			query.serialize(generator, new JacksonJsonpMapper());

			generator.close();

			return new JsonObject(os.toString());
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Builds the textual branch of the hybrid query as a multi match over the
	 * searchable text fields of the bucket, each with its configured boost,
	 * plus the {@code chunkText} field holding the embedded chunk.
	 * <p>
	 * When the bucket exposes no searchable text field, it falls back to a
	 * plain match on {@code chunkText}.
	 *
	 * @param textQueryValue the search text, already truncated if needed
	 * @return the textual query to combine with the knn one
	 */
	static Query toTextQuery(
		ParserContext parserContext,
		ParserSearchToken parserSearchToken,
		JsonObject jsonConfig,
		String textQueryValue,
		Fuzziness fuzziness,
		float boost) {

		var bucket = parserContext.getTenantWithBucket().getBucket();
		var language = parserContext.getLanguage();

		var searchableFields = Utils.getDocTypeFieldsFrom(bucket.getDatasources())
			.filter(DocTypeField::isSearchableAndText)
			.filter(docTypeField -> TextQueryParser.i18nFilter(docTypeField, language))
			.map(HybridQueryParser::toFieldWithBoost)
			.distinct()
			.toList();

		if (searchableFields.isEmpty()) {
			return new MatchQuery.Builder()
				.field(CHUNK_TEXT)
				.query(q -> q.stringValue(textQueryValue))
				.fuzziness(fuzziness.asString())
				.lenient(true)
				.boost(boost)
				.build()
				.toQuery();
		}

		var fields = new ArrayList<String>();
		fields.add(CHUNK_TEXT);
		fields.addAll(searchableFields);

		var multiMatchType = TextQueryParser.getMultiMatchType(
			parserSearchToken, jsonConfig);

		var tieBreaker = TextQueryParser.getTieBreaker(
			parserSearchToken, jsonConfig);

		return new MultiMatchQuery.Builder()
			.fields(fields)
			.query(textQueryValue)
			.type(toTextQueryType(multiMatchType))
			.tieBreaker((double) tieBreaker)
			.fuzziness(fuzziness.asString())
			.lenient(true)
			.boost(boost)
			.build()
			.toQuery();
	}

	private static String toFieldWithBoost(DocTypeField docTypeField) {
		var path = docTypeField.getPath();

		return docTypeField.isDefaultBoost()
			? path + "^" + docTypeField.getFloatBoost()
			: path;
	}

	private static TextQueryType toTextQueryType(
		MultiMatchQueryBuilder.Type type) {

		return switch (type) {
			case BEST_FIELDS -> TextQueryType.BestFields;
			case MOST_FIELDS -> TextQueryType.MostFields;
			case CROSS_FIELDS -> TextQueryType.CrossFields;
			case PHRASE -> TextQueryType.Phrase;
			case PHRASE_PREFIX -> TextQueryType.PhrasePrefix;
			case BOOL_PREFIX -> TextQueryType.BoolPrefix;
		};
	}

}
