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

package io.openk9.datasource.model.dto.base;

import io.smallrye.graphql.api.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class EmbeddingModelDTO extends K9EntityDTO {

	@Nullable
	@Description(
		"""
			The API URL for the embedding model's endpoint.
			Required only when using a custom embedding service or a model hosted
			on a private/internal network.
			"""
	)
	private String apiUrl;
	@Nullable
	@Description(
		"""
			Authentication API key required for accessing the embedding model's service.
			Necessary for providers that require authentication to use their embedding API.
			Ensure this key is kept confidential.
			"""
	)
	private String apiKey;
	@Description(
		"""
			Dimensionality of the embedding vectors produced by the model.
			This critical technical parameter determines the storage and processing requirements
			for vector representations.
			
			Most Common Dimensions:
			
			384 dimensions: Good for lightweight applications
			768 dimensions: Standard for many BERT-based models
			1,024 dimensions: High-performance models
			1,536 dimensions: Ultra-high performance models
			"""
	)
	private int vectorSize;

}
