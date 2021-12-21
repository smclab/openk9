package io.openk9.vertx.configuration.plugin;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class HealthCheckEndpoint implements HttpHandler, RouterHandler {

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router.get("/monitoring/health", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpServerRequest,
		HttpServerResponse httpServerResponse) {

		return _httpResponseWriter.write(httpServerResponse, Status.of("UP"));

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class Status {
		private String status;
	}

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
