package io.openk9.entity.manager.logic;


import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class GetOrAddEntitiesHttpHandler implements HttpHandler {

	private Scheduler _scheduler;

	@Activate
	void activate() {
		_scheduler =
			Schedulers.newSingle("get-or-add-entities-single-pool");
	}

	@Deactivate
	void deactivate() {
		_scheduler.dispose();
	}

	@Override
	public String getPath() {
		return "/get-or-add-entities";
	}

	@Override
	public int method() {
		return POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<ResponseList> response =
			Mono
				.from(httpRequest.aggregateBodyToString())
				.map(body -> _jsonFactory.fromJson(body, Request.class))
				.map(MessageRequest::of)
				.subscribeOn(_scheduler)
				.flatMap(_getOrAddEntities::handleMessage);

		return _httpResponseWriter.write(
			httpResponse, response);
	}


	@Reference
	private GetOrAddEntities _getOrAddEntities;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
