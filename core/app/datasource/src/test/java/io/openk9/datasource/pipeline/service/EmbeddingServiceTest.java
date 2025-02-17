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
import com.jayway.jsonpath.JsonPath;
import io.openk9.datasource.TestUtils;
import io.openk9.datasource.model.EmbeddingModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EmbeddingServiceTest {

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_fetch_current_embedding_model() {

		var current = sessionFactory.withTransaction(session -> session
				.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
				.getSingleResultOrNull()
			)
			.await().indefinitely();

		assertNotNull(current);
	}

	@Test
	void getMetadataMap() {

		var datapayload = TestUtils
			.getResourceAsJsonObject("embedding/datapayload.json")
			.toBuffer()
			.getBytes();

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(datapayload);

		var metadataMap = EmbeddingService.getMetadataMap(
			documentContext,
			"$.sample;$.store.bicycle"
		);

		assertTrue(metadataMap.containsKey("sample"));
		assertTrue(metadataMap.containsKey("store"));

		Map<String, Object> sample = (Map<String, Object>) metadataMap.get("sample");
		Map<String, Object> store = (Map<String, Object>) metadataMap.get("store");

		assertEquals(20, sample.get("age"));

		assertTrue(store.containsKey("bicycle"));
		assertFalse(store.containsKey("book"));

	}

	@Test
	void should_get_windows() {
		var window = 3;
		var total = 5;
		var integers = List.of(1, 2, 3, 4, 5);

		var result = integers.stream().map(i -> {
			var previous = EmbeddingService.getPrevious(window, i, integers);

			var next = EmbeddingService.getNext(window, i, total, integers);

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
}