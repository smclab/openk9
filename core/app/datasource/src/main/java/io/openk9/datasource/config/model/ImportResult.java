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

package io.openk9.datasource.config.model;

import java.util.Map;

/**
 * The outcome of applying an {@link ImportPlan}: how many entities were created,
 * overwritten and skipped, and the final {@code handle -> real target id} map
 * (useful to callers that need to locate the imported entities afterwards).
 *
 * @param created     number of entities created
 * @param overwritten number of entities overwritten
 * @param skipped     number of entities left untouched
 * @param resolvedIds package handle to the target-tenant id it resolved to
 */
public record ImportResult(
	int created, int overwritten, int skipped, Map<String, Long> resolvedIds) {

	public ImportResult {
		resolvedIds = Map.copyOf(resolvedIds);
	}

}
