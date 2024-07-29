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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.openk9.ml.grpc.EmbeddingOuterClass;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmbeddingService {

	private static final String GET_EMBEDDING_CHUNKS_CONFIGURATION =
		"EmbeddingService#getEmbeddingChunksConfiguration";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@CacheName("bucket-resource")
	Cache cache;

	public static CompletionStage<byte[]> getEmbeddedPayload(
		String tenantId, String scheduleId, byte[] payload) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				GET_EMBEDDING_CHUNKS_CONFIGURATION,
				new EmbeddingChunksConfigurationRequest(tenantId, scheduleId)
			)
			.flatMap(message -> {
				var configurations = (EmbeddingChunksConfiguration) message.body();

				var client = EmbeddingStubRegistry.getStub(configurations.apiUrl());

				var apiKey = configurations.apiKey();
				var indexName = configurations.indexName();
				var jsonConfig = configurations.jsonConfig() != null
					? configurations.jsonConfig()
					: "{}";
				var chunkType = mapChunkType(configurations);
				var windowSize = configurations.chunkWindowSize();

				var documentContext = JsonPath
					.using(Configuration.defaultConfiguration())
					.parseUtf8(payload);

				String text = documentContext.read(configurations.fieldJsonPath);
				String title = documentContext.read(configurations.fieldTitle);
				String url = documentContext.read(configurations.fieldUrl);

				String contentId = documentContext.read("$.contentId");
				Map<String, Object> acl = documentContext.read("$.acl");

				var metadataMap = getMetadataMap(
					documentContext,
					configurations.metadataMapping()
				);

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
							embeddingResponse, indexName, contentId, title, url,
							acl, metadataMap, windowSize
						)
					);
			})
			.subscribeAsCompletionStage();
	}

	protected static Map<String, Object> getMetadataMap(
		DocumentContext documentContext, String metadataMapping) {

		var metadata = new HashMap<String, Object>() {};

		if (metadataMapping == null) {
			return metadata;
		}

		for (String expression : metadataMapping.split(";")) {

			expression = expression.trim();
			var splits = expression.split("\\.");

			var key = splits[splits.length - 1];

			var paths = Arrays.copyOfRange(splits, 1, splits.length - 1);

			var value = documentContext.read(expression);

			traverse(paths, metadata, key, value);

		}

		return metadata;
	}

	protected static byte[] mapToPayload(
		EmbeddingOuterClass.EmbeddingResponse embeddingResponse,
		String indexName, String contentId, String title, String url,
		Map<String, Object> acl, Map<String, Object> metadataMap,
		int windowSize) {

		var jsonArray = new JsonArray();

		var chunks = embeddingResponse.getChunksList();

		for (EmbeddingOuterClass.ResponseChunk responseChunk : chunks) {

			var number = responseChunk.getNumber();
			var total = responseChunk.getTotal();
			var chunkText = responseChunk.getText();
			var vector = responseChunk.getVectorsList();

			var jsonObject = mapToJsonObject(
				indexName, contentId, title, url,
				acl, metadataMap,
				number, total, chunkText, vector
			);

			var previous = getPrevious(windowSize, number, chunks)
				.stream().map(it -> mapToJsonObject(
						indexName, contentId, title, url,
						acl, metadataMap,
						it.getNumber(), it.getTotal(), it.getText(), it.getVectorsList()
					)
				);

			var next = getNext(windowSize, number, total, chunks)
				.stream().map(it -> mapToJsonObject(
						indexName, contentId, title, url,
						acl, metadataMap,
						it.getNumber(), it.getTotal(), it.getText(), it.getVectorsList()
					)
				);

			jsonObject.put("previous", previous);
			jsonObject.put("next", next);

			jsonArray.add(jsonObject);

		}

		return jsonArray.toBuffer().getBytes();
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
		String title,
		String url,
		Map<String, Object> acl,
		Map<String, Object> metadataMap,
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
		jsonObject.put("title", title);
		jsonObject.put("url", url);

		if (acl == null || acl.isEmpty()) {
			jsonObject.put("acl", Map.of("public", true));
		}
		else {
			jsonObject.put("acl", acl);
		}

		for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {

			jsonObject.put(entry.getKey(), entry.getValue());

		}
		return jsonObject;
	}

	private static void traverse(
		String[] paths, Map<String, Object> root, String key, Object value) {

		for (String path : paths) {

			var nextRoot = (Map<String, Object>) root.computeIfAbsent(
				path,
				k -> new HashMap<String, Object>()
			);

			var nextPaths = Arrays.copyOfRange(paths, 1, paths.length);

			traverse(nextPaths, nextRoot, key, value);

			return;

		}

		root.put(key, value);

	}

	public Uni<EmbeddedText> getEmbeddedText(String tenantId, String text) {

		return getEmbeddingConfiguration(tenantId)
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
				getEmbeddingConfiguration(request.tenantId)
					.flatMap(embeddingConfiguration -> s.createNamedQuery(
							VectorIndex.FETCH_BY_SCHEDULE_ID, VectorIndex.class)
						.setParameter(Scheduler_.SCHEDULE_ID, request.scheduleId)
						.getSingleResult()
						.map(vectorIndex -> new EmbeddingChunksConfiguration(
							embeddingConfiguration.apiUrl(),
							embeddingConfiguration.apiKey(),
							vectorIndex.getTextEmbeddingField(),
							vectorIndex.getTitleField(),
							vectorIndex.getUrlField(),
							vectorIndex.getMetadataMapping(),
							vectorIndex.getChunkType(),
							vectorIndex.getChunkWindowSize(),
							vectorIndex.getJsonConfig(),
							vectorIndex.getDataIndex().getName()
						))
					)
			)
		);
	}

	private Uni<EmbeddingConfiguration> getEmbeddingConfiguration(String tenantId) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(tenantId),
			sessionFactory.withTransaction(
				tenantId, (s, t) -> s
					.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
					.getSingleResult()
					.map(embeddingModel -> new EmbeddingConfiguration(
						embeddingModel.getApiUrl(),
						embeddingModel.getApiKey()
					)))
		);

	}

	private static EmbeddingOuterClass.ChunkType mapChunkType(EmbeddingChunksConfiguration configurations) {
		return switch (configurations.chunkType()) {
			case DEFAULT -> EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT;
			case TEXT_SPLITTER -> EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER;
			case TOKEN_TEXT_SPLITTER ->
				EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TOKEN_TEXT_SPLITTER;
			case CHARACTER_TEXT_SPLITTER ->
				EmbeddingOuterClass.ChunkType.CHUNK_TYPE_CHARACTER_TEXT_SPLITTER;
			case SEMANTIC_SPLITTER -> EmbeddingOuterClass.ChunkType.CHUNK_TYPE_SEMANTIC_SPLITTER;
		};
	}

	private record EmbeddingChunksConfigurationRequest(String tenantId, String scheduleId) {}

	public record EmbeddingChunksConfiguration(
		String apiUrl,
		String apiKey,
		String fieldJsonPath,
		String fieldTitle,
		String fieldUrl,
		String metadataMapping,
		VectorIndex.ChunkType chunkType,
		int chunkWindowSize,
		String jsonConfig,
		String indexName
	) {}

	public record EmbeddingConfiguration(
		String apiUrl,
		String apiKey
	) {}

	public record EmbeddedText(
		List<Float> vector
	) {}

}
