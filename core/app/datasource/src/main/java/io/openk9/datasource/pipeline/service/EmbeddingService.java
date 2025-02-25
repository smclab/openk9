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

package io.openk9.datasource.pipeline.service;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmbeddingService {

	private static final String GET_EMBEDDING_CHUNKS_CONFIGURATION =
		"EmbeddingService#getEmbeddingChunksConfiguration";

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	EmbeddingModelService embeddingModelService;

	@CacheName("bucket-resource")
	Cache cache;

	private static final Logger log = Logger.getLogger(EmbeddingService.class);

	public static CompletionStage<byte[]> getEmbeddedPayload(
		String tenantId, String scheduleId, byte[] payload) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				GET_EMBEDDING_CHUNKS_CONFIGURATION,
				new EmbeddingChunksConfigurationRequest(tenantId, scheduleId)
			)
			.onItem().ifNull().failWith(PayloadEmbeddingFailed::new)
			.flatMap(message -> {
				var configurations = (EmbeddingChunksConfiguration) message.body();

				var client = EmbeddingStubRegistry.getStub(configurations.apiUrl());

				var apiKey = configurations.apiKey();
				var indexName = configurations.indexName();
				var jsonConfig = configurations.jsonConfig() != null
					? configurations.jsonConfig()
					: "{}";
				var chunkType = configurations.chunkType();
				var windowSize = configurations.chunkWindowSize();

				var documentContext = JsonPath
					.using(Configuration.defaultConfiguration())
					.parseUtf8(payload);

				var docTypeField = Objects.requireNonNull(configurations.docTypeField());
				var path = docTypeField.getPath();

				String text = documentContext.read(path);

				String contentId = documentContext.read("$.contentId");
				Map<String, Object> acl = documentContext.read("$.acl");

				return client.getMessages(EmbeddingOuterClass.EmbeddingRequest
						.newBuilder()
						.setApiKey(apiKey)
						.setChunk(EmbeddingOuterClass.RequestChunk.newBuilder()
							.setType(chunkType)
							.setJsonConfig(StructUtils.fromJson(jsonConfig))
							.build())
						.setText(text)
						.build())
					.map(embeddingResponse -> EmbeddingService.mapToPayload(
						embeddingResponse, indexName, contentId, acl, windowSize)
					);
			})
			.subscribeAsCompletionStage();
	}

	protected static <T> List<T> getPrevious(
		int windowSize, int number, List<T> chunks) {

		var fromIndex = Math.max(number - 1 - windowSize, 0);
		var toIndex = Math.min(number - 1 + windowSize, number - 1);

		return chunks.subList(fromIndex, toIndex);
	}

	protected static <T> List<T> getNext(
		int windowSize, int number, int total, List<T> chunks) {

		var fromIndex = Math.max(number, 0);
		var toIndex = Math.min(fromIndex + windowSize, total);

		return chunks.subList(fromIndex, toIndex);
	}

	private static JsonObject mapToJsonObject(
		String indexName,
		String contentId,
		Map<String, Object> acl,
		int number,
		int total,
		String chunkText,
		List<Float> vector) {

		var jsonObject = new JsonObject();

		jsonObject.put("number", number);
		jsonObject.put("total", total);
		jsonObject.put("chunkText", chunkText);
		jsonObject.put("vector", vector);
		jsonObject.put("indexName", indexName);
		jsonObject.put("contentId", contentId);

		if (acl == null || acl.isEmpty()) {
			jsonObject.put("acl", Map.of("public", true));
		}
		else {
			jsonObject.put("acl", acl);
		}

		return jsonObject;
	}

	protected static byte[] mapToPayload(
		EmbeddingOuterClass.EmbeddingResponse embeddingResponse,
		String indexName, String contentId,
		Map<String, Object> acl,
		int windowSize) {

		var jsonArray = new JsonArray();

		var chunks = embeddingResponse.getChunksList();

		for (EmbeddingOuterClass.ResponseChunk responseChunk : chunks) {

			var number = responseChunk.getNumber();
			var total = responseChunk.getTotal();
			var chunkText = responseChunk.getText();
			var vector = responseChunk.getVectorsList();

			var jsonObject = mapToJsonObject(
				indexName, contentId,
				acl,
				number, total, chunkText, vector
			);

			var previous = getPrevious(windowSize, number, chunks)
				.stream()
				.map(it -> mapToJsonObject(
					indexName,
					contentId,
					acl,
					it.getNumber(),
					it.getTotal(),
					it.getText(),
					it.getVectorsList()
					)
				);

			var next = getNext(windowSize, number, total, chunks)
				.stream()
				.map(it -> mapToJsonObject(
					indexName,
					contentId,
					acl,
					it.getNumber(),
					it.getTotal(),
					it.getText(),
					it.getVectorsList()
					)
				);

			jsonObject.put("previous", previous);
			jsonObject.put("next", next);

			jsonArray.add(jsonObject);

		}

		return jsonArray.toBuffer().getBytes();
	}

	public Uni<EmbeddedText> getEmbeddedText(String tenantId, String text) {

		return getEmbeddingConfiguration(tenantId)
			.onItem().ifNull().failWith(ConfigurationNotFound::new)
			.flatMap(configuration -> {
				var client = EmbeddingStubRegistry.getStub(configuration.apiUrl());
				return client.getMessages(EmbeddingOuterClass.EmbeddingRequest.newBuilder()
					.setText(text)
					.setApiKey(configuration.apiKey())
					.setChunk(EmbeddingOuterClass.RequestChunk.newBuilder()
						.setType(EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT)
						.build())
					.build());
			})
			.map(embeddingResponse -> new EmbeddedText(
				embeddingResponse.getChunks(0).getVectorsList()));

	}

	@ConsumeEvent(GET_EMBEDDING_CHUNKS_CONFIGURATION)
	Uni<EmbeddingChunksConfiguration> getEmbeddingChunksConfigurations(
		EmbeddingChunksConfigurationRequest request) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(request),
			sessionFactory.withTransaction(request.tenantId(), (s, t) ->
					getEmbeddingConfiguration(s, request.tenantId)
					.onItem().ifNull().failWith(ConfigurationNotFound::new)
					.flatMap(embeddingConfiguration -> s.createNamedQuery(
							Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
						.setPlan(s.getEntityGraph(
							Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
						.setParameter(Scheduler_.SCHEDULE_ID, request.scheduleId())
						.getSingleResultOrNull()
						.map(scheduler -> {

							var dataIndex = scheduler.getDataIndex();

							return new EmbeddingChunksConfiguration(
								embeddingConfiguration.apiUrl(),
								embeddingConfiguration.apiKey(),
								dataIndex.getEmbeddingDocTypeField(),
								dataIndex.getChunkType(),
								dataIndex.getChunkWindowSize(),
								dataIndex.getEmbeddingJsonConfig(),
								dataIndex.getIndexName()
							);
						})
					))
				.onFailure(EmbeddingServiceException.class)
				.invoke(throwable -> log.warnf(
						throwable,
						"Embedding service is not configured for tenantId: %s",
						request.tenantId()
					)
				)
				.onFailure()
				.invoke(throwable -> log.error(
					"Something went wrong when trying to get Embedding chunks configuration",
					throwable
				))
				.onFailure()
				.recoverWithNull()
		);
	}

	private record EmbeddingChunksConfigurationRequest(String tenantId, String scheduleId) {}

	public record EmbeddingChunksConfiguration(
		String apiUrl,
		String apiKey,
		DocTypeField docTypeField,
		EmbeddingOuterClass.ChunkType chunkType,
		int chunkWindowSize,
		String jsonConfig,
		String indexName
	) {}

	private Uni<EmbeddingConfiguration> getEmbeddingConfiguration(String tenantId) {
		return sessionFactory.withTransaction(
			tenantId, (s, t) ->
				getEmbeddingConfiguration(s, tenantId)
		);
	}

	public record EmbeddedText(
		List<Float> vector
	) {}

	private Uni<EmbeddingConfiguration> getEmbeddingConfiguration(
		Mutiny.Session session,
		String tenantId) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(tenantId),
			embeddingModelService.fetchCurrent(session)
				.map(EmbeddingConfiguration::map)
				.onFailure()
				.invoke(throwable ->
					log.warnf(
						throwable,
						"Cannot fetch current embedding model for tenantId %s",
						tenantId
					)
				)
				.onFailure()
				.recoverWithNull()
		);

	}

	public record EmbeddingConfiguration(
		String apiUrl,
		String apiKey
	) {

		public static EmbeddingConfiguration map(EmbeddingModel model) {
			return new EmbeddingConfiguration(model.getApiUrl(), model.getApiKey());
		}

	}

}
