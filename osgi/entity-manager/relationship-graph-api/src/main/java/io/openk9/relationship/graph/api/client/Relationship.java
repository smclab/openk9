package io.openk9.relationship.graph.api.client;

public interface Relationship extends Entity {

	long startNodeId();

	long endNodeId();

	String type();

	boolean hasType(String relationshipType);

}
