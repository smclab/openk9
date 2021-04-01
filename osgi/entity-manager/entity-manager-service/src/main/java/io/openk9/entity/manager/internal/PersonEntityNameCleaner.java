package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.EntityNameCleaner;
import io.vavr.collection.List;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.Conditions;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Optional;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class PersonEntityNameCleaner implements EntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	@Override
	public Statement cleanEntityName(long tenantId, String entityName) {

		Node entity = Cypher.node(getEntityType());

		Property entityNameProperty = entity.property("entityName");
		Property tenantIdProperty = entity.property("tenantId");

		String[] entityNames = entityName.split(" ");

		Optional<Condition> entityNameCondition =
			Arrays
				.stream(entityNames)
				.map(Cypher::literalOf)
				.map(entityNameProperty::contains)
				.reduce(Condition::and);

		return Cypher
			.match(entity)
			.where(
				tenantIdProperty
					.eq(literalOf(tenantId))
					.and(entityNameCondition.orElseGet(Conditions::isFalse))
			)
			.returning(entity)
			.build();

	}

}
