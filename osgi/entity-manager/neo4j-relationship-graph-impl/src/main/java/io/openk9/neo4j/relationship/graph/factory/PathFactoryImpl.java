package io.openk9.neo4j.relationship.graph.factory;

import io.openk9.neo4j.relationship.graph.client.EntityWrapper;
import io.openk9.neo4j.relationship.graph.client.NodeWrapper;
import io.openk9.neo4j.relationship.graph.client.PathWrapper;
import io.openk9.neo4j.relationship.graph.client.RelationshipWrapper;
import io.openk9.neo4j.relationship.graph.client.SegmentWrapper;
import io.openk9.relationship.graph.api.client.Entity;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Segment;
import io.openk9.relationship.graph.api.factory.PathFactory;
import org.neo4j.driver.internal.InternalPath;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = PathFactory.class
)
public class PathFactoryImpl implements PathFactory {

	@Override
	public Path createInternalPath(List<Entity> alternatingNodeAndRel) {

		List<org.neo4j.driver.types.Entity> deWrapAlternatingNodeAndRel =
			alternatingNodeAndRel
				.stream()
				.map(entity -> ((EntityWrapper)entity).getDelegate())
				.collect(Collectors.toList());

		return new PathWrapper(new InternalPath(deWrapAlternatingNodeAndRel));
	}

	@Override
	public Path createInternalPath(
		Entity... alternatingNodeAndRel) {

		org.neo4j.driver.types.Entity[] entities =
			Arrays.stream(alternatingNodeAndRel)
				.map(entity -> ((EntityWrapper) entity).getDelegate())
				.toArray(org.neo4j.driver.types.Entity[]::new);

		return new PathWrapper(new InternalPath(entities));
	}

	@Override
	public Path createInternalPath(
		List<Segment> segments, List<Node> nodes,
		List<Relationship> relationships) {

		return new PathWrapper(
			new InternalPath(
				segments
					.stream()
					.map(segment -> ((SegmentWrapper)segment).getDelegate())
					.collect(Collectors.toList()),
				nodes
					.stream()
					.map(node -> ((NodeWrapper)node).getDelegate())
					.collect(Collectors.toList()),
				relationships
					.stream()
					.map(relationship ->((RelationshipWrapper)relationship).getDelegate())
					.collect(Collectors.toList())
				));

		//TODO ble

	}

}
