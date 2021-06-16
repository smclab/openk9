package io.openk9.index.writer.entity.client.internal;

import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.index.writer.entity.client.api.IndexWriterEntityClient;
import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.index.writer.entity.model.DocumentEntityResponse;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = IndexWriterEntityClient.class
)
public class IndexWriterEntityClientImpl implements IndexWriterEntityClient {

	@interface Config {
		String url() default "http://index-writer";
	}

	@Activate
	void activate(Config config) {

		String url = config.url();

		_indexWriterHttpClient = _httpClientFactory.getHttpClient(url);
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_indexWriterHttpClient = null;
	}

	@Override
	public Mono<Void> insertEntity(DocumentEntityRequest documentEntityRequest) {
		return insertEntities(List.of(documentEntityRequest));
	}

	@Override
	public Mono<Void> insertEntities(
		Collection<DocumentEntityRequest> documentEntityRequestList) {
		return Mono
			.from(
				_indexWriterHttpClient
					.request(
						HttpHandler.POST,
						"/v1/",
						_jsonFactory.toJson(documentEntityRequestList),
						Map.of()
					)
			)
			.then();
	}

	@Override
	public Mono<List<DocumentEntityResponse>> getEntities(long tenantId, Map<String, Object> request) {
		return Mono
			.from(
				_indexWriterHttpClient
					.request(
						HttpHandler.POST,
						"/v1/get-entities/" + tenantId,
						_jsonFactory.toJson(request),
						Map.of()
					)
			)
			.map(bytes -> _jsonFactory.fromJsonList(bytes, DocumentEntityResponse.class));
	}

	private HttpClient _indexWriterHttpClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpClientFactory _httpClientFactory;
}
