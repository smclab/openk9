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

package io.openk9.search.query.internal.query.parser.util;

import io.openk9.common.api.constant.Strings;
import io.openk9.search.api.query.parser.Tuple;

public class Utils {

	public static String[] split(String s) {
		return s.split("\\s+");
	}

	public static String removeBlankSpaces(String s) {
		if (s == null || s.isBlank()) {
			return Strings.BLANK;
		}
		return s.replaceAll("\\s+", Strings.SPACE);
	}

	public static <T> Tuple<T> toTuple(T[] rhs) {
		if (rhs.length == 0) {
			return Tuple.of();
		}
		return Tuple.of(rhs);
	}

	public static String[] toArray(Tuple<String> tuple) {
		return tuple.toArray();
	}

}
