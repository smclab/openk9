package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Node;

public class NodeWrapper extends EntityWrapper implements Node {

	public NodeWrapper(org.neo4j.driver.types.Node delegate) {
		super(delegate);
	}

	@Override
	public Iterable<String> labels() {
		return getDelegate().labels();
	}

	@Override
	public boolean hasLabel(String label) {
		return getDelegate().hasLabel(label);
	}

	@Override
	public org.neo4j.driver.types.Node getDelegate() {
		return (org.neo4j.driver.types.Node)delegate;
	}

}
