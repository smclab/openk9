package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.util.Map;

public class IndexWriterActor {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	private record SearchResponseCommand(SearchResponse searchResponse, Exception exception) implements Command {}
	private record BulkResponseCommand(BulkResponse bulkResponse, Exception exception) implements Command {}
	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Failure(Exception exception) implements Response {}

	public static Behavior<Command> create(DataIndex dataIndex, DataPayload dataPayload, ActorRef<Response> replyTo) {

		RestHighLevelClient restHighLevelClient =
			CDI.current().select(RestHighLevelClient.class).get();

		return Behaviors.setup(ctx -> initial(ctx, restHighLevelClient, dataIndex, dataPayload, replyTo));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		DataIndex dataIndex, DataPayload dataPayload,
		ActorRef<Response> replyTo) {

		Logger logger = ctx.getLog();

		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(ctx, restHighLevelClient, dataIndex, dataPayload))
			.onMessage(SearchResponseCommand.class, src -> onSearchResponseCommand(
				ctx, restHighLevelClient, dataIndex, dataPayload, logger, src))
			.onMessage(BulkResponseCommand.class, brc -> onBulkResponseCommand(replyTo, logger, brc))
			.build();
	}

	private static Behavior<Command> onBulkResponseCommand(
		ActorRef<Response> replyTo, Logger logger, BulkResponseCommand brc) {
		BulkResponse response = brc.bulkResponse;
		Exception throwable = brc.exception;

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

		return Behaviors.stopped();
	}

	private static Behavior<Command> onSearchResponseCommand(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		DataIndex dataIndex, DataPayload dataPayload, Logger logger,
		SearchResponseCommand src) {
		Exception exception = src.exception;

		if (exception != null) {
			logger.error("Error on search", exception);
		}

		DocWriteRequest docWriteRequest =
			createDocWriteRequest(dataIndex, dataPayload, logger, src.searchResponse);

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.add(docWriteRequest);

		restHighLevelClient.bulkAsync(
			bulkRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(BulkResponse bulkResponse) {
					ctx.getSelf().tell(new BulkResponseCommand(bulkResponse, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(new BulkResponseCommand(null, e));
				}
			});

		return Behaviors.same();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		DataIndex dataIndex, DataPayload dataPayload) {
		ctx.getLog().info("IndexWriter start for content: " + dataPayload.getContentId());

		SearchRequest searchRequest = new SearchRequest(dataIndex.getName());

		MatchQueryBuilder matchQueryBuilder =
			QueryBuilders.matchQuery("contentId", dataPayload.getContentId());

		SearchSourceBuilder searchSourceBuilder =
			new SearchSourceBuilder();

		searchSourceBuilder.query(matchQueryBuilder);

		searchRequest.source(searchSourceBuilder);

		restHighLevelClient.searchAsync(
			searchRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(SearchResponse searchResponse) {
					ctx.getSelf().tell(new SearchResponseCommand(searchResponse, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(new SearchResponseCommand(null, e));
				}
			});

		return Behaviors.same();
	}

	private static DocWriteRequest createDocWriteRequest(
		DataIndex dataIndex, DataPayload dataPayload, Logger logger,
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

		indexRequest.source(jsonObject.toString(), XContentType.JSON);

		return indexRequest;
	}

}
