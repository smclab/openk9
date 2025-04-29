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
import com.jayway.jsonpath.DocumentContext;
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

	private static final EmbeddingOuterClass.RequestChunk REQUEST_CHUNK_DEFAULT =
		EmbeddingOuterClass.RequestChunk.newBuilder()
			.setType(EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT)
			.build();

	private static final String GET_EMBEDDING_CHUNKS_CONFIGURATION =
		"EmbeddingService#getEmbeddingChunksConfiguration";

	private static final Logger log = Logger.getLogger(EmbeddingService.class);

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	EmbeddingModelService embeddingModelService;

	@CacheName("bucket-resource")
	Cache cache;

	public static CompletionStage<byte[]> getEmbeddedPayload(
		String tenantId, String scheduleId, byte[] payload) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				GET_EMBEDDING_CHUNKS_CONFIGURATION,
				new GetEmbeddingChunkRequest(tenantId, scheduleId)
			)
			.onItem().ifNull().failWith(PayloadEmbeddingFailed::new)
			.flatMap(message -> {
				var embeddingChunksRequest = (EmbeddingChunksRequest) message.body();

				// embedding model service url
				var apiUrl = embeddingChunksRequest.embeddingModelUrl();

				var chunkWindowSize = embeddingChunksRequest.chunkWindowSize();

				var documentContext = JsonPath
					.using(Configuration.defaultConfiguration())
					.parseUtf8(payload);

				var docTypeField = Objects.requireNonNull(
					embeddingChunksRequest.docTypeField(),
					"The source field for text embedding is not specified."
				);

				var docTypeFieldJsonPath = "$." + docTypeField.getPath();

				String text = documentContext.read(docTypeFieldJsonPath);

				// remove the original docTypeField element, it is eventually split in chunks
				documentContext.delete(docTypeFieldJsonPath);

				var root = getRoot(documentContext);

				// fails if there is no text to send for text-embedding
				if (text == null || text.isEmpty()) {

					return Uni.createFrom().failure(
						new PayloadEmbeddingFailed(String.format(
							"The field %s has no text",
							docTypeFieldJsonPath
						))
					);
				}

				var client = EmbeddingStubRegistry.getStub(apiUrl);

				return client.getMessages(EmbeddingOuterClass.EmbeddingRequest
						.newBuilder()
						.setEmbeddingModel(embeddingChunksRequest.embeddingModel())
						.setChunk(embeddingChunksRequest.requestChunk())
						.setText(text)
						.build())
					.map(embeddingResponse -> EmbeddingService
						.mapToPayload(embeddingResponse, root, chunkWindowSize)
					);
			})
			.subscribeAsCompletionStage();
	}

	protected static <T> List<T> getNextWindow(
		int windowSize, int number, int total, List<T> chunks) {

		var fromIndex = Math.max(number, 0);
		var toIndex = Math.min(fromIndex + windowSize, total);

		return chunks.subList(fromIndex, toIndex);
	}

	protected static <T> List<T> getPreviousWindow(
		int windowSize, int number, List<T> chunks) {

		var fromIndex = Math.max(number - 1 - windowSize, 0);
		var toIndex = Math.min(number - 1 + windowSize, number - 1);

		return chunks.subList(fromIndex, toIndex);
	}

	protected static Map<String, Object> getRoot(DocumentContext documentContext) {

		return documentContext.read("$");
	}

	protected static byte[] mapToPayload(
		EmbeddingOuterClass.EmbeddingResponse embeddingResponse,
		Map<String, Object> root,
		int windowSize) {

		var jsonArray = new JsonArray();

		var chunks = embeddingResponse.getChunksList();

		if (chunks.isEmpty()) {
			throw new PayloadEmbeddingFailed(
				"No chunks created from this payload");
		}

		for (EmbeddingOuterClass.ResponseChunk responseChunk : chunks) {

			// get chunk fields.
			var number = responseChunk.getNumber();
			var total = responseChunk.getTotal();
			var chunkText = responseChunk.getText();
			var vector = responseChunk.getVectorsList();

			// create the document from the response chunk, merge with original source.
			var jsonObject = mapToDocumentObject(
				root, number, total, chunkText, vector);

			// create the array of the previous near chunks.
			var previous = new JsonArray();
			for (EmbeddingOuterClass.ResponseChunk chunk :
				getPreviousWindow(windowSize, number, chunks)) {

				var chunkWindowObject = mapToChunkWindowObject(chunk.getNumber(), chunk.getText());
				previous.add(chunkWindowObject);

			}

			// create the array of the next near chunks.
			var next = new JsonArray();
			for (EmbeddingOuterClass.ResponseChunk chunk :
				getNextWindow(windowSize, number, total, chunks)) {

				var chunkWindowObject = mapToChunkWindowObject(chunk.getNumber(), chunk.getText());
				next.add(chunkWindowObject);

			}

			// add previous and next chunks to the new document.
			jsonObject.put("previous", previous);
			jsonObject.put("next", next);

			jsonArray.add(jsonObject);

		}

		return jsonArray.toBuffer().getBytes();
	}

	private static JsonObject mapToChunkWindowObject(int number, String text) {

		var jsonObject = new JsonObject();
		jsonObject.put("number", number);
		jsonObject.put("chunkText", text);

		return jsonObject;
	}

	private static JsonObject mapToDocumentObject(
		Map<String, Object> root, int number, int total, String chunkText, List<Float> vector) {

		var jsonObject = new JsonObject();

		jsonObject.put("number", number);
		jsonObject.put("total", total);
		jsonObject.put("chunkText", chunkText);
		jsonObject.put("vector", vector);

		for (Map.Entry<String, Object> entry : root.entrySet()) {

			jsonObject.put(entry.getKey(), entry.getValue());

		}

		return jsonObject;
	}

	public Uni<EmbeddedText> getEmbeddedText(String tenantId, String text) {

		return getEmbeddingModel(tenantId)
			.onItem().ifNull().failWith(ConfigurationNotFound::new)
			.flatMap(embeddingModel -> {

				EmbeddingOuterClass.EmbeddingModel embeddingModelRequest =
					mapToEmbeddingModelRequest(embeddingModel);
				var apiUrl = embeddingModel.getApiUrl();

				var client = EmbeddingStubRegistry.getStub(apiUrl);
				return client.getMessages(EmbeddingOuterClass.EmbeddingRequest.newBuilder()
					.setText(text)
					.setEmbeddingModel(embeddingModelRequest)
					.setChunk(REQUEST_CHUNK_DEFAULT)
					.build());
			})
			.map(embeddingResponse -> new EmbeddedText(
				// with default chunk strategy, we always have one embedding.
				embeddingResponse.getChunks(0).getVectorsList()));

	}

	@ConsumeEvent(GET_EMBEDDING_CHUNKS_CONFIGURATION)
	Uni<EmbeddingChunksRequest> getEmbeddingChunksConfigurations(
		GetEmbeddingChunkRequest request) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(request),
			sessionFactory.withTransaction(request.tenantId(), (s, t) ->
					getEmbeddingModel(s, request.tenantId())
					.onItem().ifNull().failWith(ConfigurationNotFound::new)
						.flatMap(embeddingModel -> s.createNamedQuery(
							Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
						.setPlan(s.getEntityGraph(
							Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
						.setParameter(Scheduler_.SCHEDULE_ID, request.scheduleId())
						.getSingleResultOrNull()
						.map(scheduler -> {


							var embeddingModelUrl = embeddingModel.getApiUrl();

							EmbeddingOuterClass.EmbeddingModel embeddingModelRequest =
								mapToEmbeddingModelRequest(embeddingModel);

							var dataIndex = scheduler.getDataIndex();

							// chunk strategy and configurations
							var chunkJsonConfig = dataIndex.getEmbeddingJsonConfig();
							var chunkType = dataIndex.getChunkType();
							var chunkWindowSize = dataIndex.getChunkWindowSize();

							var requestChunk = EmbeddingOuterClass.RequestChunk.newBuilder()
								.setType(chunkType)
								.setJsonConfig(StructUtils.fromJson(chunkJsonConfig))
								.build();

							return new EmbeddingChunksRequest(
								embeddingModelUrl,
								dataIndex.getEmbeddingDocTypeField(),
								chunkWindowSize,
								embeddingModelRequest,
								requestChunk
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

	private static EmbeddingOuterClass.EmbeddingModel mapToEmbeddingModelRequest(EmbeddingModel embeddingModel) {
		var embeddingModelBuilder = EmbeddingOuterClass.EmbeddingModel.newBuilder();

		if (embeddingModel.getApiKey() != null) {
			embeddingModelBuilder.setApiKey(embeddingModel.getApiKey());
		}

		var providerModel = embeddingModel.getProviderModel();
		if (providerModel != null) {
			var providerModelBuilder = EmbeddingOuterClass.ProviderModel.newBuilder();

			if (providerModel.getModel() != null) {
				providerModelBuilder.setModel(providerModel.getModel());
			}
			if (providerModel.getProvider() != null) {
				providerModelBuilder.setProvider(providerModel.getProvider());
			}

			embeddingModelBuilder.setProviderModel(providerModelBuilder.build());
		}

		if (embeddingModel.getJsonConfig() != null) {
			embeddingModelBuilder.setJsonConfig(
				StructUtils.fromJson(embeddingModel.getJsonConfig()));
		}

		return embeddingModelBuilder.build();
	}

	private Uni<EmbeddingModel> getEmbeddingModel(String tenantId) {
		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(tenantId),
			sessionFactory.withTransaction(
				tenantId,
				(s, t) -> getEmbeddingModel(s, tenantId)
			)
		);
	}

	private Uni<EmbeddingModel> getEmbeddingModel(
		Mutiny.Session session,
		String tenantId) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(tenantId),
			embeddingModelService.fetchCurrent(session)
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

	public record EmbeddedText(
		List<Float> vector
	) {}

	private record EmbeddingChunksRequest(
		String embeddingModelUrl,
		DocTypeField docTypeField,
		int chunkWindowSize,
		EmbeddingOuterClass.EmbeddingModel embeddingModel,
		EmbeddingOuterClass.RequestChunk requestChunk
	) {}

	private record GetEmbeddingChunkRequest(String tenantId, String scheduleId) {}


}
