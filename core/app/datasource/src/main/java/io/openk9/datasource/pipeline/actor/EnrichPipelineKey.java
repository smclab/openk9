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

package io.openk9.datasource.pipeline.actor;

import io.openk9.common.util.SchedulingKey;

import java.util.Objects;

public record EnrichPipelineKey(SchedulingKey key, String contentId, String messageKey) {

	public EnrichPipelineKey {
		Objects.requireNonNull(contentId);
		assert !contentId.isBlank() : "contentId is blank";
	}

	public static EnrichPipelineKey of(
		SchedulingKey schedulingKey, String contentId, String messageKey) {
		return new EnrichPipelineKey(schedulingKey, contentId, messageKey);
	}

	public static EnrichPipelineKey fromString(String entityId) {
		var strings = entityId.split(String.valueOf(SchedulingKey.SEPARATOR));
		var schedulingKey = SchedulingKey.fromStrings(strings[0], strings[1]);
		return new EnrichPipelineKey(schedulingKey, strings[2], strings[3]);
	}

	public String asString() {
		return new StringBuilder()
			.append(key().asString())
			.append(SchedulingKey.SEPARATOR)
			.append(contentId())
			.append(SchedulingKey.SEPARATOR)
			.append(messageKey())
			.toString();
	}

}
