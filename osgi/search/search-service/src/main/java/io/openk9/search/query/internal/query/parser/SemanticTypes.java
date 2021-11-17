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

		List<Map<String, Object>> collect =
			new ArrayList<>(semanticTypes.size());

		Tuple<Integer> pos = Tuple.of();

		for (SemanticType semanticType : semanticTypes) {
			collect.addAll(semanticType.getValue());
			if (!semanticType.getPos().isEmpty()) {
				pos = semanticType.getPos();
			}
		}

		return of(SemanticType.of(pos, collect));
	}

	public static SemanticTypes EMPTY_SEMANTIC_TYPES =
		new SemanticTypes(List.of());

}
