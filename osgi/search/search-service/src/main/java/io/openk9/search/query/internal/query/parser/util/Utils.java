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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Utils {

	public static Map<Integer, ? extends TokenIndex> toTokenIndexMap(
		String searchText) {

		Map<Integer, StringBuilderTokenIndex> map = new HashMap<>();

		for (int i = 0, count = 0;  i < searchText.length(); i++) {

			char c = searchText.charAt(i);

			if (Character.isWhitespace(c)) {

				if (!map.containsKey(count)) {
					continue;
				}

				StringBuilderTokenIndex stringBuilderIndex = map.get(count);

				if (stringBuilderIndex != null) {
					stringBuilderIndex.setEndIndex(i);
				}

				count++;
			}
			else {

				final int index = i;

				StringBuilderTokenIndex stringBuilderIndex =
					map.computeIfAbsent(
						count, integer -> new StringBuilderTokenIndex(index));

				stringBuilderIndex.append(c);

			}
		}

		return map;

	}
	public static String[] split(String s) {
		return s.strip().split("\\s+");
	}

	public static boolean equalsIgnoreSpaces(String str1, String str2) {
		return Objects.equals(
			removeBlankSpaces(str1), removeBlankSpaces(str2));
	}

	public static String removeBlankSpaces(String s) {
		if (s == null || s.isBlank()) {
			return Strings.BLANK;
		}
		return s.replaceAll("\\s+", Strings.BLANK);
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

	private static class StringBuilderTokenIndex
		implements TokenIndex, Appendable {
		public StringBuilderTokenIndex(int startIndex) {
			this.startIndex = startIndex;
			this.sb = new StringBuilder();
		}
		@Override
		public int getStartIndex() {
			return startIndex;
		}

		@Override
		public int getEndIndex() {
			return endIndex;
		}

		@Override
		public String getToken() {
			return result == null ? (result = sb.toString()) : result;
		}

		@Override
		public Appendable append(CharSequence csq) {
			return sb.append(csq);
		}

		@Override
		public Appendable append(CharSequence csq, int start, int end) {
			return sb.append(csq, start, end);
		}

		@Override
		public Appendable append(char c) {
			return sb.append(c);
		}

		public void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}

		@Override
		public String toString() {
			return "TokenIndex{" +
				   "startIndex=" + startIndex +
				   ", endIndex=" + endIndex +
				   ", token=" + getToken() +
				   '}';
		}

		private final int startIndex;
		private int endIndex;
		private final StringBuilder sb;

		private String result;

	}

	public interface TokenIndex {
		int getStartIndex();
		int getEndIndex();
		String getToken();
	}

}
