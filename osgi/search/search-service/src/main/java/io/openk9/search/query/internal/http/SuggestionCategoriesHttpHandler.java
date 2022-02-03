package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.model.Tenant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.Function;

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

		String hostName = HttpUtil.getHostName(httpServerRequest);

		String categoryId = httpServerRequest.param("categoryId");

		Function<Long, Mono<?>> response;

		if (categoryId != null) {
			response = (tenantId) -> _datasourceClient.findSuggestionCategoryByTenantIdAndCategoryId(
				tenantId, Long.parseLong(categoryId));
		}
		else {
			response = (tenantId) -> _datasourceClient.findSuggestionCategories(tenantId);
		}

		return _httpResponseWriter.write(
			httpServerResponse,
			_datasourceClient
				.findTenantByVirtualHost(hostName)
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.map(Tenant::getTenantId)
				.map(response)
		);

	}

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
