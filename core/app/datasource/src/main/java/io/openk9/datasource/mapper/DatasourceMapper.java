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

package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.dto.base.DatasourceDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
	config = K9EntityMapper.class
)
public interface DatasourceMapper extends
	K9EntityMapper<Datasource, DatasourceDTO> {

	/**
	 * Updates the datasource with what the request says. The properties that
	 * shape its first dataIndex are optional in a request, and an omitted one
	 * keeps its current value instead of going back to the default.
	 */
	@Override
	@Mapping(
		target = "knnIndex",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
	)
	@Mapping(
		target = "chunkType",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
	)
	@Mapping(
		target = "chunkWindowSize",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
	)
	@Mapping(
		target = "embeddingJsonConfig",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
	)
	@Mapping(
		target = "settings",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
	)
	Datasource update(@MappingTarget Datasource entity, DatasourceDTO dto);

}