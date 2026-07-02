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

/**
 * How the importer resolves a package entity that already exists in the target
 * tenant (a natural-key match). Entities with no match are always created.
 *
 * <ul>
 *   <li>{@code OVERWRITE} — update the existing entity with the package values.</li>
 *   <li>{@code SKIP} — leave the existing entity untouched (default).</li>
 * </ul>
 */
public enum ImportMode {

	OVERWRITE,
	SKIP

}
