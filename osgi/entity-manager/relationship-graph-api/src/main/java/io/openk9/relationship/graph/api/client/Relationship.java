package io.openk9.relationship.graph.api.client;

import java.util.Map;
import java.util.function.Function;

public interface Relationship extends Entity {

	long startNodeId();

	long endNodeId();

	String type();

	boolean hasType(String relationshipType);

}
