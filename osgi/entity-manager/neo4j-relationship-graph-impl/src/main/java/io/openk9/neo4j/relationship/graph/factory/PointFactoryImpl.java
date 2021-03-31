package io.openk9.neo4j.relationship.graph.factory;

import io.openk9.relationship.graph.api.factory.PointFactory;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = PointFactory.class
)
public class PointFactoryImpl implements PointFactory {
}
