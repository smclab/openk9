package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class SuggestionCategoriesHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router
			.get("/suggestion-categories", this)
			.get("/suggestion-categories/{categoryId}", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpServerRequest,
		HttpServerResponse httpServerResponse) {

		String categoryId = httpServerRequest.param("categoryId");

		Mono<?> response;

		if (categoryId != null) {
			response = _datasourceClient.findSuggestionCategoryWithFieldsById(
				NumberUtils.toLong(categoryId));
		}
		else {
			response = _datasourceClient.findSuggestionCategoriesWithFields();
		}

		return _httpResponseWriter.write(httpServerResponse,response);
	}

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
