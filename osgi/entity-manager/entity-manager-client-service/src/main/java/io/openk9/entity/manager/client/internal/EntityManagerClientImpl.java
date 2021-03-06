package io.openk9.entity.manager.client.internal;

import io.openk9.entity.manager.client.api.EntityManagerClient;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component(
	immediate = true,
	service = EntityManagerClient.class
)
public class EntityManagerClientImpl implements EntityManagerClient {

	@interface Config {
		String url() default "http://entity-manager";
	}

	@Activate
	void activate(Config config) {

		String url = config.url();

		_entityManagerHttpClient = _httpClientFactory.getHttpClient(url);
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_entityManagerHttpClient = null;
	}

	@Override
	public Mono<ResponseList> getOrAddEntities(Request request) {
		return Mono
			.from(
				_entityManagerHttpClient
					.request(
						HttpHandler.POST,
						"/get-or-add-entities",
						_jsonFactory.toJson(request),
						Map.of()
					)
			)
			.map(bytes -> _jsonFactory.fromJson(new String(bytes), ResponseList.class));
	}

	private HttpClient _entityManagerHttpClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpClientFactory _httpClientFactory;

}
