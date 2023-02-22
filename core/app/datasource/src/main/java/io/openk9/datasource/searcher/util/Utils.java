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

package io.openk9.datasource.searcher.util;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Utils {

	public static Stream<DocTypeField> getDocTypeFieldsFrom(Bucket bucket) {
		return getDocTypeFieldsFrom(bucket.getDatasources());
	}

	public static Stream<DocTypeField> getDocTypeFieldsFrom(
		Collection<Datasource> datasources) {
		return datasources.stream()
			.flatMap(d -> d == null
				? Stream.empty()
				: d.getDataIndex() == null
					? Stream.empty()
					: d.getDataIndex().getDocTypes().stream())
			.flatMap(dts -> dts == null
				? Stream.empty()
				: dts.getDocTypeFields().stream())
			.flatMap(dtf -> dtf == null
				? Stream.empty()
				: dtf.getDocTypeFieldAndChildren().stream());
	}

	public static Map<Integer, ? extends TokenIndex> toTokenIndexMap(
		String searchText) {

		if (searchText == null || searchText.isBlank()) {
			return Map.of();
		}

		Map<Integer, StringBuilderTokenIndex> map = new IdentityHashMap<>();

		final int length = searchText.length();
		final int lastIndex = length - 1;

		boolean quote = false;

		for (int i = 0, count = 0; i < length; i++) {

			char c = searchText.charAt(i);

			if (isQuote(c)) {
				quote = !quote;
			}

			boolean whitespace = Character.isWhitespace(c);

			if (whitespace && !quote) {

				StringBuilderTokenIndex stringBuilderTokenIndex =
					map.get(count);

				if (stringBuilderTokenIndex == null) {
					continue;
				}

				stringBuilderTokenIndex.setEndIndex(i);

				count++;


			}
			else {

				final int index = i;

				StringBuilderTokenIndex stringBuilderIndex =
					map.computeIfAbsent(
						count, pos -> new StringBuilderTokenIndex(index, pos));

				stringBuilderIndex.append(c);

				if (lastIndex == i)  {
					stringBuilderIndex.setEndIndex(i + 1);
				}

			}
		}

		return Collections.unmodifiableMap(map);

	}
	public static String[] split(String searchText) {

		if ((_countQuote(searchText) % 2) != 0) {
			return searchText.strip().split("\\s+");
		}

		boolean quoted = false;
		int nStr = 0;

		Map<Integer, StringBuilder> map = new HashMap<>();

		for (int i = 0; i < searchText.length(); i++) {
			char c = searchText.charAt(i);
			if (isQuote(c)) {
				quoted = !quoted;
			}
			if (quoted) {
				map.computeIfAbsent(nStr, pos -> new StringBuilder()).append(c);
			}
			else if (Character.isWhitespace(c)) {
				nStr++;
			}
			else {
				map.computeIfAbsent(nStr, pos -> new StringBuilder()).append(c);
			}
		}
		return map
			.values()
			.stream().map(StringBuilder::toString)
			.toArray(String[]::new);
	}

	public static final List<Character> QUOTE_CHARACTER_LIST =
		List.of('"', '\'', '`');

	public static boolean equalsIgnoreSpaces(String str1, String str2) {
		return Objects.equals(
			removeBlankSpaces(str1), removeBlankSpaces(str2));
	}

	public static String removeBlankSpaces(String s) {
		if (s == null || s.isBlank()) {
			return "";
		}
		return s.replaceAll("\\s+", "");
	}

	public static boolean startWithQuote(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		return QUOTE_CHARACTER_LIST.contains(s.charAt(0));
	}

	public static boolean endWithQuote(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		return QUOTE_CHARACTER_LIST.contains(s.charAt(s.length() - 1));
	}

	public static boolean inQuote(String s) {
		if (s == null || s.isBlank() || s.length() < 3) {
			return false;
		}
		return QUOTE_CHARACTER_LIST.contains(s.charAt(0)) &&
			   QUOTE_CHARACTER_LIST.contains(s.charAt(s.length() - 1));
	}

	public static String removeQuote(String s) {
		if (s == null || s.isBlank() || s.length() < 3) {
			return s;
		}

		for (Character c : QUOTE_CHARACTER_LIST) {
			s = s.replaceAll("^" + c + "|" + c + "$", "");
		}

		return s;

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


	public static boolean isQuote(char c) {
		return QUOTE_CHARACTER_LIST.contains(c);
	}

	private static int _countQuote(String searchText) {
		int countQuote = 0;

		for (int i = 0; i < searchText.length(); i++) {
			char c = searchText.charAt(i);
			if (isQuote(c)) {
				countQuote++;
			}
		}
		return countQuote;
	}

	public static int countWords(String value) {

		if (value == null || value.isBlank()) {
			return 0;
		}

		int c = 1;

		for (int i = 0; i < value.length(); i++) {
			if (Character.isWhitespace(value.charAt(i))) {
				c++;
			}
		}

		return c;

	}

	private static class StringBuilderTokenIndex
		implements TokenIndex, Appendable {

		public StringBuilderTokenIndex(int startIndex, int pos) {
			this.startIndex = startIndex;
			this.sb = new StringBuilder();
			this.pos = pos;
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
		public int getPos() {
			return pos;
		}

		@Override
		public StringBuilderTokenIndex append(CharSequence csq) {
			sb.append(csq);
			return this;
		}

		@Override
		public StringBuilderTokenIndex append(
			CharSequence csq, int start, int end) {
			sb.append(csq, start, end);
			return this;
		}

		@Override
		public StringBuilderTokenIndex append(char c) {
			sb.append(c);
			return this;
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
				   ", pos=" + pos +
				   '}';
		}

		private final int startIndex;
		private int endIndex;
		private final StringBuilder sb;
		private String result;
		private final int pos;

	}

	public interface TokenIndex {
		int getStartIndex();
		int getEndIndex();
		String getToken();
		int getPos();

	}

}
