package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Tenant;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.search.api.query.parser.Tuple;
import io.openk9.search.query.internal.query.parser.Grammar;
import io.openk9.search.query.internal.query.parser.GrammarProvider;
import io.openk9.search.query.internal.query.parser.Parse;
import io.openk9.search.query.internal.query.parser.SemanticType;
import io.openk9.search.query.internal.query.parser.SemanticTypes;
import io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig;
import io.openk9.search.query.internal.query.parser.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class QueryAnalysisHttpHandler implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router.post("/v1/query-analysis", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpServerRequest,
		HttpServerResponse httpServerResponse) {

		Mono<QueryAnalysisRequest> requestMono =
			ReactorNettyUtils
				.aggregateBodyAsByteArray(httpServerRequest)
				.map(bytes -> _jsonFactory.fromJson(bytes,
					QueryAnalysisRequest.class));

		String hostName = HttpUtil.getHostName(httpServerRequest);

		Mono<Long> tenantIdMono =
			_datasourceClient
				.findTenantByVirtualHost(hostName)
				.next()
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.map(Tenant::getTenantId);

		Mono<QueryAnalysisResponse> response =
			Mono.zip(tenantIdMono, requestMono)
				.flatMap(t2 -> Mono.defer(() -> {

					QueryAnalysisRequest queryAnalysisRequest = t2.getT2();

					String searchText = queryAnalysisRequest.getSearchText();

					String[] tokens = Utils.split(searchText);

					List<QueryAnalysisHttpHandler.QueryAnalysisToken> requestTokens =
						queryAnalysisRequest.getTokens();

					Map<Tuple<Integer>, Map<String, Object>> chart =
						_getRequestTokensMap(searchText, requestTokens);

					Grammar grammar = _grammarProvider.getGrammar();

					Mono<List<Parse>> parsesMono = grammar.parseInput(
						t2.getT1(), searchText);

					return parsesMono
						.take(_annotatorConfig.timeout())
						.switchIfEmpty(Mono.just(List.of()))
						.map(parses -> {

						_log.info("parses count: " + parses.size());

						Set<SemanticsPos> set = new TreeSet<>(
							new ScoreComparator());

						for (Map.Entry<Tuple<Integer>, Map<String, Object>> e : chart.entrySet()) {
							set.add(SemanticsPos.of(e.getKey(), e.getValue()));
						}

						for (int i = parses.size() - 1; i >= 0; i--) {
							SemanticTypes semanticTypes =
								parses.get(i).getSemantics().apply();

							List<SemanticType> semanticTypeList =
								semanticTypes.getSemanticTypes();

							for (SemanticType maps : semanticTypeList) {
								for (Map<String, Object> map : maps) {
									Object tokenType = map.get("tokenType");
									if (!tokenType.equals("TOKEN")) {
										set.add(SemanticsPos.of(maps.getPos(), map));
									}
								}
							}
						}

						List<QueryAnalysisTokens> result = new ArrayList<>(set.size());

						Map<Tuple<Integer>, List<Map<String, Object>>> collect =
							set
								.stream()
								.collect(
									Collectors.groupingBy(
										SemanticsPos::getPos,
										Collectors.mapping(
											SemanticsPos::getSemantics,
											Collectors.toList())
									)
								);

						for (Map.Entry<Tuple<Integer>, List<Map<String, Object>>> entry : collect.entrySet()) {

							Integer startPos =
								entry.getKey().getOrDefault(0, -1);

							Integer endPos =
								entry.getKey().getOrDefault(1, -1);

							String text = Arrays
								.stream(tokens, startPos, endPos)
								.collect(Collectors.joining(" "));

							int indexOf = searchText.indexOf(text, startPos);

							result.add(
								QueryAnalysisTokens.of(
									text,
									indexOf,
									indexOf + text.length(),
									entry.getValue())
							);
						}

						return QueryAnalysisResponse.of(
							searchText,
							result
						);

					});

				})
			);

		return _httpResponseWriter.write(
			httpServerResponse,
			response
		);

	}

	private Map<Tuple<Integer>, Map<String, Object>> _getRequestTokensMap(
		String searchText, List<QueryAnalysisToken> requestTokens) {

		Map<Tuple<Integer>, Map<String, Object>> chart;

		if (!requestTokens.isEmpty()) {

			chart = new HashMap<>();

			for (QueryAnalysisToken token : requestTokens) {

				String analyzed =
					searchText
						.substring(
							token.getStart(), token.getEnd());

				String prefix =
					searchText.substring(0, token.getStart());

				long prefixCount =
					prefix
						.codePoints()
						.filter(Character::isWhitespace)
						.count();

				String[] splitAnalyzed = Utils.split(analyzed);

				Tuple<Integer> pos =
					Tuple.of(
						(int)prefixCount,
						(int)(prefixCount + splitAnalyzed.length));

				Map<String, Object> copy = new HashMap<>(
					token.getToken());

				copy.put("score", 100.0f);

				chart.put(pos, copy);

			}
		}
		else {
			chart = Map.of();
		}
		return chart;
	}

	@Reference
	private GrammarProvider _grammarProvider;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private AnnotatorConfig _annotatorConfig;

	private static final Logger _log = LoggerFactory.getLogger(
		QueryAnalysisHttpHandler.class);

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class QueryAnalysisRequest {
		private String searchText;
		private List<QueryAnalysisToken> tokens;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class QueryAnalysisToken {
		private String text;
		private Integer start;
		private Integer end;
		private Map<String, Object> token;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@EqualsAndHashCode
	public static class QueryAnalysisTokens {
		private String text;
		private Integer start;
		private Integer end;
		private Collection<Map<String, Object>> tokens;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class QueryAnalysisResponse {
		private String searchText;
		private List<QueryAnalysisTokens> analysis;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class SemanticsPos {
		private Tuple<Integer> pos;
		private Map<String, Object> semantics;
	}

	public static class ScoreComparator implements Comparator<SemanticsPos> {

		@Override
		public int compare(
			SemanticsPos to1,
			SemanticsPos to2) {

			Map<String, Object> o1 = to1.semantics;
			Map<String, Object> o2 = to2.semantics;

			Object tokenType1 = o1.get("tokenType");
			Object tokenType2 = o2.get("tokenType");

			if (!Objects.equals(tokenType1, tokenType2)) {

				int scoreCompared = _getScoreCompared(o1, o2);

				if (scoreCompared != 0) {
					return scoreCompared;
				}
				return 1;
			}

			Object value1 = o1.get("value");
			Object value2 = o2.get("value");

			if (!Objects.deepEquals(value1, value2)) {

				int scoreCompared = _getScoreCompared(o1, o2);

				if (scoreCompared != 0) {
					return scoreCompared;
				}
				return 1;
			}

			return 0;

		}

		private int _getScoreCompared(
			Map<String, Object> o1, Map<String, Object> o2) {

			double scoreO1 = toDouble(o1.getOrDefault("score", -1.0));
			double scoreO2 = toDouble(o2.getOrDefault("score", -1.0));

			return -Double.compare(scoreO1, scoreO2);

		}

		double toDouble(Object score) {
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

	}

}
