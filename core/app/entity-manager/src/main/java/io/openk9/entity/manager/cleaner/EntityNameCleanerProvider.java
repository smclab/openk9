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

package io.openk9.entity.manager.cleaner;

import io.quarkus.arc.Unremovable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
@Unremovable
public class EntityNameCleanerProvider {

	public EntityNameCleaner get(String type) {
		return handlers
			.stream()
			.filter(e -> e.getEntityType().equals(type))
			.findFirst()
			.orElseGet(
				() -> EntityNameCleaner.DefaultEntityNameCleaner.of(type));
	}

	@Inject
	Instance<EntityNameCleaner> handlers;

}
