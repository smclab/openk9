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

package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Tuple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SemanticType implements Iterable<Map<String, Object>> {

	protected SemanticType(List<Map<String, Object>> value) {
		this(value, Tuple.of());
	}

	protected SemanticType(List<Map<String, Object>> value, Tuple<Integer> tuple) {
		this.value = value;
		this.pos = tuple;
	}

	public Tuple<Integer> getPos() {
		return pos;
	}

	public List<Map<String, Object>> getValue() {
		return value;
	}

	public Map<String, Object> next() {
		return value.isEmpty() ? null : value.get(0);
	}

	@Override
	public Iterator<Map<String, Object>> iterator() {
		return value.iterator();
	}

	@Override
	public String toString() {

		if (value.size() == 1) {
			return next().toString() + "pos: " + pos;
		}
		else {
			return value + "pos: " + pos;
		}
	}

	private final List<Map<String, Object>> value;
	private final Tuple<Integer> pos;

	public static SemanticType of(SemanticType...semanticTypes) {

		List<Map<String, Object>> collect = Arrays
			.stream(semanticTypes)
			.map(SemanticType::getValue)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		return of(collect);

	}

	public static SemanticType of(Map<String, Object> value) {
		return new SemanticType(List.of(value));
	}

	public static SemanticType of(List<Map<String, Object>> value) {
		return new SemanticType(value);
	}

	@SafeVarargs
	public static SemanticType of(Map<String, Object>...values) {
		return new SemanticType(List.of(values));
	}

	public static SemanticType of(
		Tuple<Integer> pos, Map<String, Object> value) {
		return new SemanticType(List.of(value), pos);
	}

	public static SemanticType of(
		Tuple<Integer> pos, List<Map<String, Object>> value) {
		return new SemanticType(value, pos);
	}

	@SafeVarargs
	public static SemanticType of(
		Tuple<Integer> pos, Map<String, Object>...values) {
		return new SemanticType(List.of(values), pos);
	}

	public static SemanticType of() {
		return EMPTY_SEMANTIC_TYPE;
	}

	public static final SemanticType EMPTY_SEMANTIC_TYPE =
		new SemanticType(List.of()){};


}
