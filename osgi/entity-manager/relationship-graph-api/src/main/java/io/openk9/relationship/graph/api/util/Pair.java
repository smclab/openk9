package io.openk9.relationship.graph.api.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Conditions;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;

import java.util.List;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;
import static org.neo4j.cypherdsl.core.Cypher.mapOf;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Pair<KEY, VALUE> {
	private final KEY key;
	private final VALUE value;

	public static void main(String[] args) {
		Node alias = Cypher.node("person").named("p");
		Property entityName = alias.property("entityName");
		Property tenantId = alias.property("tenantId");

		Statement build = Cypher
			.match(alias)
			.where(
				tenantId.eq(Cypher.literalOf(1))
				.and(
					entityName
						.contains(
							Cypher.literalOf("daniele"))
						.and(
							entityName
								.contains(
									Cypher.literalOf(
									"caldarini"
									)
								)
						)
				)
			)
			.returning(alias)
			.build();

		System.out.println(build.getCypher());

		Statement build1 = Cypher
			.call("db" , "index", "fulltext", "createNodeIndex")
			.withArgs(
				Cypher.literalOf("person"),
				Cypher.listOf(Cypher.literalOf("person")),
				Cypher.listOf(Cypher.literalOf("entityName")))
			.build();

		System.out.println(build1.getCypher());

		Node entity = Cypher
			.node("person")
			.named("entity")
			.withProperties(
				"entityName", Cypher.literalOf("Daniele Caldarini"),
				"tenantId", Cypher.literalOf(1));

		Statement build2 = Cypher
			.create(entity)
			.returning(entity)
			.build();

		System.out.println(build2.getCypher());

		Node test = Cypher.anyNode("test");
		Property e = test.property("entityName");
		Property t = test.property("tenantId");



		Statement build3 = Cypher
			.match(test)
			.where(
				Functions.id(test).eq(Cypher.literalOf(1))
			)
			.returning(test)
			.build();

		System.out.println(build3.getCypher());

		Node entity4 = Cypher.anyNode("entity");

		Property entityNameProperty = entity.property("entityName");

		Statement build4 = Cypher
			.match(entity4)
			.where(entityNameProperty.eq(Cypher.literalOf("caldarini")))
			.returning(entity)
			.build();

		System.out.println(build4.getCypher());

		List<String> entityNames = List.of("name1", "name2", "name3");

		Node named = Cypher.node("person").named("p");

		Property entityName1 = named.property("entityName");

		Statement[] statements = entityNames
			.stream()
			.map(nam -> Cypher
				.match(named)
				.where(entityName1.eq(literalOf(nam)))
				.returning(named)
				.build()
			)
			.toArray(Statement[]::new);

		Statement union = Cypher.unionAll(statements);

		System.out.println();
		System.out.println(union.getCypher());


		/*
		CALL gds.alpha.bfs.stream({
                  nodeProjection: '*',
                  relationshipProjection: {
                    relType: {
                      type: '*',
                      orientation: 'UNDIRECTED',
                      properties: {}
                        }
                    },
                    startNode: 1,
                    maxDepth: 5
                  })
                  YIELD
                  // general stream return columns
                  startNodeId, nodeIds, path
                  RETURN nodeIds;
		 */

		SymbolicName path = Cypher.name("path");
		SymbolicName n = Cypher.name("n");

		Statement build5 = Cypher.call("gds.alpha.bfs.stream")
			.withArgs(
				mapOf(
					"nodeProjection", literalOf("*"),
					"relationshipProjection", mapOf(
						"relType", mapOf(
							"type", literalOf("*"), "orientation", literalOf("UNDIRECTED"), "properties", mapOf())),
					"startNode", literalOf(1), "maxDepth", literalOf(5)
				)
			)
			.yield(path)
			.with(path)
			.unwind(Cypher.listOf(n.in(Functions.nodes(path)), Conditions.isFalse().as(n))).as("entities")
			.returning("entities")
			.build();

		System.out.println();
		System.out.println(build5.getCypher());

		Node nodeEntity =
			Cypher.node("test").named("entity");
		AliasedExpression aliasedExpression = nodeEntity.as("entity");
		SymbolicName path1 = Cypher.name("path");

		Statement entity1 = Cypher
			.match(nodeEntity)
			.where(Functions.id(nodeEntity).eq(literalOf(1)))
			.call("apoc.path.expand").withArgs(aliasedExpression.getDelegate(), Cypher.literalOf(null), Cypher.literalOf(null), Cypher.literalOf(1), Cypher.literalOf(5))
			.yield(path1)
			.returning(Functions.last(Functions.nodes(path1)).as("node"), Functions.size(Functions.nodes(path1)).subtract(literalOf(1)).as("hops"))
			.build();

		Statement entity2 = Cypher
			.match(nodeEntity)
			.where(Functions.id(nodeEntity).eq(literalOf(1)))
			.call("apoc.path.expand").withArgs(aliasedExpression.getDelegate(), Cypher.literalOf(null), Cypher.literalOf(null), Cypher.literalOf(1), Cypher.literalOf(5))
			.yield(path1)
			.returning(Functions.last(Functions.nodes(path1)).as("node"), Functions.size(Functions.nodes(path1)).subtract(literalOf(1)).as("hops"))
			.build();

		System.out.println();
		System.out.println(entity1.getCypher());

		System.out.println();
		System.out.println(Cypher
			.call(Cypher.unionAll(entity1, entity2))
			.returning("node", "hops")
			.orderBy(Cypher.name("hops"))
			.build().getCypher());



	}

}
