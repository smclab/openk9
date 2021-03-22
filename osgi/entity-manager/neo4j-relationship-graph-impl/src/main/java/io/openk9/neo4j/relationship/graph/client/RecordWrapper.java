package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Entity;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Record;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Value;
import io.openk9.relationship.graph.api.util.Pair;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecordWrapper implements Record {

	@Override
	public List<String> keys() {
		return this.delegate.keys();
	}

	@Override
	public List<Value> values() {
		return this.delegate.values()
			.stream()
			.map(ValueWrapper::new)
			.collect(Collectors.toList());
	}

	@Override
	public boolean containsKey(String key) {
		return this.delegate.containsKey(key);
	}

	@Override
	public int index(String key) {
		return this.delegate.index(key);
	}

	@Override
	public Value get(String key) {
		return new ValueWrapper(this.delegate.get(key));
	}

	@Override
	public Value get(int index) {
		return new ValueWrapper(this.delegate.get(index));
	}

	@Override
	public int size() {
		return this.delegate.size();
	}

	@Override
	public Map<String, Object> asMap() {
		return this.delegate.asMap();
	}

	@Override
	public <T> Map<String, T> asMap(Function<Value, T> mapper) {
		return this.delegate.asMap(value -> mapper.apply(new ValueWrapper(value)));
	}

	@Override
	public List<Pair<String, Value>> fields() {
		return this.delegate.fields()
			.stream()
			.map(e -> Pair.of(e.key(), (Value)new ValueWrapper(e.value())))
			.collect(Collectors.toList());
	}

	@Override
	public Value get(String key, Value defaultValue) {
		return new ValueWrapper(
			this.delegate.get(key, ((ValueWrapper)defaultValue).delegate));
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public Number get(String key, Number defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public Entity get(String key, Entity defaultValue) {
		return new EntityWrapper(
			this.delegate.get(key, ((EntityWrapper)defaultValue).delegate));
	}

	@Override
	public Node get(String key, Node defaultValue) {
		return new NodeWrapper(
			this.delegate.get(key, ((NodeWrapper)defaultValue).getDelegate()));
	}

	@Override
	public Path get(String key, Path defaultValue) {
		return new PathWrapper(
			this.delegate.get(key, ((PathWrapper)defaultValue).delegate));
	}

	@Override
	public Relationship get(String key, Relationship defaultValue) {
		return new RelationshipWrapper(
			this.delegate.get(
				key, ((RelationshipWrapper)defaultValue).getDelegate()));
	}

	@Override
	public List<Object> get(String key, List<Object> defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public <T> List<T> get(
		String key, List<T> defaultValue, Function<Value, T> mapFunc) {
		return this.delegate.get(
			key, defaultValue,
			value -> mapFunc.apply(new ValueWrapper(value)));
	}

	@Override
	public Map<String, Object> get(
		String key, Map<String, Object> defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public <T> Map<String, T> get(
		String key, Map<String, T> defaultValue, Function<Value, T> mapFunc) {
		return this.delegate.get(
			key, defaultValue,
			value -> mapFunc.apply(new ValueWrapper(value)));
	}

	@Override
	public int get(String key, int defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public long get(String key, long defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public boolean get(String key, boolean defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public String get(String key, String defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public float get(String key, float defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public double get(String key, double defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.Record getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.Record delegate;

}
