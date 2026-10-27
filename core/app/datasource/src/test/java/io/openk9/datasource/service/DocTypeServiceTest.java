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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DocTypeServiceTest {

	private static final String ENTITY_NAME_PREFIX = "DocTypeServiceTest - ";

	@Inject
	AnalyzerService analyzerService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_delete_docType_used_by_the_shape_of_a_datasource() {
		// 1. A datasource that asks for its first dataIndex to use the docType
		var docType = docTypeService.create(DocTypeDTO.builder()
				.name(ENTITY_NAME_PREFIX + "docType")
				.build())
			.await().indefinitely();

		var pluginDriver = pluginDriverService
			.findByName("public", Initializer.INIT_DATASOURCE_PLUGIN)
			.await().indefinitely();

		var datasourceId = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(ENTITY_NAME_PREFIX + "datasource")
					.pluginDriverId(pluginDriver.getId())
					.docTypeIds(Set.of(docType.getId()))
					.dataIndex(DataIndexDTO.builder()
						.name(ENTITY_NAME_PREFIX + "dataIndex")
						.build())
					.build())
			.await().indefinitely().getEntity().getId();

		// 2. The docType can be deleted
		docTypeService.deleteById(docType.getId()).await().indefinitely();

		assertNull(docTypeService.findById(docType.getId()).await().indefinitely());

		// 3. And the datasource no longer asks for it
		var datasource = sessionFactory.withSession(session -> datasourceService
			.findById(session, datasourceId)
			.call(found -> session.fetch(found.getDocTypes()))
		).await().indefinitely();

		assertEquals(0, datasource.getDocTypes().size());

		// cleanup
		datasourceService.deleteById(datasourceId).await().indefinitely();
	}

	@Test
	void should_delete_docType() {

		var toBeDeleted = docTypeService.create(DocTypeDTO.builder()
				.name("ToBeDeleted")
				.build())
			.await().indefinitely();

		var first = docTypeService.addDocTypeField(
				toBeDeleted.getId(),
				DocTypeFieldDTO.builder()
					.name("field1")
					.fieldName("field1")
					.fieldType(FieldType.TEXT)
					.build()
			)
			.await().indefinitely().right;

		var second = docTypeService.addDocTypeField(
				toBeDeleted.getId(),
				DocTypeFieldDTO.builder()
					.name("field2")
					.fieldName("field2")
					.fieldType(FieldType.TEXT)
					.build()
			)
			.await().indefinitely().right;


		var subfield = docTypeFieldService.createSubField(
				second.getId(),
				DocTypeFieldDTO.builder()
					.name("subfield2")
					.fieldName("subfield2")
					.fieldType(FieldType.TEXT)
					.build()
			)
			.await().indefinitely();

		var analyzer = analyzerService.create(AnalyzerDTO.builder()
				.name("DocTypeServiceTest analyzer")
				.type("custom")
				.build())
			.await().indefinitely();

		docTypeFieldService.bindAnalyzer(first.getId(), analyzer.getId())
			.await().indefinitely();

		docTypeService.deleteById(toBeDeleted.getId()).await().indefinitely();

		analyzerService.deleteById(analyzer.getId()).await().indefinitely();

		var deletedDocType = docTypeService.findById(toBeDeleted.getId()).await().indefinitely();

		var deletedAnalyzer = analyzerService.findById(analyzer.getId()).await().indefinitely();

		var deletedFields = docTypeFieldService.findByIds(Set.of(
			first.getId(),
			second.getId(),
			subfield.getId()
		)).await().indefinitely();

		assertNull(deletedDocType);
		assertNull(deletedAnalyzer);

		assertEquals(3, deletedFields.size());

		for (DocTypeField deleted : deletedFields) {

			assertNull(deleted);

		}

	}

	@Test
	void should_bind_and_unbind_searchAnalyzer() {

		var docType = docTypeService.create(DocTypeDTO.builder()
				.name("SearchAnalyzerTest")
				.build())
			.await().indefinitely();

		var field = docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name("searchAnalyzerField")
					.fieldName("searchAnalyzerField")
					.fieldType(FieldType.TEXT)
					.build()
			)
			.await().indefinitely().right;

		var searchAnalyzer = analyzerService.create(AnalyzerDTO.builder()
				.name("SearchAnalyzerTest search_analyzer")
				.type("custom")
				.build())
			.await().indefinitely();

		// bind searchAnalyzer
		var bindResult = docTypeFieldService
			.bindSearchAnalyzer(field.getId(), searchAnalyzer.getId())
			.await().indefinitely();

		assertNotNull(bindResult.right);
		assertEquals(searchAnalyzer.getId(), bindResult.right.getId());

		// verify getSearchAnalyzer returns the bound analyzer
		var fetchedAnalyzer = docTypeFieldService
			.getSearchAnalyzer(field.getId())
			.await().indefinitely();

		assertNotNull(fetchedAnalyzer);
		assertEquals(searchAnalyzer.getId(), fetchedAnalyzer.getId());

		// unbind searchAnalyzer
		var unbindResult = docTypeFieldService
			.unbindSearchAnalyzer(field.getId())
			.await().indefinitely();

		assertNull(unbindResult.right);

		// verify getSearchAnalyzer returns null after unbind
		var fetchedAfterUnbind = docTypeFieldService
			.getSearchAnalyzer(field.getId())
			.await().indefinitely();

		assertNull(fetchedAfterUnbind);

		// cleanup
		docTypeService.deleteById(docType.getId()).await().indefinitely();
		analyzerService.deleteById(searchAnalyzer.getId()).await().indefinitely();
	}

	@Test
	void should_persist_offsetSource_for_text_field() {

		var docType = docTypeService.create(DocTypeDTO.builder()
				.name("OffsetSourceTextTest")
				.build())
			.await().indefinitely();

		docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name("offsetSourceField")
					.fieldName("offsetSourceField")
					.fieldType(FieldType.TEXT)
					.offsetSource(DocTypeField.OffsetSourceType.TERM_VECTOR)
					.build()
			)
			.await().indefinitely();

		var field = EntitiesUtils.getEntity(
			"offsetSourceField",
			docTypeFieldService,
			sessionFactory
		);

		assertEquals(DocTypeField.OffsetSourceType.TERM_VECTOR, field.getOffsetSource());

		docTypeService.deleteById(docType.getId()).await().indefinitely();
	}

	@Test
	void should_not_persist_unexpected_offsetSource_for_non_text_field() {

		var docType = docTypeService.create(DocTypeDTO.builder()
				.name("OffsetSourceKeywordTest")
				.build())
			.await().indefinitely();

		assertThrows(ConstraintViolationException.class, () -> docTypeService.addDocTypeField(
					docType.getId(),
					DocTypeFieldDTO.builder()
						.name("offsetSourceField")
						.fieldName("offsetSourceField")
						.fieldType(FieldType.KEYWORD)
						.offsetSource(DocTypeField.OffsetSourceType.TERM_VECTOR)
						.build()
				)
				.await()
				.indefinitely()
		);

	}

	@Test
	void should_not_persist_unexpected_offsetSource_for_non_text_subField() {

		var docType = docTypeService.create(DocTypeDTO.builder()
				.name("OffsetSourceSubFieldTest")
				.build())
			.await().indefinitely();

		var parent = docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name("parentField")
					.fieldName("parentField")
					.fieldType(FieldType.TEXT)
					.build()
			)
			.await().indefinitely().right;

		assertThrows(ConstraintViolationException.class, () -> docTypeFieldService.createSubField(
					parent.getId(),
					DocTypeFieldDTO.builder()
						.name("keywordSubField")
						.fieldName("keyword")
						.fieldType(FieldType.KEYWORD)
						.offsetSource(DocTypeField.OffsetSourceType.TERM_VECTOR)
						.build()
				)
				.await()
				.indefinitely()
		);

	}

}
