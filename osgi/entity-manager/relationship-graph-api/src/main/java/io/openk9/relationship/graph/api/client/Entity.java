package io.openk9.relationship.graph.api.client;

import java.util.Map;
import java.util.function.Function;

public interface Entity {
	long id();

	Iterable<String> keys();

	boolean containsKey(String key);

	Value get(String key);

	int size();

	Iterable<Value> values();

	<T> Iterable<T> values(Function<Value, T> mapFunction);

	Map<String, Object> asMap();

	<T> Map<String, T> asMap(Function<Value, T> mapFunction);
}
