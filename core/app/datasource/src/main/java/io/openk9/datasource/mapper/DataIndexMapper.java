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

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.dto.DataIndexDTO;

import org.hibernate.reactive.mutiny.Mutiny;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
	config = K9EntityMapper.class,
	uses = ReferenceMapper.class
)
public interface DataIndexMapper extends
	K9EntityMapper<DataIndex, DataIndexDTO> {

	@Mapping(
		target = "embeddingDocTypeField",
		source = "embeddingDocTypeFieldId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	@Mapping(
		target = "datasource",
		source = "datasourceId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	DataIndex create(
		DataIndexDTO dto,
		@Context Mutiny.Session session);

	@BeanMapping(
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(
		target = "embeddingDocTypeField",
		source = "embeddingDocTypeFieldId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	@Mapping(
		target = "datasource",
		source = "datasourceId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	DataIndex patch(
		@MappingTarget DataIndex entity,
		DataIndexDTO dto,
		@Context Mutiny.Session session);

	@Mapping(
		target = "embeddingDocTypeField",
		source = "embeddingDocTypeFieldId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	@Mapping(
		target = "datasource",
		source = "datasourceId",
		qualifiedByName = ReferenceMapper.GET_REFERENCE)
	DataIndex update(
		@MappingTarget DataIndex entity,
		DataIndexDTO dto,
		@Context Mutiny.Session session);


}