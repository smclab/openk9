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

package com.openk9.sql.api.entity;

import java.util.Map;
import java.util.function.Function;

public interface EntityMapper {

	Function<Object, Map<String, Object>> toMap(Class<?> clazz);

	Function<Object, Map<String, Object>> toMapWithoutPK(Class<?> clazz);

	Object NULL = new Object();

	String ENTITY_MAPPER_PROPERTY = "entity.mapper";

}
