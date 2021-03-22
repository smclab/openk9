package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Segment;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.types.Path;

@RequiredArgsConstructor(staticName = "of")
public class SegmentWrapper implements Segment {

	@Override
	public Relationship relationship() {
		return new RelationshipWrapper(this.delegate.relationship());
	}

	@Override
	public Node start() {
		return new NodeWrapper(this.delegate.start());
	}

	@Override
	public Node end() {
		return new NodeWrapper(this.delegate.end());
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public Path.Segment getDelegate() {
		return delegate;
	}

	final Path.Segment delegate;

}
