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

package io.openk9.datasource.service.util;

import io.openk9.datasource.model.util.K9Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class K9EntityEvent<ENTITY extends K9Entity> {

	private final EventType type;
	private final ENTITY entity;
	private ENTITY previousEntity;

	public boolean isCreate() {
		return type == EventType.CREATE;
	}

	public boolean isUpdate() {
		return type == EventType.UPDATE;
	}

	public boolean isDelete() {
		return type == EventType.DELETE;
	}

	public enum EventType {
		CREATE,
		UPDATE,
		DELETE
	}

}
