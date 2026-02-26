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

package io.openk9.common.util.ingestion;

import java.util.Arrays;
import java.util.Objects;

public record ShardingKey(String... elements) {

	private static final char SEPARATOR = '#';

	public ShardingKey {
		assert elements.length >= 2 : "Must have at least 2 elements (tenantId, scheduleId)";
	}

	public static String asString(ShardingKey key) {
		return asString(key.elements);
	}

	public static String asString(String... elements) {
		return String.join(String.valueOf(SEPARATOR), elements);
	}

	public static ShardingKey concat(ShardingKey upperKey, String... elements) {
		String[] inherits = upperKey.elements();

		var newLength = inherits.length + elements.length;

		var strings = new String[newLength];

		System.arraycopy(inherits, 0, strings, 0, inherits.length);

		System.arraycopy(elements, 0,
			strings,
			inherits.length,
			newLength - inherits.length
		);

		return new ShardingKey(strings);
	}

	public static ShardingKey fromStrings(String... elements) {
		return new ShardingKey(elements);
	}

	public static ShardingKey fromString(String entityId) {
		String[] strings = entityId.split(String.valueOf(SEPARATOR));
		return new ShardingKey(strings);
	}

	public String tenantId() {
		return elements[0];
	}

	public String scheduleId() {
		return elements[1];
	}

	public ShardingKey baseKey() {
		return new ShardingKey(tenantId(), scheduleId());
	}

	public String asString() {
		return asString(this);
	}

	@Override
	public String toString() {
		return "ShardingKey[elements=" + Arrays.toString(elements) + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		if (o == null || getClass() != o.getClass()) {return false;}
		ShardingKey that = (ShardingKey) o;
		return Objects.deepEquals(elements, that.elements);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(elements);
	}

}
