package io.openk9.relationship.graph.api.client;

public interface Type {
	String name();

	boolean isTypeOf(Value value);
}
