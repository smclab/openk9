package io.openk9.entity.manager.service.graph;

import io.openk9.entity.manager.model.graph.EntityGraph;
import io.quarkus.arc.Unremovable;
import org.jboss.logging.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;

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
			.thenApply(record -> EntityGraph.from(record.get("f").asNode()))
			.thenCompose(
				persistedFruit ->
					session.closeAsync().thenApply(signal -> persistedFruit));

	}

	public CompletionStage<Void> createRelationship(
		long graphId1, long graphId2, String relationName) {

		AsyncSession session = driver.asyncSession();

		return session
			.writeTransactionAsync(tx -> tx
				.runAsync(
					"MATCH (a), (b)\n" +
					"WHERE id(a) = " + graphId1 + " AND id(b) = " + graphId2 + "\n" +
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



	@Inject
	Driver driver;

	@Inject
	Logger _logger;

}
