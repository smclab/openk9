package io.openk9.relationship.graph.api.client;

public interface Node extends Entity {
	Iterable<String> labels();

	boolean hasLabel(String label);
}
