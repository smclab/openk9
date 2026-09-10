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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class IndexMappingServiceTest {

	private static final JsonObject mappings = TestUtils
		.getResourceAsJsonObject("es/mappings_response.json");
	private static final DocType docType;
	private static final DocTypeField title, titleKeyword, titleTrigram;

	static {
		docType = new DocType();
		docType.setName("web");
		docType.setId(1L);

		title = new DocTypeField();
		title.setId(2L);
		title.setDocType(docType);
		title.setFieldName("title");
		title.setName("web.title");
		title.setDescription("persisted");
		title.setFieldType(FieldType.TEXT);

		titleKeyword = new DocTypeField();
		titleKeyword.setId(3L);
		titleKeyword.setDocType(docType);
		titleKeyword.setDescription("persisted");
		titleKeyword.setFieldName("keyword");
		titleKeyword.setName("web.title.keyword");
		titleKeyword.setFieldType(FieldType.KEYWORD);
		titleKeyword.setParentDocTypeField(title);

		titleTrigram = new DocTypeField();
		titleTrigram.setId(4L);
		Analyzer trigram = new Analyzer();
		trigram.setId(5L);
		trigram.setName("trigram");
		trigram.setType("custom");
		titleTrigram.setDocType(docType);
		titleTrigram.setDescription("persisted");
		titleTrigram.setFieldName("trigram");
		titleTrigram.setName("web.title.trigram");
		titleTrigram.setFieldType(FieldType.TEXT);
		titleTrigram.setAnalyzer(trigram);
		titleTrigram.setParentDocTypeField(title);

		title.setSubDocTypeFields(new LinkedHashSet<>(List.of(titleKeyword, titleTrigram)));

		docType.setDocTypeFields(new LinkedHashSet<>(List.of(title)));
	}

	@Test
	void shouldMapToDocTypeFields() {

		List<DocTypeField> docTypeFields =
			IndexMappingService.toDocTypeFields(mappings.getMap());

		Map<String, List<DocTypeField>> docTypeAndFieldsGroup =
			IndexMappingService.toDocTypeAndFieldsGroup(
				docTypeFields, List.of("web", "resources", "document"));

		Set<DocType> docTypes =
			IndexMappingService.mergeDocTypes(docTypeAndFieldsGroup, List.of(docType));

		// mappings contains default (acl, documentTypes)
		assertTrue(
			docTypeAndFieldsGroup
				.get(DocType.DEFAULT_NAME)
				.stream()
				.filter(f -> "acl".equals(f.getFieldName()))
				.flatMap(f -> f.getSubDocTypeFields().stream())
				.anyMatch(f -> "public".equals(f.getFieldName()))
		);

		// mappings contains default ignorable fields (rawContent, datasourceId)
		assertTrue(
			docTypeAndFieldsGroup
				.get(DocType.DEFAULT_NAME)
				.stream()
				.anyMatch(f -> "datasourceId".equals(f.getFieldName()))
		);

		assertTrue(
			docTypeAndFieldsGroup
				.get("web")
				.stream()
				.anyMatch(f -> "title".equals(f.getFieldName()))
		);

		// contains web.title, already persisted
		assertTrue(docTypes
			.stream()
			.filter(dt -> "web".equals(dt.getName()))
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.anyMatch(f -> f.equals(title))
		);

		// contains web.content, auto-generated
		assertTrue(docTypes
			.stream()
			.filter(dt -> dt.getName().equals("web"))
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.anyMatch(f -> "content".equals(f.getFieldName()))
		);

		// ignored fields does not exist
		assertTrue(docTypes
			.stream()
			.filter(dt -> DocType.DEFAULT_NAME.equals(dt.getName()))
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.noneMatch(f -> IndexMappingService.isIgnoredFieldPath(f.getPath()))
		);

	}

	@Test
	void shouldLayRecordedSettingsOverDerivedOnesExceptTheAnalysis() {
		// a docType whose field carries an analyzer, as the docTypes hold it now
		var analyzer = new Analyzer();
		analyzer.setId(6L);
		analyzer.setName("current_analyzer");
		analyzer.setType("standard");
		analyzer.setJsonConfig("{\"type\": \"standard\"}");

		var content = new DocTypeField();
		content.setId(7L);
		content.setFieldName("content");
		content.setName("analyzed.content");
		content.setFieldType(FieldType.TEXT);
		content.setAnalyzer(analyzer);

		var analyzed = new DocType();
		analyzed.setId(8L);
		analyzed.setName("analyzed");
		analyzed.setDocTypeFields(new LinkedHashSet<>(List.of(content)));
		content.setDocType(analyzed);

		var dataIndex = new DataIndex();
		dataIndex.setDocTypes(Set.of(analyzed));

		// the settings recorded at creation: a key of the operator's own, a
		// derived default overridden, a stale definition of the same analyzer
		// and a filter the docTypes know nothing about
		var recorded = new JsonObject()
			.put("index", new JsonObject()
				.put("number_of_replicas", 2)
				.put("highlight", new JsonObject().put("max_analyzed_offset", "5")))
			.put("analysis", new JsonObject()
				.put("analyzer", new JsonObject()
					.put("current_analyzer", new JsonObject()
						.put("type", "whitespace")
						.put("stopwords", "_english_")))
				.put("filter", new JsonObject()
					.put("operator_filter", new JsonObject().put("type", "lowercase"))))
			.getMap();

		var settings = IndexMappingService.getSettings(recorded, dataIndex);

		// what was recorded wins over what the docTypes derive
		assertEquals("2", settings.get("index.number_of_replicas"));
		assertEquals("5", settings.get("index.highlight.max_analyzed_offset"));

		// but the analyzer is the one the docTypes hold now, whole (the
		// analysis block is not under index yet: OpenSearch normalizes it when
		// the template is applied)
		assertEquals(
			"standard", settings.get("analysis.analyzer.current_analyzer.type"));
		assertNull(settings.get("analysis.analyzer.current_analyzer.stopwords"));

		// and a definition the docTypes do not know is kept
		assertEquals(
			"lowercase", settings.get("analysis.filter.operator_filter.type"));
	}

	@Test
	void shouldDeriveTheSettingsWhenNoneWereRecorded() {
		var dataIndex = new DataIndex();
		dataIndex.setDocTypes(Set.of(docType));

		var settings = IndexMappingService.getSettings(null, dataIndex);

		assertEquals("10000000", settings.get("index.highlight.max_analyzed_offset"));
	}

	private static void printTree(DocTypeField docTypeField, String depth) {
		System.out.println(depth + docTypeField.getFieldName());
		for (DocTypeField child : docTypeField.getSubDocTypeFields()) {
			printTree(child, depth + "\t");
		}
	}

}
