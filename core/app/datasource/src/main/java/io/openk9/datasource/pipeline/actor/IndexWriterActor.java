package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.serialization.jackson.JacksonObjectMapperProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IndexWriterActor {

	public sealed interface Command extends CborSerializable {}
	public record Start(DataIndex dataIndex, DataPayload dataPayload, ActorRef<Response> replyTo) implements Command {}
	private record SearchResponseCommand(DataIndex dataIndex, DataPayload dataPayload, ActorRef<Response> replyTo, SearchResponse searchResponse, Exception exception) implements Command {}
	private record BulkResponseCommand(ActorRef<Response> replyTo, BulkResponse bulkResponse, Exception exception) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public enum Success implements Response {INSTANCE}
	public record Failure(Exception exception) implements Response {}

	public static Behavior<Command> create() {

		return Behaviors.setup(ctx -> {

			RestHighLevelClient restHighLevelClient =
				CDI.current().select(RestHighLevelClient.class).get();

			return initial(ctx, restHighLevelClient);

		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient) {

		Logger logger = ctx.getLog();

		return Behaviors.receive(Command.class)
			.onMessage(Start.class, (start) -> onStart(ctx, restHighLevelClient, start.dataIndex, start.dataPayload, start.replyTo))
			.onMessage(SearchResponseCommand.class, src -> onSearchResponseCommand(
				ctx, restHighLevelClient, src, logger))
			.onMessage(BulkResponseCommand.class, brc -> onBulkResponseCommand(logger, brc))
			.build();
	}

	private static Behavior<Command> onBulkResponseCommand(
		Logger logger, BulkResponseCommand brc) {

		BulkResponse response = brc.bulkResponse;
		Exception throwable = brc.exception;
		ActorRef<Response> replyTo = brc.replyTo;

		if (response != null) {

			if (response.hasFailures()) {
				String errorMessage = response.buildFailureMessage();
				logger.error("Bulk request error: " + errorMessage);
				replyTo.tell(new Failure(new RuntimeException(errorMessage)));
			}

		}

		if (throwable != null) {
			logger.error("Error on bulk request", throwable);
			replyTo.tell(new Failure(throwable));
		}
		else {
			replyTo.tell(Success.INSTANCE);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onSearchResponseCommand(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		SearchResponseCommand src, Logger logger) {

		Exception exception = src.exception;
		DataIndex dataIndex = src.dataIndex;
		DataPayload dataPayload = src.dataPayload;
		SearchResponse searchResponse = src.searchResponse;
		ActorRef<Response> replyTo = src.replyTo;

		if (exception != null) {
			logger.error("Error on search", exception);
		}

		DocWriteRequest docWriteRequest =
			createDocWriteRequest(ctx, dataIndex, dataPayload, logger, searchResponse);

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
		bulkRequest.add(docWriteRequest);

		restHighLevelClient.bulkAsync(
			bulkRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(BulkResponse bulkResponse) {
					ctx.getSelf().tell(
						new BulkResponseCommand(
							replyTo, bulkResponse, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(
						new BulkResponseCommand(
							replyTo, null, e));
				}
			});

		return Behaviors.same();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		DataIndex dataIndex, DataPayload dataPayload,
		ActorRef<Response> replyTo) {

		ctx.getLog().info("index writer start for content: " + dataPayload.getContentId());

		SearchRequest searchRequest = new SearchRequest(dataIndex.getName());

		TermQueryBuilder termQueryBuilder =
			QueryBuilders.termQuery("contentId.keyword", dataPayload.getContentId());

		SearchSourceBuilder searchSourceBuilder =
			new SearchSourceBuilder();

		searchSourceBuilder.query(termQueryBuilder);

		searchRequest.source(searchSourceBuilder);

		restHighLevelClient.searchAsync(
			searchRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(SearchResponse searchResponse) {
					ctx.getSelf().tell(
						new SearchResponseCommand(
							dataIndex, dataPayload, replyTo, searchResponse, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(
						new SearchResponseCommand(
							dataIndex, dataPayload, replyTo, null, e));
				}
			});

		return Behaviors.same();
	}

	private static DocWriteRequest createDocWriteRequest(
		ActorContext<?> ctx, DataIndex dataIndex, DataPayload dataPayload, Logger logger,
		SearchResponse searchResponse) {

		String indexName = dataIndex.getName();

		IndexRequest indexRequest = new IndexRequest(indexName);

		if (searchResponse != null && searchResponse.getHits().getHits().length > 0) {

			logger.info("found document for contentId: " + dataPayload.getContentId());

			String documentId = searchResponse.getHits().getAt(0).getId();

			String[] documentTypes = dataPayload.getDocumentTypes();

			if (documentTypes == null || documentTypes.length == 0) {

				logger.info("delete document for contentId: " + dataPayload.getContentId());

				return new DeleteRequest(indexName, documentId);
			}

			indexRequest.id(documentId);
		}

		logger.info("index document for contentId: " + dataPayload.getContentId());

		JsonObject jsonObject = JsonObject.mapFrom(dataPayload);

		JsonObject acl =
			jsonObject.getJsonObject("acl");

		if (acl == null || acl.isEmpty()) {
			jsonObject.put("acl", Map.of("public", true));
		}

		ObjectMapper objectMapper =
			JacksonObjectMapperProvider.get(ctx.getSystem()).getOrCreate(
				"jackson-json",
				Optional.empty()
			);

		try {
			indexRequest.source(
				objectMapper.writeValueAsBytes(dataPayload),
				XContentType.JSON);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return indexRequest;
	}

}
