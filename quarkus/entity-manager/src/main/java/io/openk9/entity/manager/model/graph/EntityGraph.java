package io.openk9.entity.manager.model.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.neo4j.driver.types.Node;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class EntityGraph {
	private long id;
	private Long graphId;
	private long tenantId;
	private String name;
	private String type;

	public static EntityGraph from(String type, Node node) {
		return new EntityGraph(
			node.get("id").asLong(),
			node.id(),
			node.get("tenantId").asLong(),
			node.get("name").asString(),
			type
		);
	}

}
