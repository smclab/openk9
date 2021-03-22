package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Relationship;

public class RelationshipWrapper extends EntityWrapper implements Relationship {

	public RelationshipWrapper(
		org.neo4j.driver.types.Relationship relationship) {
		super(relationship);
	}

	@Override
	public long startNodeId() {
		return getDelegate().startNodeId();
	}

	@Override
	public long endNodeId() {
		return getDelegate().endNodeId();
	}

	@Override
	public String type() {
		return getDelegate().type();
	}

	@Override
	public boolean hasType(String relationshipType) {
		return getDelegate().hasType(relationshipType);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public org.neo4j.driver.types.Relationship getDelegate() {
		return (org.neo4j.driver.types.Relationship)delegate;
	}

}
