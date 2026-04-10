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

import java.util.Set;
import jakarta.inject.Inject;

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DocTypeServiceTest {

	@Inject
	AnalyzerService analyzerService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	DocTypeFieldService docTypeFieldService;

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

}
