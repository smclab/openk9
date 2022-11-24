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

package io.openk9.datasource.web;

import io.smallrye.mutiny.tuples.Tuple2;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceUtil {

	public static Tuple2<String, Map<String, Object>> getFilterQuery(
		Map<String, Object> maps) {

		StringBuilder sb = new StringBuilder();

		Map<String, Object> collect =
			maps
				.entrySet()
				.stream()
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Function<String, String> condition =
			key -> key.endsWith("_not")
				? " != "
				: " = ";

		Iterator<String> iterator = collect.keySet().iterator();

		if (iterator.hasNext()) {
			String key = iterator.next();
			sb.append(key).append(condition.apply(key)).append(':').append(key);
		}

		while (iterator.hasNext()) {
			String key = iterator.next();
			sb
				.append(" and ")
				.append(key)
				.append(condition.apply(key))
				.append(':')
				.append(key);
		}

		return Tuple2.of(sb.toString(), collect);

	}

}
