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

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.validation.ValidationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeleteDocTypeFieldTest {

	private static final String DOC_TYPE_FIELD_NAME = "ddtft.docTypeField";
	private static final String DOC_TYPE_FIELD_FIELD_NAME = "ddtft_field";

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(DOC_TYPE_FIELD_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(false)
			.sortable(false)
			.boost(1.0)
			.build();

		EntitiesUtils.createEntity(dto, docTypeFieldService, sf);
	}

	@Test
	@DisplayName("Should throw ValidationException deleting DocTypeField with a different name")
	void should_fail_deleting_with_different_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		assertThrows(
			ValidationException.class,
			() -> docTypeFieldService.deleteById(
				docTypeField.getId(),
				"not-the-same-name.docTypeField")
				.await()
				.indefinitely()
		);
	}

	@Test
	@DisplayName("Should delete DocTypeField with the same name")
	void should_delete_with_correct_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		docTypeFieldService.deleteById(docTypeField.getId(), DOC_TYPE_FIELD_NAME)
			.await()
			.indefinitely();

		assertThrows(
			NoResultException.class,
			() -> EntitiesUtils.getEntity(
				DOC_TYPE_FIELD_NAME, docTypeFieldService, sf)
		);
	}

	@Test
	@DisplayName("Should throw ValidationException deleting DocTypeField with the same name in different case")
	void should_fail_deleting_with_correct_name_case_insensitive() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		assertThrows(
			ValidationException.class,
			() -> docTypeFieldService.deleteById(
					docTypeField.getId(),
					DOC_TYPE_FIELD_NAME.toUpperCase())
				.await()
				.indefinitely()
		);
	}

	@AfterEach
	void tearDown() {
		try {
			EntitiesUtils.removeEntity(
				DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);
		}
		catch (Exception ignored) {
		}
	}
}
