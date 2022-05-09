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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class SemanticTypes implements Iterable<SemanticType> {

	public SemanticTypes(
		List<SemanticType> semanticTypes) {
		Objects.requireNonNull(semanticTypes, "semanticTypes is null");
		_semanticTypes = semanticTypes;
	}

	public SemanticType get(int index) {
		return _semanticTypes.get(index);
	}

	public List<SemanticType> getSemanticTypes() {
		return _semanticTypes;
	}

	public Stream<SemanticType> stream() {
		return _semanticTypes.stream();
	}

	public int size() {
		return _semanticTypes.size();
	}

	public boolean isEmpty() {
		return _semanticTypes.isEmpty();
	}

	@Override
	public Iterator<SemanticType> iterator() {
		return _semanticTypes.iterator();
	}

	@Override
	public String toString() {

		if (_semanticTypes.size() == 1) {
			return "(" + _semanticTypes.get(0) + ")";
		}
		else {
			return "(" + _semanticTypes + ')';
		}

	}

	private final List<SemanticType> _semanticTypes;

	public static SemanticTypes of() {
		return EMPTY_SEMANTIC_TYPES;
	}

	public static SemanticTypes of(SemanticType semanticType) {
		return new SemanticTypes(List.of(semanticType));
	}

	public static SemanticTypes of(SemanticType...semanticType) {
		return new SemanticTypes(List.of(semanticType));
	}

	public static SemanticTypes of(List<SemanticType> values) {
		return new SemanticTypes(values);
	}

	public static SemanticTypes merge(SemanticTypes semanticTypes) {

		if (semanticTypes.size() == 1) {
			return semanticTypes;
		}

		int startPos = -1;
		int endPos = -1;

		List<Map<String, Object>> newSemantics = new ArrayList<>();

		for (SemanticType semanticType : semanticTypes) {
			Tuple<Integer> pos = semanticType.getPos();
			if (!pos.isEmpty()) {

				Integer start = pos.get(0);

				if (startPos == -1 || startPos > start) {
					startPos = start;
				}

				Integer end = pos.get(1);

				if (endPos == -1 || endPos < end) {
					endPos = end;
				}

			}

			newSemantics.addAll(semanticType.getValue());
		}

		return of(SemanticType.of(Tuple.of(startPos, endPos), newSemantics));
	}

	public static SemanticTypes EMPTY_SEMANTIC_TYPES =
		new SemanticTypes(List.of());

	public boolean isNotEmpty() {
		return !isEmpty();
	}
}
