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

import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.AnalyzerDTO;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
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


	}


}
