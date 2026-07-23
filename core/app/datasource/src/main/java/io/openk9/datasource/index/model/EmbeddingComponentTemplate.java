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

package io.openk9.datasource.index.model;

import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.model.EmbeddingModel;

public record EmbeddingComponentTemplate(
	String tenantId,
	String embeddingModelName,
	int vectorSize,
	EmbeddingModel.VectorDataType vectorDataType
) {

	/**
	 * {@code index.knn.algo_param.ef_search} applied to quantized indexes.
	 * Quantization (byte/binary) loses recall, so the search-time exploration
	 * factor is raised well above the OpenSearch default of {@code 100} to
	 * recover it.
	 */
	public static final int EF_SEARCH_QUANTIZED = 512;

	public EmbeddingComponentTemplate {

		assert embeddingModelName != null && !embeddingModelName.isEmpty();
		assert tenantId != null && !tenantId.isEmpty();
		assert vectorSize > 0;

		vectorDataType = vectorDataType != null
			? vectorDataType
			: EmbeddingModel.VectorDataType.FLOAT32;
	}

	@Override
	public String embeddingModelName() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return OpenSearchUtils.nameSanitizer(
			String.format("%s-%s", tenantId, embeddingModelName));
	}

	@Override
	public String tenantId() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether the vector is quantized (anything other than {@code FLOAT32}).
	 * Quantized indexes get an explicit {@code knn_vector} method and a raised
	 * {@code ef_search}.
	 *
	 * @return {@code true} for {@code BYTE} and {@code BINARY}
	 */
	public boolean quantized() {
		return vectorDataType != EmbeddingModel.VectorDataType.FLOAT32;
	}

	/**
	 * The OpenSearch {@code knn_vector} {@code data_type} for this vector type.
	 *
	 * @return {@code "byte"}, {@code "binary"} or {@code null} for
	 * {@code FLOAT32} (which uses the OpenSearch default)
	 */
	public String dataType() {
		return switch (vectorDataType) {
			case BYTE -> "byte";
			case BINARY -> "binary";
			case FLOAT32 -> null;
		};
	}

	/**
	 * The OpenSearch k-NN engine backing this vector type.
	 *
	 * @return {@code "lucene"} for {@code BYTE}, {@code "faiss"} for
	 * {@code BINARY}, {@code null} for {@code FLOAT32}
	 */
	public String engine() {
		return switch (vectorDataType) {
			case BYTE -> "lucene";
			case BINARY -> "faiss";
			case FLOAT32 -> null;
		};
	}

	/**
	 * The OpenSearch k-NN {@code space_type} for this vector type.
	 *
	 * @return {@code "cosinesimil"} for {@code BYTE}, {@code "hamming"} for
	 * {@code BINARY}, {@code null} for {@code FLOAT32}
	 */
	public String spaceType() {
		return switch (vectorDataType) {
			case BYTE -> "cosinesimil";
			case BINARY -> "hamming";
			case FLOAT32 -> null;
		};
	}

}
