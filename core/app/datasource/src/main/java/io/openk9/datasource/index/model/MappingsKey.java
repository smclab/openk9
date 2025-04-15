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

package io.openk9.datasource.index.model;

import java.util.Objects;

import lombok.Getter;

public final class MappingsKey {

	@Getter
	private final String key;
	private final String hashKey;

	public static MappingsKey of(String key) {
		return new MappingsKey(key);
	}

	public static MappingsKey of(String key, String hashKey) {
		return new MappingsKey(key, hashKey);
	}

	public MappingsKey(String key) {
		this(key, key);
	}

	public MappingsKey(String key, String hashKey) {
		this.key = key;
		this.hashKey = hashKey;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.hashKey);
	}

	@Override
	public boolean equals(Object other) {

		if (other instanceof MappingsKey mappingsKey) {
			return Objects.equals(this.hashKey, mappingsKey.hashKey);
		}

		return false;
	}

	@Override
	public String toString() {
		return key;
	}

}