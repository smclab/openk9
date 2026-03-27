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
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
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
public class UpdateDocTypeFieldTest {

	private static final String DOC_TYPE_FIELD_NAME = "udtft.docTypeField";
	private static final String DOC_TYPE_FIELD_FIELD_NAME = "udtft_field";
	private static final String UPDATED_FIELD_NAME = "udtft_field_updated";

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
	@DisplayName("Should update DocTypeField with the correct validation name")
	void should_update_with_correct_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		DocTypeFieldDTO updateDto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(UPDATED_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(true)
			.sortable(false)
			.boost(2.0)
			.build();

		var updated = docTypeFieldService.update(
				docTypeField.getId(), updateDto, DOC_TYPE_FIELD_NAME)
			.await()
			.indefinitely();

		assertEquals(UPDATED_FIELD_NAME, updated.getFieldName());
		assertEquals(true, updated.getSearchable());
		assertEquals(2.0, updated.getBoost());
	}

	@Test
	@DisplayName("Should throw ValidationException updating DocTypeField with a different name")
	void should_fail_updating_with_different_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		DocTypeFieldDTO updateDto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(UPDATED_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(true)
			.sortable(false)
			.boost(1.0)
			.build();

		assertThrows(
			ValidationException.class,
			() -> docTypeFieldService.update(
				docTypeField.getId(),
				updateDto,
				"not-the-same-name.docTypeField")
				.await()
				.indefinitely()
		);
	}

	@Test
	@DisplayName("Should patch DocTypeField with the correct validation name")
	void should_patch_with_correct_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		DocTypeFieldDTO patchDto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(DOC_TYPE_FIELD_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(true)
			.sortable(false)
			.boost(1.0)
			.build();

		var patched = docTypeFieldService.patch(
				docTypeField.getId(), patchDto, DOC_TYPE_FIELD_NAME)
			.await()
			.indefinitely();

		assertEquals(true, patched.getSearchable());
	}

	@Test
	@DisplayName("Should throw ValidationException patching DocTypeField with a different name")
	void should_fail_patching_with_different_name() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		DocTypeFieldDTO patchDto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(DOC_TYPE_FIELD_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(true)
			.sortable(false)
			.boost(1.0)
			.build();

		assertThrows(
			ValidationException.class,
			() -> docTypeFieldService.patch(
				docTypeField.getId(),
				patchDto,
				"not-the-same-name.docTypeField")
				.await()
				.indefinitely()
		);
	}

	@Test
	@DisplayName("Should update DocTypeField with the correct validation name in different case")
	void should_update_with_correct_name_case_insensitive() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME, docTypeFieldService, sf);

		DocTypeFieldDTO updateDto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName(UPDATED_FIELD_NAME)
			.fieldType(FieldType.TEXT)
			.searchable(false)
			.sortable(false)
			.boost(1.0)
			.build();

		var updated = docTypeFieldService.update(
				docTypeField.getId(), updateDto,
				DOC_TYPE_FIELD_NAME.toUpperCase())
			.await()
			.indefinitely();

		assertEquals(UPDATED_FIELD_NAME, updated.getFieldName());
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
