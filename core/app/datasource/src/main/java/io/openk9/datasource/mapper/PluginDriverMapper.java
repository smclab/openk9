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

import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
	config = K9EntityMapper.class
)
public interface PluginDriverMapper extends
	K9EntityMapper<PluginDriver, PluginDriverDTO> {

	/**
	 * Creates a {@link PluginDriver} from the dto, defaulting a missing
	 * provisioning to {@link PluginDriver.Provisioning#USER}.
	 */
	@Override
	@Mapping(target = "provisioning", defaultValue = "USER")
	PluginDriver create(PluginDriverDTO dto);

	/**
	 * Replaces the state of the {@link PluginDriver} with the dto, defaulting
	 * a missing provisioning to {@link PluginDriver.Provisioning#USER}.
	 */
	@Override
	@Mapping(target = "provisioning", defaultValue = "USER")
	PluginDriver update(@MappingTarget PluginDriver entity, PluginDriverDTO dto);

}