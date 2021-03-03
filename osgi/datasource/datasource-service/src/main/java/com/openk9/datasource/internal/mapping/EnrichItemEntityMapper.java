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

package com.openk9.datasource.internal.mapping;

import com.openk9.datasource.model.EnrichItem;
import com.openk9.sql.api.entity.EntityMapper;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.function.Function;

@Component(
	immediate = true,
	property = EntityMapper.ENTITY_MAPPER_PROPERTY + "=com.openk9.datasource.model.EnrichItem",
	service = EntityMapper.class
)
public class EnrichItemEntityMapper implements EntityMapper {

	@Override
	public Function<Object, Map<String, Object>> toMap(Class<?> clazz) {
		return obj -> {

			EnrichItem enrichItem =(EnrichItem)obj;

			return Map.of(
				"enrichItemId", enrichItem.getEnrichItemId(),
				"_position", enrichItem.get_position(),
				"active", enrichItem.getActive(),
				"jsonConfig",enrichItem.getJsonConfig(),
				"enrichPipelineId", enrichItem.getEnrichPipelineId(),
				"name", enrichItem.getName(),
				"serviceName", enrichItem.getServiceName()
			);

		};
	}

	@Override
	public Function<Object, Map<String, Object>> toMapWithoutPK(Class<?> clazz) {
		return obj -> {

			EnrichItem enrichItem =(EnrichItem)obj;

			return Map.of(
				"_position", enrichItem.get_position(),
				"active", enrichItem.getActive(),
				"jsonConfig",enrichItem.getJsonConfig(),
				"enrichPipelineId", enrichItem.getEnrichPipelineId(),
				"name", enrichItem.getName(),
				"serviceName", enrichItem.getServiceName()
			);

		};
	}


}
