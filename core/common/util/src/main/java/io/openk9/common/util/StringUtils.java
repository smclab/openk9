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

import lombok.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	private StringUtils() {}


	/**
	 * Adds a suffix to a {@link String}.
	 * This method concatenates two {@link String} with a '-' character between them.
	 *
	 * @param value  cannot be null, the string where to add the suffix.
	 * @param suffix the optional suffix that need to be added to the value.
	 * @return the source string without any characters than alphanumeric
	 */
	public static String withSuffix(@NonNull String value, String suffix) {
		if (suffix == null) {
			return value;
		}

		if (suffix.isBlank()) {
			return value;
		}

		return String.format("%s-%s", value, suffix);
	}

	/**
	 * This method retains only alphanumeric characters in a {@link String}.
	 *
	 * @param source the string that has to be processed
	 * @return the source string without any characters than alphanumeric
	 */
	public static String retainsAlnum(String source) {

		final Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		final Matcher matcher = pattern.matcher(source);

		return matcher.replaceAll("");
	}

}
