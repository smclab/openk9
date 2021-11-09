package io.openk9.entity.manager.service.graph;

import io.openk9.entity.manager.model.graph.EntityGraph;
import io.quarkus.arc.Unremovable;
import org.jboss.logging.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
@Unremovable
public class EntityGraphService {

	public EntityGraph insertEntity(
		String type, EntityGraph entityGraph) {

		try (Session session = driver.session()) {

			return session.writeTransaction(tx -> {
				Result result = tx.run(
					"CREATE (f:" + type +
					" {name: $name, id: $id, tenantId: $tenantId}) RETURN f",
					Values.parameters(
						"name", entityGraph.getName(),
						"id", entityGraph.getId(),
						"tenantId", entityGraph.getTenantId()
					)
				);

				Node node = result.single().get("f").asNode();

				return EntityGraph.from(type, node);

			});
		}

	}

	public void createRelationship(
		long graphId1, long graphId2, String relationName) {

		try (Session session = driver.session()) {

			session.writeTransaction(tx -> {
				Result result = tx.run(
					"MATCH (a), (b)\n" +
					"WHERE a.id = $id1 AND b.id = $id2\n" +
					"MERGE (a)-[r:" + relationName + "]->(b)\n" +
					"RETURN type(r)",
					Values.parameters(
						"id1", graphId1,
						"id2", graphId2,
						"relationName", relationName
					)
				);

				List<Record> list = result.list();

				if (_logger.isDebugEnabled()) {
					_logger.debug(list);
				}

				return list;

			});
		}

	}

	public EntityGraph searchByNameAndType(
		long tenantId, String name, String type) {

		try (Session session = driver.session()) {

			Result result = session.run(
				"MATCH (a:" + type + ") " +
				"WHERE a.name = $name AND a.tenantId = $tenantId " +
				"RETURN a",
				Values.parameters(
					"tenantId", tenantId,
					"name", name
				)
			);

			if (result.hasNext()) {
				Record next = result.next();
				Node node = next.get("a").asNode();
				String entityName = node.get("name").asString();
				long id = node.get("id").asLong();
				return EntityGraph.of(
					id, node.id(), tenantId, entityName, type);
			}
			else {
				return null;
			}
		}

	}

	@Inject
	Driver driver;

	@Inject
	Logger _logger;

}
