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

import com.jayway.jsonpath.JsonPath;
import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.openk9.datasource.util.VertxJsonNodeJsonProvider;
import io.openk9.ml.grpc.EmbeddingOuterClass;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmbeddingService {

	private static final String GET_CONFIGURATIONS =
		"EmbeddingService#getConfigurations";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@CacheName("bucket-resource")
	Cache cache;

	public static CompletionStage<byte[]> getEmbeddedPayload(
		String tenantId, String scheduleId, byte[] payload) {

		return EventBusInstanceHolder.getEventBus()
			.request(GET_CONFIGURATIONS, new ConfigurationsRequest(tenantId, scheduleId))
			.flatMap(message -> {
				var configurations = (EmbeddingConfiguration) message.body();
				var client = EmbeddingStubRegistry.getStub(configurations.apiUrl());

				var apiKey = configurations.apiKey();
				var jsonConfig = StructUtils.fromJson(configurations.jsonConfig());
				var chunkType = mapChunkType(configurations);
				var documentContext =
					JsonPath.using(VertxJsonNodeJsonProvider.CONFIGURATION).parseUtf8(payload);

				String text = documentContext.read(configurations.fieldJsonPath);
				String indexName = documentContext.read("$.indexName");
				String contentId = documentContext.read("$.contentId");
				Map<String, List<String>> acl = documentContext.read("$.acl");

				return client.getMessages(EmbeddingOuterClass.EmbeddingRequest.newBuilder()
						.setApiKey(apiKey)
						.setChunk(EmbeddingOuterClass.RequestChunk.newBuilder()
							.setType(chunkType)
							.setJsonConfig(jsonConfig)
							.build())
						.setText(text)
						.build())
					.map(embeddingResponse -> {
						List<EmbeddedChunk> list = new ArrayList<>();

						for (EmbeddingOuterClass.ResponseChunk responseChunk : embeddingResponse.getChunksList()) {

							var number = responseChunk.getNumber();
							var total = responseChunk.getTotal();
							var chunkText = responseChunk.getText();
							var vector = responseChunk.getVectorsList();

							var embeddedChunk = new EmbeddedChunk(
								indexName, contentId, acl, number, total, chunkText, vector);

							list.add(embeddedChunk);

						}

						return Json.encodeToBuffer(new EmbeddedChunks(list)).getBytes();
					});
			})
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(GET_CONFIGURATIONS)
	Uni<EmbeddingConfiguration> getConfigurations(ConfigurationsRequest request) {

		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(request),
			sessionFactory.withTransaction(request.tenantId(), (s, t) ->
				s.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
					.getSingleResult()
					.flatMap(embeddingModel -> s.createNamedQuery(
							VectorIndex.FETCH_BY_SCHEDULE_ID, VectorIndex.class)
						.setParameter(Scheduler_.SCHEDULE_ID, request.scheduleId)
						.getSingleResult()
						.map(vectorIndex -> new EmbeddingConfiguration(
							embeddingModel.getApiUrl(),
							embeddingModel.getApiKey(),
							vectorIndex.getFieldJsonPath(),
							vectorIndex.getChunkType(),
							vectorIndex.getJsonConfig()
						))
					)
			)
		);
	}

	private static EmbeddingOuterClass.ChunkType mapChunkType(EmbeddingConfiguration configurations) {
		return switch (configurations.chunkType()) {
			case DEFAULT -> EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT;
			case TEXT_SPLITTER -> EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER;
			case TOKEN_TEXT_SPLITTER ->
				EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TOKEN_TEXT_SPLITTER;
			case CHARACTER_TEXT_SPLITTER ->
				EmbeddingOuterClass.ChunkType.CHUNK_TYPE_CHARACTER_TEXT_SPLITTER;
		};
	}

	private record ConfigurationsRequest(String tenantId, String scheduleId) {}

	public record EmbeddingConfiguration(
		String apiUrl,
		String apiKey,
		String fieldJsonPath,
		VectorIndex.ChunkType chunkType,
		String jsonConfig
	) {}

	public record EmbeddedChunk(
		String indexName,
		String contentId,
		Map<String, List<String>> acl,
		int number,
		int total,
		String chunkText,
		List<Float> vector
	) {}

	public record EmbeddedChunks(List<EmbeddedChunk> list) {}

}
