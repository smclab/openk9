package io.openk9.util.script.groovy.web;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.util.script.groovy.api.GroovyScriptExecutor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class GroovyScriptHttpHandler
	implements HttpHandler, RouterHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/script/groovy", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Mono<String> body =
			ReactorNettyUtils.aggregateBodyAsString(httpRequest);

		return _httpResponseWriter.write(
			httpResponse, Mono
				.from(body)
				.publishOn(Schedulers.boundedElastic())
				.map(_groovyScriptExecutor::execute)
				.map(String::new)
		);
	}

	@Reference
	private GroovyScriptExecutor _groovyScriptExecutor;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
