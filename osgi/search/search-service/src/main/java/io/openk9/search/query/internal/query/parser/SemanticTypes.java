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
