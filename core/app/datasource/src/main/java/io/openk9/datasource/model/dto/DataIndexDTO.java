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

package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.validation.json.Json;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import io.smallrye.graphql.api.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DataIndexDTO extends K9EntityDTO {

	@Nullable
	@Builder.Default
	@Description("""
		Define if this index is a knn index, this property enables
		vector similarity search features on this DataIndex.""")
	private Boolean knnIndex = false;

	@Nullable
	@Description("The number of chunks before and after every chunk.")
	private Integer chunkWindowSize;

	@Nullable
	@Description("The chunk strategy to apply.")
	private EmbeddingOuterClass.ChunkType chunkType;

	@Nullable
	@Description(
		"""
			The field used during the text embedding,
			must be a valid docTypeFieldId.
			""")
	private Long embeddingDocTypeFieldId;

	@Json
	@Nullable
	@Description("The configurations used by the embedding model, if needed.")
	private String embeddingJsonConfig;

}
