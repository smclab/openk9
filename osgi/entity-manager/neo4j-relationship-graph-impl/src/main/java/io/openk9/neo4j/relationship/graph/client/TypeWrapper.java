package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Type;
import io.openk9.relationship.graph.api.client.Value;

public class TypeWrapper implements Type {

	public TypeWrapper(org.neo4j.driver.types.Type type) {
		this.delegate = type;
	}

	@Override
	public String name() {
		return this.delegate.name();
	}

	@Override
	public boolean isTypeOf(Value value) {
		return this.delegate.isTypeOf(((ValueWrapper)value).delegate);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.types.Type getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.types.Type delegate;

}
