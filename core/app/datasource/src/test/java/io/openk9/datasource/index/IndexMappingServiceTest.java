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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.model.Analyzer;
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

	private static void printTree(DocTypeField docTypeField, String depth) {
		System.out.println(depth + docTypeField.getFieldName());
		for (DocTypeField child : docTypeField.getSubDocTypeFields()) {
			printTree(child, depth + "\t");
		}
	}

}
