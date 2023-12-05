package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
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
import java.util.Map;

public class IndexWriterActor {

	public sealed interface Command extends CborSerializable {}
	public record Start(io.openk9.datasource.pipeline.actor.dto.SchedulerDTO scheduler, byte[] dataPayload, ActorRef<Response> replyTo)
		implements Command {}
	private record SearchResponseCommand(SchedulerDTO scheduler, DataPayload dataPayload, ActorRef<Response> replyTo, SearchResponse searchResponse, Exception exception) implements Command {}
	private record BulkResponseCommand(ActorRef<Response> replyTo, BulkResponse bulkResponse, DataPayload dataPayload, Exception exception) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public enum Success implements Response {INSTANCE}
	public record Failure(Exception exception) implements Response {}

	public static Behavior<Command> create() {

		return Behaviors.setup(ctx -> {

			RestHighLevelClient restHighLevelClient =
				CDI.current().select(RestHighLevelClient.class).get();

			DatasourceEventBus eventBus =
				CDI.current().select(DatasourceEventBus.class).get();

			return initial(ctx, restHighLevelClient, eventBus);

		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		DatasourceEventBus eventBus) {

		Logger logger = ctx.getLog();

		return Behaviors.receive(Command.class)
			.onMessage(Start.class, (start) -> onStart(
				ctx, restHighLevelClient, start.scheduler, start.dataPayload, start.replyTo)
			)
			.onMessage(SearchResponseCommand.class, src -> onSearchResponseCommand(
				ctx, restHighLevelClient, src, logger)
			)
			.onMessage(BulkResponseCommand.class, brc -> onBulkResponseCommand(
				logger, brc, eventBus)
			)
			.build();
	}

	private static Behavior<Command> onBulkResponseCommand(
		Logger logger, BulkResponseCommand brc, DatasourceEventBus eventBus) {

		BulkResponse response = brc.bulkResponse;
		Exception throwable = brc.exception;
		ActorRef<Response> replyTo = brc.replyTo;
		DataPayload dataPayload = brc.dataPayload();

		if (response != null) {

			if (response.hasFailures()) {
				String errorMessage = response.buildFailureMessage();

				eventBus.sendEvent(
					DatasourceMessage.Failure
						.builder()
						.ingestionId(dataPayload.getIngestionId())
						.datasourceId(dataPayload.getDatasourceId())
						.contentId(dataPayload.getContentId())
						.parsingDate(dataPayload.getParsingDate())
						.tenantId(dataPayload.getTenantId())
						.indexName(dataPayload.getIndexName())
						.error(errorMessage)
						.build()
				);

				logger.error("Bulk request error: " + errorMessage);
				replyTo.tell(new Failure(new RuntimeException(errorMessage)));
			}

		}

		if (throwable != null) {
			logger.error("Error on bulk request", throwable);
			replyTo.tell(new Failure(throwable));
		}
		else {

			for (BulkItemResponse item : response) {

				DocWriteResponse curResponse = item.getResponse();
				String index = curResponse.getIndex();
				DocWriteResponse.Result result = curResponse.getResult();

				DatasourceMessage.DatasourceMessageBuilder<?, ?> builder =
					switch (result) {
						case CREATED -> DatasourceMessage.New.builder();
						case UPDATED -> DatasourceMessage.Update.builder();
						case DELETED -> DatasourceMessage.Delete.builder();
						default -> DatasourceMessage.Unknown.builder();
					};

				eventBus.sendEvent(
					builder
						.ingestionId(dataPayload.getIngestionId())
						.datasourceId(dataPayload.getDatasourceId())
						.contentId(dataPayload.getContentId())
						.parsingDate(dataPayload.getParsingDate())
						.tenantId(dataPayload.getTenantId())
						.indexName(index)
						.build()
				);

			}

			replyTo.tell(Success.INSTANCE);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onSearchResponseCommand(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		SearchResponseCommand src, Logger logger) {

		Exception exception = src.exception;
		SchedulerDTO scheduler = src.scheduler;
		DataPayload dataPayload = src.dataPayload;
		SearchResponse searchResponse = src.searchResponse;
		ActorRef<Response> replyTo = src.replyTo;
		String oldDataIndexName = scheduler.getOldDataIndexName();
		String newDataIndexName = scheduler.getNewDataIndexName();

		if (exception != null) {
			logger.error("Error on search", exception);
		}

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

		if (oldDataIndexName != null) {
			bulkRequest.add(createDocWriteRequest(ctx, oldDataIndexName, dataPayload, logger, searchResponse));
		}
		if (newDataIndexName != null) {
			bulkRequest.add(createDocWriteRequest(ctx, newDataIndexName, dataPayload, logger, searchResponse));
		}

		restHighLevelClient.bulkAsync(
			bulkRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(BulkResponse bulkResponse) {
					ctx.getSelf().tell(
						new BulkResponseCommand(
							replyTo, bulkResponse, dataPayload, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(
						new BulkResponseCommand(
							replyTo, null, dataPayload, e));
				}
			});

		return Behaviors.same();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient,
		SchedulerDTO scheduler, byte[] data,
		ActorRef<Response> replyTo) {

		DataPayload dataPayload = Json.decodeValue(Buffer.buffer(data), DataPayload.class);

		ctx.getLog().info("index writer start for content: " + dataPayload.getContentId());

		String oldDataIndexName = scheduler.getOldDataIndexName();


		if (oldDataIndexName != null) {
			SearchRequest searchRequest = new SearchRequest(oldDataIndexName);

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
								scheduler, dataPayload, replyTo, searchResponse, null));
					}

					@Override
					public void onFailure(Exception e) {
						ctx.getSelf().tell(
							new SearchResponseCommand(
								scheduler, dataPayload, replyTo, null, e));
					}
				});
		}
		else {
			ctx.getSelf().tell(
				new SearchResponseCommand(
					scheduler, dataPayload, replyTo, null, null));
		}
		return Behaviors.same();
	}

	private static DocWriteRequest createDocWriteRequest(
		ActorContext<?> ctx, String indexName, DataPayload dataPayload, Logger logger,
		SearchResponse searchResponse) {

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

		return indexRequest.source(jsonObject.toString(), XContentType.JSON);
	}

}
