/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.entity.manager.service.graph;

import io.openk9.entity.manager.model.graph.DocumentGraph;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

import java.util.List;

@ApplicationScoped
@Unremovable
public class EntityGraphService {

	public EntityGraph getEntity(String id) {

		try (Session session = driver.session()) {

			Result result = session.run(
				"MATCH (n) WHERE n.id = $id RETURN n",
				Values.parameters("id", id)
			);

			if (result.hasNext()) {

				Record record = result.next();

				Node node = record.get(0).asNode();

				String type = node.labels().iterator().next();

				return EntityGraph.from(type, node);

			}
			else {
				return null;
			}

		}
	}

	public List<EntityGraph> search(Statement statement) {

		try (Session session = driver.session()) {

			Result result = session.run(statement.getCypher());

			return result
				.list(record -> {

					Node node = record.get(0).asNode();

					String type = node.labels().iterator().next();

					return EntityGraph.from(type, node);
				});

		}
	}

	public List<EntityGraph> search(String type, String sql, Value value) {

		try (Session session = driver.session()) {

			Result result = session.run(sql, value);

			return result
				.list(record -> EntityGraph.from(type, record.get(0).asNode()));

		}
	}

	public DocumentGraph insertDocument(DocumentGraph entityGraph) {

		try (Session session = driver.session()) {

			return session.writeTransaction(tx -> {
				Result result = tx.run(
					"CREATE (f:document" +
					" {contentId: $contentId, datasourceId: $datasourceId, tenantId: $tenantId}) RETURN f",
					Values.parameters(
						"contentId", entityGraph.getContentId(),
						"datasourceId", entityGraph.getDatasourceId(),
						"tenantId", entityGraph.getTenantId()
					)
				);

				Node node = result.single().get("f").asNode();

				return DocumentGraph.from(node);

			});
		}
	}

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

	public void createDocumentRelationship(
		String entityId, long documentId, String relationName) {

		try (Session session = driver.session()) {

			session.writeTransaction(tx -> {
				Result result = tx.run(
					"MATCH (a), (b)\n" +
					"WHERE a.id = $id1 AND ID(b) = $id2\n" +
					"MERGE (a)-[r:" + relationName + "]->(b)\n" +
					"RETURN type(r)",
					Values.parameters(
						"id1", entityId,
						"id2", documentId
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

	public void createRelationship(
		String graphId1, String graphId2, String relationName) {

		try (Session session = driver.session()) {

			session.writeTransaction(tx -> {
				Result result = tx.run(
					"MATCH (a), (b)\n" +
					"WHERE a.id = $id1 AND b.id = $id2\n" +
					"MERGE (a)-[r:" + relationName + "]->(b)\n" +
					"RETURN type(r)",
					Values.parameters(
						"id1", graphId1,
						"id2", graphId2
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

	@Inject
	Driver driver;

	@Inject
	Logger _logger;

}
