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

import jakarta.validation.constraints.NotNull;

import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.validation.json.Json;
import io.openk9.ml.grpc.EmbeddingOuterClass;

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
public class DataIndexDTO extends K9EntityDTO {

	@NotNull
	@Description("The field used during the text embedding, defined as JsonPath")
	private String textEmbeddingField;
	@NotNull
	@Description("The chunk strategy to apply")
	private EmbeddingOuterClass.ChunkType chunkType;
	@Json
	@Description("The configurations needed by the embedding model")
	private String embeddingJsonConfig;
	@Description(
		"The size of array chunks before and after the actual chunk, sent to embedding model")
	private Integer chunkWindowSize;

}
