package io.openk9.util.script.groovy.web;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.util.script.groovy.api.GroovyScriptExecutor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class GroovyScriptHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/script/groovy";
	}

	@Override
	public int method() {
		return POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Publisher<String> request = httpRequest.aggregateBodyToString();

		return _httpResponseWriter.write(
			httpResponse, Mono
				.from(request)
				.map(_groovyScriptExecutor::execute)
				.map(String::new)
		);
	}

	@Reference
	private GroovyScriptExecutor _groovyScriptExecutor;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
