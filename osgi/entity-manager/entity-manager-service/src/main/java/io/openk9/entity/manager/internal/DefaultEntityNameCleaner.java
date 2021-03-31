package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.EntityNameCleaner;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Component;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class DefaultEntityNameCleaner implements EntityNameCleaner {

	@Override
	public String getEntityType() {
		return "default";
	}

	@Override
	public Statement cleanEntityName(long tenantId, String entityName) {

		Node entity = Cypher.anyNode("entity");

		Property entityNameProperty = entity.property("entityName");
		Property tenantIdProperty = entity.property("tenantId");

		return Cypher
			.match(entity)
			.where(
				tenantIdProperty
					.eq(literalOf(tenantId))
					.and(
						entityNameProperty
							.eq(literalOf(entityName)
						)
					)
			)
			.returning(entity)
			.build();

	}

}
