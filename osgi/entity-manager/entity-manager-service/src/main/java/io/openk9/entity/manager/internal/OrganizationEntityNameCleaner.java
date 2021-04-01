package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.EntityNameCleaner;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.Conditions;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import java.util.Arrays;
import java.util.Optional;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class OrganizationEntityNameCleaner implements EntityNameCleaner {

	@interface Config {
		String[] stopWords() default {"spa", "s.p.a.", "srl", "s.r.l."};
	}

	@Activate
	void activate(Config config) {
		_stopWords = config.stopWords();
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_stopWords = null;
	}

	@Override
	public String getEntityType() {
		return "organization";
	}

	@Override
	public Statement cleanEntityName(long tenantId, String entityName) {

		Node entity = Cypher.node(getEntityType());

		Property entityNameProperty = entity.property("entityName");
		Property tenantIdProperty = entity.property("tenantId");

		for (String stopWord : _stopWords) {
			entityName = entityName.replaceAll(stopWord, "");
		}

		String[] entityNames = entityName.split(" ");

		Optional<Condition> entityNameCondition =
			Arrays
				.stream(entityNames)
				.map(Cypher::literalOf)
				.map(entityNameProperty::contains)
				.reduce(Condition::or);

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

	private String[] _stopWords;

}
