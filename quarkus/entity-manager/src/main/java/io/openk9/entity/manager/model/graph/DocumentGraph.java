package io.openk9.entity.manager.model.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.neo4j.driver.types.Node;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class DocumentGraph {
	private Long id;
	private Long datasourceId;
	private Long tenantId;
	private String contentId;

	public static DocumentGraph from(Node node) {
		return new DocumentGraph(
			node.id(),
			node.get("datasourceId").asLong(),
			node.get("tenantId").asLong(),
			node.get("contentId").asString()
		);
	}

}
