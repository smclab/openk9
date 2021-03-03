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

import org.osgi.service.component.annotations.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(
	property = "entity.mapper=default",
	service = EntityMapper.class
)
public class ReflectionEntityMapper implements EntityMapper {

	@Override
	public Function<Object, Map<String, Object>> toMap(Class<?> clazz) {

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		List<Map.Entry<String, MethodHandle>> result =
			new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) == 0) {

				field.setAccessible(true);

				try {
					result.add(
						Map.entry(
							field.getName(),
							lookup.unreflectGetter(field))
					);
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}

		return entity -> result
			.stream()
			.collect(
				Collectors.toMap(
					Map.Entry::getKey,
					entry -> {
						try {
							Object fieldValue =
								entry.getValue().invoke(entity);

							if (fieldValue == null) {
								return NULL;
							}

							return fieldValue;

						}
						catch (Throwable throwable) {
							throw new IllegalStateException(throwable);
						}
					}));

	}

	@Override
	public Function<Object, Map<String, Object>> toMapWithoutPK(
		Class<?> clazz) {

		return toMap(clazz);
	}

}
