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

package io.openk9.datasource.service;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import jakarta.inject.Inject;

import io.openk9.datasource.model.dto.base.SearchConfigDTO;
import io.openk9.datasource.model.dto.request.HybridSearchPipelineDTO;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;

@QuarkusTest
class SearchConfigSearchPipelineTest {

	private static final String NAME = "SearchConfigSearchPipelineTest";

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	OpenSearchClient openSearchClient;

	@AfterEach
	void tearDown() throws IOException {
		searchConfigService.findAll()
			.await().indefinitely()
			.stream()
			.filter(searchConfig -> searchConfig.getName().equals(NAME))
			.forEach(searchConfig -> searchConfigService
				.deleteById(searchConfig.getId())
				.await().indefinitely());

		send("DELETE", NAME, null);
	}

	@Test
	void create_provisionsThePipelineWithTheDefaultBody() throws IOException {
		create();

		var processor = normalizationProcessor();

		Assertions.assertEquals(
			"min_max",
			processor.getJsonObject("normalization").getString("technique"));
		Assertions.assertEquals(
			"arithmetic_mean",
			processor.getJsonObject("combination").getString("technique"));
		Assertions.assertEquals(
			new JsonArray().add(0.3).add(0.7),
			weights(processor));
	}

	@Test
	void update_keepsTheTunedPipeline() throws IOException {
		var searchConfig = create();

		searchConfigService.configureHybridSearch(
				searchConfig.getId(),
				HybridSearchPipelineDTO.builder()
					.weights(List.of(0.5d, 0.5d))
					.build())
			.await().indefinitely();

		searchConfigService.update(
				searchConfig.getId(),
				SearchConfigDTO.builder()
					.name(NAME)
					.description("updated")
					.minScore(1F)
					.build())
			.await().indefinitely();

		Assertions.assertEquals(
			new JsonArray().add(0.5).add(0.5),
			weights(normalizationProcessor()));
	}

	@Test
	void create_keepsAPipelineAlreadyThere() throws IOException {
		send("PUT", NAME, SearchConfigService.getJsonBody(
			HybridSearchPipelineDTO.builder()
				.weights(List.of(0.1d, 0.9d))
				.build()
		).toString());

		create();

		Assertions.assertEquals(
			new JsonArray().add(0.1).add(0.9),
			weights(normalizationProcessor()));
	}

	@Test
	void delete_removesThePipeline() throws IOException {
		var searchConfig = create();

		searchConfigService.deleteById(searchConfig.getId())
			.await().indefinitely();

		Assertions.assertEquals(404, send("GET", NAME, null).status());
	}

	@Test
	void create_failsWhenOpenSearchIsUnreachable() throws IOException {
		var genericClient = Mockito.mock(OpenSearchGenericClient.class);
		Mockito.when(genericClient.execute(ArgumentMatchers.any()))
			.thenThrow(new ConnectException("Connection refused"));

		var unreachable = Mockito.mock(OpenSearchClient.class);
		Mockito.when(unreachable.generic()).thenReturn(genericClient);

		var service = new SearchConfigService(null);
		service.openSearchClient = unreachable;

		var searchConfig = new io.openk9.datasource.model.SearchConfig();
		searchConfig.setName(NAME);

		var failure = Assertions.assertThrows(
			Exception.class,
			() -> service.provisionSearchPipeline(searchConfig)
				.await().indefinitely());

		Throwable rootCause = failure;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}

		Assertions.assertInstanceOf(ConnectException.class, rootCause);
	}

	private io.openk9.datasource.model.SearchConfig create() {
		return searchConfigService.create(
				SearchConfigDTO.builder()
					.name(NAME)
					.minScore(0F)
					.build())
			.await().indefinitely();
	}

	private JsonObject normalizationProcessor() throws IOException {
		var response = send("GET", NAME, null);

		Assertions.assertEquals(200, response.status());

		return new JsonObject(response.body())
			.getJsonObject(NAME)
			.getJsonArray("phase_results_processors")
			.getJsonObject(0)
			.getJsonObject("normalization-processor");
	}

	private static JsonArray weights(JsonObject processor) {
		return processor
			.getJsonObject("combination")
			.getJsonObject("parameters")
			.getJsonArray("weights");
	}

	private PipelineResponse send(String method, String name, String json)
		throws IOException {

		var builder = Requests.builder()
			.method(method)
			.endpoint("_search/pipeline/" + name);

		if (json != null) {
			builder.json(json);
		}

		try (var response = openSearchClient.generic().execute(builder.build())) {
			return new PipelineResponse(
				response.getStatus(),
				response.getBody()
					.map(body -> body.bodyAsString())
					.orElse("")
			);
		}
	}

	private record PipelineResponse(int status, String body) {}

}
