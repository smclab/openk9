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

package io.openk9.datasource.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import io.openk9.datasource.model.EmbeddingModel.VectorDataType;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VectorDataTypeValidatorTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	@Test
	void binary_requires_a_vector_size_multiple_of_8() {

		// BINARY with a vectorSize that is not a multiple of 8 is rejected
		var violations = validateVectorDataType(dto(VectorDataType.BINARY, 100));

		assertEquals(1, violations.size());
		assertEquals(
			"vectorSize",
			violations.getFirst().getPropertyPath().toString());
	}

	@Test
	void binary_accepts_a_vector_size_multiple_of_8() {

		// 768 is a multiple of 8, so BINARY is accepted
		var violations = validateVectorDataType(dto(VectorDataType.BINARY, 768));

		assertTrue(violations.isEmpty());
	}

	@Test
	void byte_does_not_require_a_vector_size_multiple_of_8() {

		// BYTE has no multiple-of-8 constraint
		var violations = validateVectorDataType(dto(VectorDataType.BYTE, 100));

		assertTrue(violations.isEmpty());
	}

	@Test
	void float32_is_always_accepted() {

		// FLOAT32 (the default) is never constrained on vectorSize
		var violations = validateVectorDataType(dto(VectorDataType.FLOAT32, 100));

		assertTrue(violations.isEmpty());
	}

	private static EmbeddingModelDTO dto(VectorDataType vectorDataType, int vectorSize) {
		var dto = new EmbeddingModelDTO();
		dto.setVectorDataType(vectorDataType);
		dto.setVectorSize(vectorSize);
		return dto;
	}

	/**
	 * Validates the DTO and keeps only the {@link ValidVectorDataType}
	 * violations, ignoring unrelated field constraints on the DTO.
	 */
	private static List<ConstraintViolation<EmbeddingModelDTO>> validateVectorDataType(
		EmbeddingModelDTO dto) {

		return validator.validate(dto).stream()
			.filter(violation -> violation
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType() == ValidVectorDataType.class)
			.toList();
	}

}
