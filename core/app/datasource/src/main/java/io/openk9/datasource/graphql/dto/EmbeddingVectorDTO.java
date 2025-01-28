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

package io.openk9.datasource.graphql.dto;

import io.openk9.ml.grpc.EmbeddingOuterClass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EmbeddingVectorDTO {

	@Description("It is the field that contains the data that has to be embedded.")
	private long embeddingDocTypeField;

	@Description(
		"It defines the strategy to use when chunking the text that is going to be embedded.")
	private EmbeddingOuterClass.ChunkType chunkType;

	@Description(
		"It defines the number of previous and next chunks that are related to every chunk.")
	private int chunkWindowSize;

	@Description(
		"It defines the configurations that the user can pass to the embedding model service used.")
	private String embeddingJsonConfig;


}
