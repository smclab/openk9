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

package io.openk9.datasource.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.openk9.datasource.config.model.ConfigEntityType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure guard (no Quarkus boot, so it runs in a normal build): the importer resolves
 * ConfigEntityMapper's {@code entity(...)}/{@code update(...)} overloads by entity
 * type, and each overload maps the DTO its own signature declares. These assert that,
 * for every non-join exportable type, both overloads exist and carry a DTO compatible
 * with the one the {@link ConfigEntityType} declares: create with the exact declared
 * DTO (so create-only fields like {@code RAGConfiguration.type} are mapped), update
 * with that DTO or a supertype. A mismatch fails the build instead of surfacing at
 * import time, as it once did with {@code RAGConfiguration.type}.
 */
public class ConfigEntityMapperTest {

	@Test
	void every_exportable_type_has_an_inverse_entity_method() {
		List<String> mismatches = new ArrayList<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			Class<?> entityClass = type.getEntityType();
			if (ConfigMatcher.isJoinEntity(entityClass)) {
				// join entities carry no scalar DTO: the importer rebuilds them
				// from their resolved endpoints, it does not map them.
				continue;
			}
			Method method = findMethod(
				"entity",
				m -> m.getParameterCount() == 1 && m.getReturnType() == entityClass);
			if (method == null) {
				mismatches.add("no entity(...) returning " + entityClass.getSimpleName());
			}
			else if (method.getParameterTypes()[0] != type.getAttributesType()) {
				// create must map the exact DTO the enum declares (the deserialized
				// instance's type): a supertype would drop create-only fields.
				mismatches.add("entity(...) for " + entityClass.getSimpleName()
					+ " takes " + method.getParameterTypes()[0].getSimpleName()
					+ ", expected declared " + type.getAttributesType().getSimpleName());
			}
		}

		assertTrue(
			mismatches.isEmpty(),
			"ConfigEntityMapper.entity(...) must map the DTO its ConfigEntityType "
				+ "declares; mismatches: " + mismatches);
	}

	@Test
	void every_exportable_type_has_an_inverse_update_method() {
		List<String> mismatches = new ArrayList<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			Class<?> entityClass = type.getEntityType();
			if (ConfigMatcher.isJoinEntity(entityClass)) {
				continue;
			}
			Method method = findMethod(
				"update",
				m -> m.getParameterCount() == 2 && m.getParameterTypes()[0] == entityClass);
			if (method == null) {
				mismatches.add("no update(" + entityClass.getSimpleName() + ", ...)");
			}
			else if (!method.getParameterTypes()[1]
					.isAssignableFrom(type.getAttributesType())) {
				// update may declare the base DTO, but must still accept the declared
				// (deserialized) attributes instance.
				mismatches.add("update(" + entityClass.getSimpleName() + ", "
					+ method.getParameterTypes()[1].getSimpleName()
					+ ") cannot accept declared " + type.getAttributesType().getSimpleName());
			}
		}

		assertTrue(
			mismatches.isEmpty(),
			"ConfigEntityMapper.update(...) must accept the DTO its ConfigEntityType "
				+ "declares; mismatches: " + mismatches);
	}

	private static Method findMethod(String name, Predicate<Method> match) {
		for (Method method : ConfigEntityMapper.class.getMethods()) {
			if (method.getName().equals(name) && match.test(method)) {
				return method;
			}
		}
		return null;
	}

}
