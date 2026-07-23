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

package io.openk9.datasource.pipeline.service;

import java.util.List;

import io.openk9.ml.grpc.EmbeddingOuterClass;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmbeddingServiceQueryTest {

	private static final String TENANT_ID = "1";
	private static final EmbeddingOuterClass.EmbeddingModel MODEL =
		EmbeddingOuterClass.EmbeddingModel.getDefaultInstance();

	@Test
	void should_build_a_text_only_query_request() {

		// 1. build a request with text and no media
		var request = EmbeddingService.buildEmbedQueryRequest(
			TENANT_ID, MODEL, "hello", null);

		// 2. only text is set, quantized as FLOAT32 like the index
		Assertions.assertEquals(TENANT_ID, request.getTenantId());
		Assertions.assertTrue(request.hasText());
		Assertions.assertEquals("hello", request.getText());
		Assertions.assertFalse(request.hasInline());
		Assertions.assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_FLOAT32,
			request.getVectorDataType());
	}

	@Test
	void should_build_an_inline_only_query_request() {

		// 1. build a request with media and no text
		var imageBytes = new byte[] {1, 2, 3};
		var request = EmbeddingService.buildEmbedQueryRequest(
			TENANT_ID, MODEL, null,
			new EmbeddingService.QueryMedia(imageBytes, "image/png"));

		// 2. only inline is set, mapped 1:1 to InlineMedia
		Assertions.assertFalse(request.hasText());
		Assertions.assertTrue(request.hasInline());
		Assertions.assertArrayEquals(
			imageBytes, request.getInline().getData().toByteArray());
		Assertions.assertEquals(
			"image/png", request.getInline().getContentType());
	}

	@Test
	void should_build_a_combined_text_and_inline_query_request() {

		// 1. build a request with both text and media
		var imageBytes = new byte[] {4, 5};
		var request = EmbeddingService.buildEmbedQueryRequest(
			TENANT_ID, MODEL, "a cat",
			new EmbeddingService.QueryMedia(imageBytes, "image/jpeg"));

		// 2. both text and inline are set for a single combined vector
		Assertions.assertTrue(request.hasText());
		Assertions.assertEquals("a cat", request.getText());
		Assertions.assertTrue(request.hasInline());
		Assertions.assertEquals(
			"image/jpeg", request.getInline().getContentType());
	}

	@Test
	void should_decode_a_float32_vector() {

		// 1. a FLOAT32 EmbeddedVector from the module
		var vector = EmbeddingOuterClass.EmbeddedVector.newBuilder()
			.setVectorDataType(
				EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_FLOAT32)
			.setDimension(3)
			.setF32(EmbeddingOuterClass.FloatVector.newBuilder()
				.addValues(0.1f)
				.addValues(0.2f)
				.addValues(0.3f)
				.build())
			.build();

		// 2. the float components are extracted
		var queryVector = EmbeddingService.toQueryVector(vector);

		Assertions.assertEquals(
			List.of(0.1f, 0.2f, 0.3f), queryVector.vector());
	}

	@Test
	void should_reject_a_non_float32_vector() {

		// 1. a quantized BYTE vector, which is not requested at query-time yet
		var vector = EmbeddingOuterClass.EmbeddedVector.newBuilder()
			.setVectorDataType(
				EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_BYTE)
			.setDimension(3)
			.setI8(ByteString.copyFrom(new byte[] {1, 2, 3}))
			.build();

		// 2. decoding fails fast as a contract violation
		Assertions.assertThrows(
			PayloadEmbeddingFailed.class,
			() -> EmbeddingService.toQueryVector(vector));
	}

}
