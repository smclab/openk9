package io.openk9.search.query.internal.http;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.search.query.internal.query.parser.Grammar;
import io.openk9.search.query.internal.query.parser.GrammarProvider;
import io.openk9.search.query.internal.query.parser.Parse;
import io.openk9.search.query.internal.query.parser.SemanticType;
import io.openk9.search.query.internal.query.parser.SemanticTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		Mono<QueryAnalysisResponse> response =
			requestMono
				.flatMap(
					queryAnalysisRequest ->
						Mono.fromSupplier(() -> {

							Grammar grammar = _grammarProvider.getGrammar();

							List<Parse> parses = grammar.parseInput(
								queryAnalysisRequest.getSearchText());

							List<QueryAnalysisTokens> tokens = new ArrayList<>();

							for (Parse pars : parses) {

								SemanticTypes semanticTypes =
									pars.getSemantics().apply();

								for (SemanticType semanticType : semanticTypes) {

									for (Map<String, Object> map : semanticType) {

										tokens.add(
											QueryAnalysisTokens.of(
												"", -1, -1, List.of(map)
											)
										);

									}


								}

							}

							return QueryAnalysisResponse.of(
								queryAnalysisRequest.getSearchText(),
								tokens
							);

						})
						.subscribeOn(Schedulers.boundedElastic())
				);

		return _httpResponseWriter.write(httpServerResponse, response);

	}

	@Reference
	private GrammarProvider _grammarProvider;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private JsonFactory _jsonFactory;

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
		private int start;
		private int end;
		private Map<String, Object> token;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class QueryAnalysisTokens {
		private String text;
		private int start;
		private int end;
		private List<Map<String, Object>> tokens;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class QueryAnalysisResponse {
		private String searchText;
		private List<QueryAnalysisTokens> analysis;
	}

}
