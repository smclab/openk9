package io.openk9.relationship.graph.api.factory;

import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Value;

import java.util.Collection;
import java.util.Map;

public interface EntityFactory {

	Relationship createRelationship(
		long id, long start, long end, String type);

	Relationship createRelationship(
		long id, long start, long end, String type,
		Map<String, Value> properties);

	Node createNode(long id);

	Node createNode(
		long id, Collection<String> labels, Map<String, Value> properties);

}
