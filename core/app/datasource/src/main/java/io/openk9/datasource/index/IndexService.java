package io.openk9.datasource.index;

import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpEntity;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class IndexService {

	public Uni<Map<String, Object>> getMappings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return client.indices().getMapping(
						new GetMappingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.mappings().get(indexName).sourceAsMap());
	}

	public Uni<String> getSettings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return client.indices().getSettings(
						new GetSettingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.getIndexToSettings().get(indexName).toString());
	}

	public Uni<CatResponse> get_catIndicesFirst(String indexName) {
		return get_catIndices(indexName).map(catResponses -> {
			if (catResponses.isEmpty()) {
				return null;
			}
			return catResponses.get(0);
		});
	}

	public Uni<List<CatResponse>> get_catIndices(Collection<String> indexNames) {
		return get_catIndices(indexNames.toArray(String[]::new));
	}

	public Uni<List<CatResponse>> get_catIndices(String...indexNames) {

		String indexName =
			indexNames == null || indexNames.length == 0
				? ""
				: String.join(",", indexNames);

		return Uni
			.createFrom()
			.<Response>emitter((sink) -> {

				RestClient lowLevelClient = client.getLowLevelClient();

				Request
					catRequest = new Request("GET", "/_cat/indices/" + indexName);

				catRequest.addParameter("format", "JSON");
				catRequest.addParameter("v", "true");

				lowLevelClient.performRequestAsync(
					catRequest,
					new ResponseListener() {
						@Override
						public void onSuccess(Response response) {
							sink.complete(response);
						}

						@Override
						public void onFailure(Exception exception) {
							sink.fail(exception);
						}
					});
			})
			.map(IndexService::responseToCatResponses);
	}

	private static List<CatResponse> responseToCatResponses(Response response) {

		JsonArray catResponseArr = parseEntity(response.getEntity());

		List<CatResponse> catResponseList =
			new ArrayList<>(catResponseArr.size());

		for (Object catResponse : catResponseArr) {
			if (catResponse instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) catResponse;
				catResponseList.add(
					jsonObject.mapTo(CatResponse.class));
			}
		}

		return catResponseList;

	}

	private static JsonArray parseEntity(final HttpEntity entity) {
		if (entity == null) {
			throw new IllegalStateException("Response body expected but not returned");
		}
		if (entity.getContentType() == null) {
			throw new IllegalStateException("Elasticsearch didn't return the [Content-Type] header, unable to parse response body");
		}
		XContentType xContentType = XContentType.fromMediaTypeOrFormat(entity.getContentType().getValue());
		if (xContentType == null) {
			throw new IllegalStateException("Unsupported Content-Type: " + entity.getContentType().getValue());
		}

		try (InputStream inputStream = entity.getContent()) {
			String stringContent = new String(inputStream.readAllBytes());
			return new JsonArray(stringContent);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	public Uni<Long> indexCount(Collection<String> indexNames) {
		return indexCount(indexNames.toArray(String[]::new));
	}

	public Uni<Long> indexCount(String...indexName) {
		return Uni
			.createFrom()
			.<CountResponse>emitter(
				emitter -> client
					.countAsync(
						new CountRequest(indexName), RequestOptions.DEFAULT,
						UniActionListener.of(emitter)
					)
			)
			.map(CountResponse::getCount);
	}

	public Uni<Boolean> indexExist(String name) {
		return Uni
			.createFrom()
			.emitter(
				emitter -> client
					.indices()
					.existsAsync(
						new GetIndexRequest(name), RequestOptions.DEFAULT,
						UniActionListener.of(emitter)
					)
			);
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	Logger logger;

}
