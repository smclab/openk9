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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.ProviderModel;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

public class EmbeddingServiceUtilsTest {

	@Test
	void should_get_everything_from_the_payload() {

		var datapayload = TestUtils
			.getResourceAsJsonObject("embedding/datapayload.json")
			.toBuffer()
			.getBytes();

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(datapayload);

		var metadataMap = EmbeddingService.getRoot(
			documentContext
		);

		assertTrue(metadataMap.containsKey("sample"));
		assertTrue(metadataMap.containsKey("store"));

		Map<String, Object> sample = (Map<String, Object>) metadataMap.get("sample");
		Map<String, Object> store = (Map<String, Object>) metadataMap.get("store");

		assertEquals(20, sample.get("age"));

		assertTrue(store.containsKey("bicycle"));
		assertTrue(store.containsKey("book"));

	}

	@Test
	void should_get_windows() {
		var size = 3;
		var total = 5;
		var integers = List.of(1, 2, 3, 4, 5);

		var result = integers.stream().map(i -> {
			var previous = EmbeddingService.getPreviousWindow(size, i, integers);

			var next = EmbeddingService.getNextWindow(size, i, total, integers);

			return new ItemWithNeighbors(i, previous, next);
		}).toList();

		assertEquals(0, result.get(0).previous().size());
		assertEquals(3, result.get(0).next().size());

		assertEquals(1, result.get(1).previous().size());
		assertEquals(3, result.get(1).next().size());

		assertEquals(2, result.get(2).previous().size());
		assertEquals(2, result.get(2).next().size());

		assertEquals(3, result.get(3).previous().size());
		assertEquals(1, result.get(3).next().size());

		assertEquals(3, result.get(4).previous().size());
		assertEquals(0, result.get(4).next().size());
	}

	record ItemWithNeighbors(int i, List<Integer> previous, List<Integer> next) {}

	@Test
	void mapToEmbeddingModelRequest_should_propagate_apiUrl_when_present() {

		var embeddingModel = new EmbeddingModel();
		embeddingModel.setApiKey("test-key");
		embeddingModel.setApiUrl("https://api.openai.com/v1/embeddings");
		var providerModel = new ProviderModel();
		providerModel.setProvider("OPEN_AI");
		providerModel.setModel("text-embedding-ada-002");
		embeddingModel.setProviderModel(providerModel);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		assertTrue(request.hasApiUrl());
		assertEquals("https://api.openai.com/v1/embeddings", request.getApiUrl());
		assertEquals("test-key", request.getApiKey());
		assertEquals("OPEN_AI", request.getProviderModel().getProvider());
		assertEquals("text-embedding-ada-002", request.getProviderModel().getModel());
	}

	@Test
	void mapToEmbeddingModelRequest_should_omit_apiUrl_when_null() {

		var embeddingModel = new EmbeddingModel();
		embeddingModel.setApiKey("test-key");
		embeddingModel.setApiUrl(null);
		var providerModel = new ProviderModel();
		providerModel.setProvider("HUGGING_FACE");
		providerModel.setModel("sentence-transformers/all-MiniLM-L6-v2");
		embeddingModel.setProviderModel(providerModel);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		assertFalse(
			request.hasApiUrl(),
			"apiUrl must be unset on the gRPC payload when the entity does not provide one"
		);
		assertEquals("test-key", request.getApiKey());
		assertEquals("HUGGING_FACE", request.getProviderModel().getProvider());
	}

}
