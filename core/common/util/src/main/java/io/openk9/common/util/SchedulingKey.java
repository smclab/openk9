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

package io.openk9.common.util;

import java.util.Objects;

public record SchedulingKey(String tenantId, String scheduleId) {

	public SchedulingKey {
		Objects.requireNonNull(tenantId);
		Objects.requireNonNull(scheduleId);
		assert tenantId.isBlank() : "tenantId is blank";
		assert scheduleId.isBlank() : "scheduleId is blank";
	}

	public static String asString(SchedulingKey key) {
		return asString(key.tenantId(), key.scheduleId());
	}

	public static String asString(String tenantId, String scheduleId) {
		return tenantId + "#" + scheduleId;
	}

	public static SchedulingKey fromStrings(String tenantId, String scheduleId) {
		return new SchedulingKey(tenantId, scheduleId);
	}

	public static SchedulingKey fromString(String entityId) {
		String[] strings = entityId.split("#");
		return new SchedulingKey(strings[0], strings[1]);
	}

	public String asString() {
		return asString(this);
	}

}
