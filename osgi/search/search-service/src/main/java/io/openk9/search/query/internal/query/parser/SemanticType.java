package io.openk9.search.query.internal.query.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SemanticType implements Iterable<Map<String, Object>> {

	protected SemanticType(List<Map<String, Object>> value) {
		this.value = value;
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
			return next().toString();
		}
		else {
			return value.toString();
		}
	}

	private final List<Map<String, Object>> value;

	public static SemanticType of(SemanticType...semanticTypes) {

		List<Map<String, Object>> collect = Arrays
			.stream(semanticTypes)
			.map(SemanticType::getValue)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		return of(collect);

	}

	public static SemanticType of(Map<String, Object> value) {
		return new SingleSemanticType(value);
	}

	public static SemanticType of(List<Map<String, Object>> value) {
		return new ListSemanticType(value);
	}

	public static SemanticType of(Map<String, Object>...values) {
		return new ListSemanticType(List.of(values));
	}

	public static SemanticType of() {
		return EMPTY_SEMANTIC_TYPE;
	}

	public static final SemanticType EMPTY_SEMANTIC_TYPE =
		new SemanticType(List.of()){};


	public static class ListSemanticType extends SemanticType {
		public ListSemanticType(List<Map<String, Object>> value) {
			super(value);
		}
	}

	public static class SingleSemanticType extends SemanticType {
		public SingleSemanticType(Map<String, Object> value) {
			super(List.of(value));
		}
	}

}
