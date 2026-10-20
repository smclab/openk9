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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.model.DocTypeField;
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

	@Test
	void should_map_the_fixed_binary_fields_for_exact_filters() {

		var binary = render(VectorDataType.FLOAT32)
			.getJsonObject("properties")
			.getJsonObject("binary")
			.getJsonObject("properties");

		// identity and type are keywords: a filter matches the whole value
		assertEquals("keyword", typeOf(binary, "id"));
		assertEquals("keyword", typeOf(binary, "contentType"));
		assertEquals("keyword", typeOf(binary, "sourceUrl"));

		// name and caption are text with a keyword subfield
		assertTextWithKeyword(binary.getJsonObject("name"));
		assertTextWithKeyword(
			binary.getJsonObject("metadata")
				.getJsonObject("properties")
				.getJsonObject("caption"));
	}

	@Test
	void should_map_the_free_metadata_keys_as_text_even_when_dates() {

		var dynamicTemplates = render(VectorDataType.FLOAT32)
			.getJsonArray("dynamic_templates");

		// one template for strings, one for strings shaped as dates
		assertEquals(2, dynamicTemplates.size());

		for (int i = 0; i < dynamicTemplates.size(); i++) {
			var entry = dynamicTemplates.getJsonObject(i);
			var dynamicTemplate = entry.getJsonObject(
				entry.fieldNames().iterator().next());

			assertEquals("binary.metadata.*", dynamicTemplate.getString("path_match"));
			assertTextWithKeyword(dynamicTemplate.getJsonObject("mapping"));
		}

		assertEquals(
			"string",
			dynamicTemplates.getJsonObject(0)
				.getJsonObject("binary_metadata_strings")
				.getString("match_mapping_type"));
		assertEquals(
			"date",
			dynamicTemplates.getJsonObject(1)
				.getJsonObject("binary_metadata_dates")
				.getString("match_mapping_type"));
	}

	@Test
	void should_drop_a_malformed_page_instead_of_the_chunk() {

		var metadata = render(VectorDataType.FLOAT32)
			.getJsonObject("properties")
			.getJsonObject("binary")
			.getJsonObject("properties")
			.getJsonObject("metadata")
			.getJsonObject("properties");

		// a page that is not an integer is ignored, the chunk is indexed
		var page = metadata.getJsonObject("page");
		assertEquals("integer", page.getString("type"));
		assertTrue(page.getBoolean("ignore_malformed"));

		// generated stays a strict boolean
		assertEquals("boolean", typeOf(metadata, "generated"));
	}

	@Test
	void should_generate_searchable_default_fields_for_text_and_keywords() {

		// the fields the component template adds to the default doc type
		var docTypeFields = IndexMappingService.toDocTypeFields(
			render(VectorDataType.FLOAT32).getMap());
		Map<String, DocTypeField> fieldByName = new HashMap<>();
		flatten(docTypeFields, fieldByName);

		// text and keyword fields can be used by a FILTER
		for (String name : List.of(
			"binary.id", "binary.name", "binary.contentType",
			"binary.sourceUrl", "binary.metadata.caption")) {

			assertTrue(
				fieldByName.get(name).isSearchable(), name + " not searchable");
		}

		// page and generated cannot
		assertFalse(fieldByName.get("binary.metadata.page").isSearchable());
		assertFalse(fieldByName.get("binary.metadata.generated").isSearchable());

		// the dynamic templates are not fields
		assertTrue(fieldByName.keySet()
			.stream()
			.noneMatch(name -> name.startsWith("dynamic_templates")));
	}

	private static void flatten(
		Collection<DocTypeField> docTypeFields,
		Map<String, DocTypeField> fieldByName) {

		for (DocTypeField docTypeField : docTypeFields) {
			fieldByName.put(docTypeField.getName(), docTypeField);
			flatten(docTypeField.getSubDocTypeFields(), fieldByName);
		}
	}

	private static String typeOf(JsonObject properties, String field) {
		return properties.getJsonObject(field).getString("type");
	}

	private static void assertTextWithKeyword(JsonObject field) {
		assertEquals("text", field.getString("type"));
		assertEquals(
			"keyword",
			field.getJsonObject("fields")
				.getJsonObject("keyword")
				.getString("type"));
	}

	private static JsonObject renderVector(VectorDataType vectorDataType) {

		return render(vectorDataType)
			.getJsonObject("properties")
			.getJsonObject("vector");
	}

	private static JsonObject render(VectorDataType vectorDataType) {

		// resolve the knn parameters the same way the service does
		var componentTemplate = new EmbeddingComponentTemplate(
			"tenant", "e5-small", 768, vectorDataType);

		var rendered = IndexMappingService.renderEmbeddingComponentMappings(
			template(), componentTemplate);

		return new JsonObject(rendered);
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
