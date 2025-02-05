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

import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.dto.PluginDriverDTO;

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
public class DatasourceConnectionDTO extends DatasourceDTO {

	@Nullable
	@Description("PluginDriver to be associated (optional)")
	private Long pluginDriverId;

	@Nullable
	@Description("Pipeline to be associated (optional)")
	private Long pipelineId;

	@Nullable
	@Description("PluginDriver to be created and associated (optional)")
	private PluginDriverDTO pluginDriver;

	@Nullable
	@Description("Pipeline to be created and associated (optional)")
	private PipelineWithItemsDTO pipeline;

	@Nullable
	@Description("Configurations used to create the dataIndex (optional)")
	private DataIndexDTO dataIndexDTO;

}
