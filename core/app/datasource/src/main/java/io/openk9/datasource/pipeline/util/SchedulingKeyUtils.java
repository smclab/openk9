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

package io.openk9.datasource.pipeline.util;

import io.openk9.datasource.pipeline.actor.Scheduling;

public class SchedulingKeyUtils {

	public static String asString(Scheduling.Key key) {
		return asString(key.tenantId(), key.scheduleId());
	}

	public static String asString(String tenantId, String scheduleId) {
		return tenantId + "#" + scheduleId;
	}

	public static Scheduling.Key fromStrings(String tenantId, String scheduleId) {
		return new Scheduling.Key(tenantId, scheduleId);
	}

	public static Scheduling.Key fromString(String entityId) {
		String[] strings = entityId.split("#");
		return new Scheduling.Key(strings[0], strings[1]);
	}

}
