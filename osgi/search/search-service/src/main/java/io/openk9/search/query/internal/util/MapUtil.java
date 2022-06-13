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

package io.openk9.search.query.internal.util;

import java.util.Map;
import java.util.Set;

public class MapUtil {

	public static void deepAbbreviateString(
		Map<String, Object> map, int maxLength) {

		Set<Map.Entry<String, Object>> entries = map.entrySet();

		for (Map.Entry<String, Object> entry : entries) {
			Object value = entry.getValue();

			if (value instanceof String) {
				String stringValue = (String) value;

				if (stringValue.length() > maxLength) {
					entry.setValue(_abbreviate(stringValue, maxLength));
				}

			}
			else if (value instanceof Map) {
				Map<String, Object> stringMap =(Map<String, Object>) value;

				deepAbbreviateString(stringMap, maxLength);

			}

		}

	}

	private static String _abbreviate(String stringValue, int maxLength) {
		return stringValue.substring(0, maxLength) + "...";
	}

}
