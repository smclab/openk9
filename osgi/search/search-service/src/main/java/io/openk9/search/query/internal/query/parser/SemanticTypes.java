package io.openk9.search.query.internal.query.parser;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

		List<SemanticType> list =
			semanticTypes
				.stream()
				.filter(maps -> maps.getValue().isEmpty())
				.collect(Collectors.toList());

		return of(list);
	}

	public static SemanticTypes EMPTY_SEMANTIC_TYPES =
		new SemanticTypes(List.of());

}
