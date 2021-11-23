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
import io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig;
import io.openk9.search.query.internal.query.parser.util.Utils;
import io.vavr.Tuple2;
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

					Grammar grammar = _grammarProvider.getGrammar();

					Mono<List<Parse>> parsesMono = grammar.parseInput(
						t2.getT1(), searchText);

					return parsesMono
						.take(_annotatorConfig.timeout())
						.switchIfEmpty(Mono.just(List.of()))
						.map(parses -> {

						_log.info("parses count: " + parses.size());

						Set<Tuple2<Tuple<Integer>, Map<String, Object>>> set =
							new TreeSet<>(new ScoreComparator());

						for (Map.Entry<Tuple<Integer>, Map<String, Object>> e : chart.entrySet()) {
							set.add(io.vavr.Tuple.of(e.getKey(), e.getValue()));
						}

						for (Parse pars : parses) {
							for (SemanticType maps : pars.getSemantics().apply()) {
								for (Map<String, Object> map : maps) {
									Object tokenType = map.get("tokenType");
									if (!tokenType.equals("TOKEN")) {
										set.add(io.vavr.Tuple.of(maps.getPos(), map));
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
										Tuple2::_1,
										Collectors.mapping(
											Tuple2::_2, Collectors.toList())
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

	public static class ScoreComparator
		implements Comparator<Tuple2<Tuple<Integer>, Map<String, Object>>> {

		@Override
		public int compare(
			Tuple2<Tuple<Integer>, Map<String, Object>> to1, Tuple2<Tuple<Integer>, Map<String, Object>> to2) {

			Map<String, Object> o1 = to1._2();
			Map<String, Object> o2 = to2._2();

			Set<String> lKeys = o1.keySet();
			Set<String> rKeys = o2.keySet();

			Set<String> keys = lKeys.size() >= rKeys.size() ? lKeys : rKeys;

			double scoreO1 = toDouble(o1.getOrDefault("score", -1.0));
			double scoreO2 = toDouble(o2.getOrDefault("score", -1.0));

			int scoreCompared = -Double.compare(scoreO1, scoreO2);

			for (String key : keys) {
				if (!key.equals("score")) {
					Object lValue = o1.get(key);
					Object rValue = o2.get(key);
					if (!Objects.deepEquals(lValue, rValue)) {

						if (scoreCompared != 0) {
							return scoreCompared;
						}

						return -1;
					}
				}
			}

			return 0;

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
