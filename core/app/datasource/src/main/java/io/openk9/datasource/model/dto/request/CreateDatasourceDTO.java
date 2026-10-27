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

package io.openk9.datasource.model.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DatasourceDTO;

import io.smallrye.graphql.api.Nullable;
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
@EqualsAndHashCode(callSuper = true)
public class CreateDatasourceDTO extends DatasourceDTO {

	@Positive
	@Description("PluginDriver to be associated")
	private long pluginDriverId;

	@Nullable
	@Description("Pipeline to be associated (optional)")
	private Long pipelineId;

	@Nullable
	@Description("Pipeline to be created and associated (optional)")
	private PipelineWithItemsDTO pipeline;

	@NotNull
	@Description("""
		The dataIndex to create. Its name and description are used as given;
		the knnIndex, chunkType, chunkWindowSize, embeddingDocTypeFieldId,
		embeddingJsonConfig, docTypeIds and settings it carries are the ones
		the datasource does not specify.""")
	private DataIndexDTO dataIndex;

	@Nullable
	@Description("""
		The field used during the text embedding of the first dataIndex,
		must be a valid docTypeFieldId.""")
	private Long embeddingDocTypeFieldId;

	@Nullable
	@Description("The list of documentType ids that the first dataIndex is composed of.")
	private Set<Long> docTypeIds;

}
