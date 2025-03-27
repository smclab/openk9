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

import java.util.Map;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;
import io.openk9.datasource.processor.payload.DataPayload;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
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
import org.opensearch.core.action.ActionListener;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

public class IndexWriter {

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx -> {

			RestHighLevelClient restHighLevelClient =
				CDI.current().select(RestHighLevelClient.class).get();

			var oldDataIndexName = scheduler.getOldDataIndexName();
			var newDataIndexName = scheduler.getNewDataIndexName();

			return initial(
				ctx,
				restHighLevelClient,
				oldDataIndexName,
				newDataIndexName,
				replyTo
			);

		});
	}

	private static DocWriteRequest createDocWriteRequest(
		String indexName, byte[] dataPayload, Logger logger,
		SearchResponse searchResponse, HeldMessage heldMessage) {

		IndexRequest indexRequest = new IndexRequest(indexName);

		if (searchResponse != null && searchResponse.getHits().getHits().length > 0) {

			logger.info("found document for contentId: " + heldMessage.contentId());

			String documentId = searchResponse.getHits().getAt(0).getId();

			indexRequest.id(documentId);

			if (dataPayload == null) {

				logger.info("delete document for contentId: " + heldMessage.contentId());

				return new DeleteRequest(indexName, documentId);
			}
		}

		logger.info("index document for contentId: " + heldMessage.contentId());

		var jsonObject = (JsonObject) Json.decodeValue(Buffer.buffer(dataPayload));

		JsonObject acl =
			jsonObject.getJsonObject("acl");

		if (acl == null || acl.isEmpty()) {
			jsonObject.put("acl", Map.of("public", true));
		}

		return indexRequest.source(jsonObject.toString(), XContentType.JSON);
	}

	private static Behavior<Writer.Command> initial(
		ActorContext<Writer.Command> ctx, RestHighLevelClient restHighLevelClient,
		String oldDataIndexName,
		String newDataIndexName,
		ActorRef<Writer.Response> replyTo) {

		Logger logger = ctx.getLog();

		return Behaviors.receive(Writer.Command.class)
			.onMessage(Writer.Start.class, (start) -> onStart(
					ctx,
					restHighLevelClient,
					oldDataIndexName,
					start
				)
			)
			.onMessage(SearchResponseCommand.class, src -> onSearchResponseCommand(
				ctx, restHighLevelClient, src, logger, oldDataIndexName, newDataIndexName)
			)
			.onMessage(BulkResponseCommand.class, brc -> onBulkResponseCommand(
				logger, brc, replyTo)
			)
			.build();
	}

	private static Behavior<Writer.Command> onBulkResponseCommand(
		Logger logger, BulkResponseCommand brc, ActorRef<Writer.Response> replyTo) {

		BulkResponse bulkResponse = brc.bulkResponse;
		Exception throwable = brc.exception;
		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(brc.dataPayload()), DataPayload.class);

		var heldMessage = brc.heldMessage;

		if (throwable != null) {
			logger.warn("Error on bulk request", throwable);
			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

			return Behaviors.same();
		}

		if (bulkResponse != null) {

			if (bulkResponse.hasFailures()) {
				String errorMessage = bulkResponse.buildFailureMessage();

				DatasourceEventBus.sendMessage(DatasourceMessage.Failure
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

				logger.warn("Bulk request error: {}", errorMessage);
				replyTo.tell(new Writer.Failure(
					new WriterException(errorMessage),
					heldMessage
				));
			}
			else {

				for (BulkItemResponse itemResponse : bulkResponse) {

					DocWriteResponse response = itemResponse.getResponse();
					String index = response.getIndex();
					DocWriteResponse.Result result = response.getResult();

					DatasourceMessage.DatasourceMessageBuilder<?, ?> messageBuilder =
						switch (result) {
							case CREATED -> DatasourceMessage.New.builder();
							case UPDATED -> DatasourceMessage.Update.builder();
							case DELETED -> DatasourceMessage.Delete.builder();
							default -> DatasourceMessage.Unknown.builder();
						};

					DatasourceEventBus.sendMessage(messageBuilder
						.ingestionId(dataPayload.getIngestionId())
						.datasourceId(dataPayload.getDatasourceId())
						.contentId(dataPayload.getContentId())
						.parsingDate(dataPayload.getParsingDate())
						.tenantId(dataPayload.getTenantId())
						.indexName(index)
						.build()
					);

				}

				replyTo.tell(new Writer.Success(heldMessage));
			}

		}

		return Behaviors.same();
	}

	private static Behavior<Writer.Command> onSearchResponseCommand(
		ActorContext<Writer.Command> ctx,
		RestHighLevelClient restHighLevelClient,
		SearchResponseCommand src,
		Logger logger,
		String oldDataIndexName,
		String newDataIndexName) {

		Exception exception = src.exception;
		byte[] dataPayload = src.dataPayload;
		SearchResponse searchResponse = src.searchResponse;
		var heldMessage = src.heldMessage();

		if (exception != null) {
			logger.error("Error on search", exception);
		}

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

		if (oldDataIndexName != null) {
			bulkRequest.add(createDocWriteRequest(
				oldDataIndexName,
				dataPayload,
				logger,
				searchResponse,
				heldMessage
			));
		}
		if (newDataIndexName != null) {
			bulkRequest.add(createDocWriteRequest(
				newDataIndexName,
				dataPayload,
				logger,
				searchResponse,
				heldMessage
			));
		}

		restHighLevelClient.bulkAsync(
			bulkRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(BulkResponse bulkResponse) {
					ctx.getSelf().tell(
						new BulkResponseCommand(dataPayload, heldMessage, bulkResponse, null));
				}

				@Override
				public void onFailure(Exception e) {
					ctx.getSelf().tell(
						new BulkResponseCommand(dataPayload, heldMessage, null, e));
				}
			});

		return Behaviors.same();
	}

	private static Behavior<Writer.Command> onStart(
		ActorContext<Writer.Command> ctx,
		RestHighLevelClient restHighLevelClient,
		String oldDataIndexName,
		Writer.Start start) {

		var dataPayload = start.dataPayload();
		var heldMessage = start.heldMessage();


		ctx.getLog().info("index writer start for content: " + heldMessage.contentId());

		if (oldDataIndexName != null) {
			SearchRequest searchRequest = new SearchRequest(oldDataIndexName);

			TermQueryBuilder termQueryBuilder =
				QueryBuilders.termQuery("contentId.keyword", heldMessage.contentId());

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
								dataPayload, heldMessage, searchResponse, null));
					}

					@Override
					public void onFailure(Exception e) {
						ctx.getSelf().tell(
							new SearchResponseCommand(
								dataPayload, heldMessage, null, e));
					}
				});
		}
		else {
			ctx.getSelf().tell(
				new SearchResponseCommand(
					dataPayload, heldMessage, null, null));
		}

		return Behaviors.same();
	}

	private record BulkResponseCommand(
		byte[] dataPayload,
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Exception exception
	) implements Writer.Command {}

	private record SearchResponseCommand(
		byte[] dataPayload,
		HeldMessage heldMessage,
		SearchResponse searchResponse,
		Exception exception
	) implements Writer.Command {}

}
