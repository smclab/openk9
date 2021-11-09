package io.openk9.entity.manager.service.graph;

import io.openk9.entity.manager.model.graph.EntityGraph;
import io.quarkus.arc.Unremovable;
import org.jboss.logging.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.types.Node;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Unremovable
public class EntityGraphService {

	public CompletionStage<EntityGraph> insertEntity(
		String type, EntityGraph entityGraph) {

		AsyncSession session = driver.asyncSession();

		return session
			.writeTransactionAsync(tx -> tx
				.runAsync(
					"CREATE (f:" + type + " {name: $name, id: $id, tenantId: $tenantId}) RETURN f",
					Values.parameters(
						"name", entityGraph.getName(),
						"id", entityGraph.getId(),
						"tenantId", entityGraph.getTenantId()
					)
				)
				.thenCompose(ResultCursor::singleAsync)
			)
			.thenApply(record -> EntityGraph.from(type, record.get("f").asNode()))
			.thenCompose(
				eg ->
					session.closeAsync().thenApply(signal -> eg));

	}

	public CompletionStage<Void> createRelationship(
		long graphId1, long graphId2, String relationName) {

		AsyncSession session = driver.asyncSession();

		return session
			.writeTransactionAsync(tx -> tx
				.runAsync(
					"MATCH (a), (b)\n" +
					"WHERE a.id = " + graphId1 + " AND b.id = " + graphId2 + "\n" +
					"MERGE (a)-[r:" + relationName + "]->(b)\n" +
					"RETURN type(r)"
				)
				.thenCompose(ResultCursor::singleAsync)
			)
			.thenAccept(record -> {
				if (_logger.isDebugEnabled()) {
					_logger.debug(record);
				}
			})
			.thenCompose(
				nothing ->
					session.closeAsync()
						.thenApply(signal -> nothing)
			);

	}

	public EntityGraph searchByNameAndType(
		long tenantId, String name, String type) {

		Session session = driver.session();

		Result result = session.run(
			"MATCH (a:" + type + ") " +
			"WHERE a.name = '" + name + "' AND a.tenantId = " + tenantId + " " +
			"RETURN a"
		);

		if (result.hasNext()) {
			Record next = result.next();
			Node node = next.get("a").asNode();
			String entityName = node.get("name").asString();
			long id = node.get("id").asLong();
			return EntityGraph.of(id, node.id(), tenantId, entityName, type);
		}
		else {
			return null;
		}

	}



	@Inject
	Driver driver;

	@Inject
	Logger _logger;

}
