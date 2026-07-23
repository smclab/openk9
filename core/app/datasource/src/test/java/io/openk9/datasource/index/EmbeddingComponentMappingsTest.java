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

package io.openk9.datasource.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.model.EmbeddingModel.VectorDataType;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

/**
 * Renders the real {@code embeddingComponentMappings} Qute template through a
 * standalone engine, driven by the {@link EmbeddingComponentTemplate} knn
 * parameters, and asserts the resulting {@code knn_vector} field for each
 * {@link VectorDataType}.
 */
public class EmbeddingComponentMappingsTest {

	@Test
	void should_render_float32_as_plain_knn_vector() {

		// FLOAT32 keeps the OpenSearch default: no data_type, no method block
		var vector = renderVector(VectorDataType.FLOAT32);

		assertEquals("knn_vector", vector.getString("type"));
		assertEquals(768, vector.getInteger("dimension"));
		assertNull(vector.getString("data_type"));
		assertFalse(vector.containsKey("method"));
	}

	@Test
	void should_render_byte_with_lucene_cosinesimil() {

		// BYTE -> byte data_type on the lucene engine, cosinesimil space
		var vector = renderVector(VectorDataType.BYTE);

		assertEquals("knn_vector", vector.getString("type"));
		assertEquals(768, vector.getInteger("dimension"));
		assertEquals("byte", vector.getString("data_type"));

		var method = vector.getJsonObject("method");
		assertEquals("hnsw", method.getString("name"));
		assertEquals("lucene", method.getString("engine"));
		assertEquals("cosinesimil", method.getString("space_type"));
	}

	@Test
	void should_render_binary_with_faiss_hamming() {

		// BINARY -> binary data_type on the faiss engine, hamming space
		var vector = renderVector(VectorDataType.BINARY);

		assertEquals("knn_vector", vector.getString("type"));
		assertEquals(768, vector.getInteger("dimension"));
		assertEquals("binary", vector.getString("data_type"));

		var method = vector.getJsonObject("method");
		assertEquals("hnsw", method.getString("name"));
		assertEquals("faiss", method.getString("engine"));
		assertEquals("hamming", method.getString("space_type"));
	}

	private static JsonObject renderVector(VectorDataType vectorDataType) {

		// resolve the knn parameters the same way the service does
		var componentTemplate = new EmbeddingComponentTemplate(
			"tenant", "e5-small", 768, vectorDataType);

		var rendered = IndexMappingService.renderEmbeddingComponentMappings(
			template(), componentTemplate);

		return new JsonObject(rendered)
			.getJsonObject("properties")
			.getJsonObject("vector");
	}

	private static Template template() {

		try (InputStream is = EmbeddingComponentMappingsTest.class
			.getResourceAsStream("/templates/embeddingComponentMappings")) {

			var content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			return Engine.builder()
				.addDefaults()
				.strictRendering(false)
				.build()
				.parse(content);
		}
		catch (Exception e) {
			throw new IllegalStateException(
				"cannot load embeddingComponentMappings template", e);
		}
	}

}
