package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.util.IteratorWrapper;
import io.openk9.relationship.graph.api.client.Entity;
import io.openk9.relationship.graph.api.client.Value;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class EntityWrapper implements Entity {

	@Override
	public long id() {
		return this.delegate.id();
	}

	@Override
	public Iterable<String> keys() {
		return this.delegate.keys();
	}

	@Override
	public boolean containsKey(String key) {
		return this.delegate.containsKey(key);
	}

	@Override
	public Value get(String key) {
		return new ValueWrapper(this.delegate.get(key));
	}

	@Override
	public int size() {
		return this.delegate.size();
	}

	@Override
	public Iterable<Value> values() {

		Iterable<org.neo4j.driver.Value> values = this.delegate.values();

		return () -> IteratorWrapper.of(
			values.iterator(), ValueWrapper::new);
	}

	@Override
	public <T> Iterable<T> values(Function<Value, T> mapFunction) {
		return this.delegate.values(
			value -> mapFunction.apply(new ValueWrapper(value)));
	}

	@Override
	public Map<String, Object> asMap() {
		return this.delegate.asMap();
	}

	@Override
	public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
		return this.delegate.asMap(
			value -> mapFunction.apply(new ValueWrapper(value)));
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.types.Entity getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.types.Entity delegate;

}
