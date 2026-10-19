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

package io.openk9.searcher.resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Obscures the source of a result the user is not authorized to read: only
 * the visible fields stay, with the identifying ones, and the source lists
 * the paths it lost.
 * <p>
 * Each visible field is the most specific field holding a value, so the value
 * at its path is kept whole: an I18N field with all its variants, an object
 * without mapped sub-fields with all its keys. A key below its path, written
 * with dots such as {@code "summary.base"}, is part of that value too. A path
 * that only leads to visible fields is walked into, element by element for an
 * array of objects.
 */
final class ResultObscurer {

	/**
	 * The key of the source listing the paths left out; its presence marks
	 * the result as obscured, even when the list is empty.
	 */
	static final String OBSCURED_FIELDS = "obscuredFields";

	/**
	 * The root fields every result keeps: the frontend picks the renderer
	 * from documentTypes.
	 */
	private static final Set<String> KEPT_FIELDS = Set.of("id", "documentTypes");

	private ResultObscurer() {}

	/**
	 * Builds the obscured copy of a source.
	 *
	 * @param source        the source of the result, left untouched
	 * @param visibleFields the paths of the fields left visible
	 * @return the visible part of the source, with the sorted list of the
	 *         paths left out under {@link #OBSCURED_FIELDS}
	 */
	static Map<String, Object> obscureSource(
		Map<String, Object> source, Set<String> visibleFields) {

		SortedSet<String> obscuredFields = new TreeSet<>();

		Map<String, Object> obscured =
			filterObject(source, "", visibleFields, obscuredFields);

		obscured.put(OBSCURED_FIELDS, List.copyOf(obscuredFields));

		return obscured;
	}

	/**
	 * Tells whether a path belongs to a visible field: the field itself, or
	 * one of its variants and sub-fields, such as {@code web.title.i18n.it}
	 * or {@code web.title.keyword} for {@code web.title}.
	 *
	 * @param path          the path of a field, as a highlight names it
	 * @param visibleFields the paths of the fields left visible
	 * @return {@code true} if the path may be shown
	 */
	static boolean isVisible(String path, Set<String> visibleFields) {
		if (visibleFields.contains(path)) {
			return true;
		}

		for (String visibleField : visibleFields) {
			if (path.startsWith(visibleField + ".")) {
				return true;
			}
		}

		return false;
	}

	private static Map<String, Object> filterObject(
		Map<String, Object> object, String prefix, Set<String> visibleFields,
		SortedSet<String> obscuredFields) {

		Map<String, Object> filtered = new LinkedHashMap<>();

		for (Map.Entry<String, Object> entry : object.entrySet()) {
			String key = entry.getKey();
			String path = prefix.isEmpty() ? key : prefix + "." + key;

			if ((prefix.isEmpty() && KEPT_FIELDS.contains(key))
				|| isVisible(path, visibleFields)) {

				filtered.put(key, entry.getValue());
			}
			else if (leadsToVisibleField(path, visibleFields)) {
				Object kept = filterValue(
					entry.getValue(), path, visibleFields, obscuredFields);

				if (kept != null) {
					filtered.put(key, kept);
				}
			}
			else {
				obscuredFields.add(path);
			}
		}

		return filtered;
	}

	/**
	 * Filters the value at a path that only leads to visible fields.
	 *
	 * @return the visible part, or {@code null} when nothing visible is left
	 */
	@SuppressWarnings("unchecked")
	private static Object filterValue(
		Object value, String path, Set<String> visibleFields,
		SortedSet<String> obscuredFields) {

		if (value instanceof Map<?, ?> map) {
			Map<String, Object> kept = filterObject(
				(Map<String, Object>) map, path, visibleFields, obscuredFields);

			return kept.isEmpty() ? null : kept;
		}

		if (value instanceof List<?> list) {
			List<Object> kept = new ArrayList<>();

			for (Object item : list) {
				Object keptItem =
					filterValue(item, path, visibleFields, obscuredFields);

				if (keptItem != null) {
					kept.add(keptItem);
				}
			}

			return kept.isEmpty() ? null : kept;
		}

		// a value where the mapping expects an object: nothing visible in it
		obscuredFields.add(path);

		return null;
	}

	private static boolean leadsToVisibleField(
		String path, Set<String> visibleFields) {

		String prefix = path + ".";

		for (String visibleField : visibleFields) {
			if (visibleField.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

}
