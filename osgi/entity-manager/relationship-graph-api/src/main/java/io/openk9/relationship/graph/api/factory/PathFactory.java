package io.openk9.relationship.graph.api.factory;

import io.openk9.relationship.graph.api.client.Entity;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Segment;

import java.util.List;

public interface PathFactory {

	Path createInternalPath(List<Entity> alternatingNodeAndRel);
	Path createInternalPath(Entity...alternatingNodeAndRel);
	Path createInternalPath(
		List<Segment> segments,
		List<Node> nodes, List<Relationship> relationships);

}
