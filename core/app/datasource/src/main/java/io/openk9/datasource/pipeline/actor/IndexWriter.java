/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.opensearch.action.ActionListener;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import java.util.Map;
import javax.enterprise.inject.spi.CDI;

public class IndexWriter {

	public sealed interface Command extends CborSerializable {}

	public record Start(SchedulerDTO scheduler, byte[] dataPayload, ActorRef<Response> replyTo)
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
