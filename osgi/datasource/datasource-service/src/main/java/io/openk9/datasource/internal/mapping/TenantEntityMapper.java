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

package io.openk9.datasource.internal.mapping;

import io.openk9.datasource.model.Tenant;
import io.openk9.sql.api.entity.EntityMapper;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.function.Function;

@Component(
	immediate = true,
	property = EntityMapper.ENTITY_MAPPER_PROPERTY + "=io.openk9.datasource.model.Tenant",
	service = EntityMapper.class
)
public class TenantEntityMapper implements EntityMapper {

	@Override
	public Function<Object, Map<String, Object>> toMap(Class<?> clazz) {
		return obj -> {

			Tenant tenant =(Tenant)obj;

			return Map.of(
				"tenantId", tenant.getTenantId(),
				"name", tenant.getName(),
				"virtualHost", tenant.getVirtualHost(),
				"jsonConfig", tenant.getJsonConfig()
			);

		};
	}

	@Override
	public Function<Object, Map<String, Object>> toMapWithoutPK(
		Class<?> clazz) {

		return obj -> {

			Tenant tenant =(Tenant)obj;

			return Map.of(
				"name", tenant.getName(),
				"virtualHost", tenant.getVirtualHost()
			);

		};
	}

}
