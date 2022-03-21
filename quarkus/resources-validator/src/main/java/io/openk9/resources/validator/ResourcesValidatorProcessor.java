package io.openk9.resources.validator;

import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@ApplicationScoped
@Startup
public class ResourcesValidatorProcessor {

	@Incoming("resources-validator-incoming")
	public CompletionStage<Void> consume(Message<?> message) {

		Object obj = message.getPayload();

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject) obj
				: new JsonObject(new String((byte[]) obj));

		String replyTo = jsonObject.getString("replyTo");

		JsonObject payload = jsonObject.getJsonObject("payload");

		Long tenantId = payload.getLong("tenantId");

		Long datasourceId = payload.getLong("datasourceId");

		String contentId = payload.getString("contentId");

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(matchQuery("datasourceId", datasourceId));

		boolQueryBuilder.must(matchQuery("contentId", contentId));

		boolQueryBuilder.must(existsQuery("hashCodes"));

		SearchRequest searchRequest = new SearchRequest(tenantId + "-*-data");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(boolQueryBuilder);

		searchSourceBuilder.fetchField("hashCodes");

		searchRequest.source(searchSourceBuilder);

		JsonArray binaries =
			payload
				.getJsonObject("resources")
				.getJsonArray("binaries");

		String rawContent = payload.getString("rawContent");

		List<Integer> hashCodes = _getHashCodes(rawContent, binaries);

		try {

			SearchResponse searchResponse =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : searchResponse.getHits()) {

				Map<String, Object> sourceAsMap = hit.getSourceAsMap();

				Object documentHashCodes = sourceAsMap.get("hashCodes");

				if (documentHashCodes instanceof Collection) {

					Collection<Integer> documentHashCodesList =
						(Collection<Integer>)documentHashCodes;

					if (hashCodes.size() == documentHashCodesList.size() &&
						hashCodes.containsAll(documentHashCodesList)) {
						logger.info(
							"document found. dropped message with contentId: "
							+ contentId);
						return message.ack();
					}

				}

			}

			payload.put("hashCodes", hashCodes);

			emitter.send(
				Message.of(
					jsonObject,
					Metadata.of(
						new OutgoingRabbitMQMetadata.Builder()
							.withRoutingKey(replyTo)
							.withContentType("application/json")
							.build()
					)
				)
			);

			return message.ack();

		}
		catch (IOException e) {
			// logger.error(e.getMessage(), e);
			return message.nack(e);
		}

	}

	private List<Integer> _getHashCodes(String rawContent, JsonArray binaries) {

		if (rawContent == null && (binaries == null || binaries.isEmpty())) {
			return List.of();
		}

		List<Integer> hashCodes = new ArrayList<>();

		if (binaries != null) {

			for (int i = 0; i < binaries.size(); i++) {

				String dataBase64 =
					binaries.getJsonObject(i).getString("data");

				hashCodes.add(dataBase64.hashCode());
			}

		}

		if (rawContent != null) {
			hashCodes.add(rawContent.hashCode());
		}

		return hashCodes;
	}

	@Inject
	@Channel("resources-validator-outgoing")
	Emitter<JsonObject> emitter;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Logger logger;

}
