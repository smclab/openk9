package io.openk9.relationship.graph.api.client;

import io.openk9.relationship.graph.api.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Record {

	List<String> keys();

	List<Value> values();

	boolean containsKey(String key);

	int index(String key);

	Value get(String key);

	Value get(int index);

	int size();

	Map<String, Object> asMap();

	<T> Map<String, T> asMap(Function<Value, T> mapper);

	List<Pair<String, Value>> fields();

	Value get(String key, Value defaultValue);

	Object get(String key, Object defaultValue);

	Number get(String key, Number defaultValue);

	Entity get(String key, Entity defaultValue);

	Node get(String key, Node defaultValue);

	Path get(String key, Path defaultValue);

	Relationship get(String key, Relationship defaultValue);

	List<Object> get(String key, List<Object> defaultValue);

	<T> List<T> get(
		String key, List<T> defaultValue, Function<Value, T> mapFunc);

	Map<String, Object> get(
		String key, Map<String, Object> defaultValue);

	<T> Map<String, T> get(
		String key, Map<String, T> defaultValue, Function<Value, T> mapFunc);

	int get(String key, int defaultValue);

	long get(String key, long defaultValue);

	boolean get(String key, boolean defaultValue);

	String get(String key, String defaultValue);

	float get(String key, float defaultValue);

	double get(String key, double defaultValue);
}
