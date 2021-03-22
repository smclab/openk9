package io.openk9.neo4j.relationship.graph.factory;

import io.openk9.neo4j.relationship.graph.client.NodeWrapper;
import io.openk9.neo4j.relationship.graph.client.RelationshipWrapper;
import io.openk9.neo4j.relationship.graph.client.ValueWrapper;
import io.openk9.neo4j.relationship.graph.util.CollectionWrapper;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Value;
import io.openk9.relationship.graph.api.factory.EntityFactory;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.osgi.service.component.annotations.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component(
	immediate = true,
	service = EntityFactory.class
)
public class EntityFactoryImpl implements EntityFactory {

	@Override
	public Relationship createRelationship(
		long id, long start, long end, String type) {
		return createRelationship(
			id, start, end, type, Collections.emptyMap());
	}

	@Override
	public Relationship createRelationship(
		long id, long start, long end, String type,
		Map<String, Value> properties) {

		return new RelationshipWrapper(
			new InternalRelationship(
				id, start, end, type, CollectionWrapper.wrapMap(
					properties, value -> ((ValueWrapper)value).getDelegate())));
	}

	@Override
	public Node createNode(long id) {
		return new NodeWrapper(new InternalNode(id));
	}

	@Override
	public Node createNode(
		long id, Collection<String> labels, Map<String, Value> properties) {
		return new NodeWrapper(
			new InternalNode(
				id, labels, CollectionWrapper.wrapMap(
					properties, value -> ((ValueWrapper)value).getDelegate())
			)
		);
	}

}
