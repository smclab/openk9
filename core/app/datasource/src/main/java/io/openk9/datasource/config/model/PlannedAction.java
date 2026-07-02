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
 * The action the importer plans for one package entity, together with the
 * resolved target-tenant id when the entity matched an existing one.
 *
 * @param ref        the package handle of the entity
 * @param type       the entity type
 * @param action     {@code CREATE} (no match), {@code OVERWRITE} or {@code SKIP}
 *                   (matched an existing entity, depending on the import mode)
 * @param existingId the id of the matched existing entity, or {@code null} for
 *                   {@code CREATE}
 */
public record PlannedAction(
	String ref, ConfigEntityType type, Action action, Long existingId) {

	public enum Action {
		CREATE,
		OVERWRITE,
		SKIP
	}

}
