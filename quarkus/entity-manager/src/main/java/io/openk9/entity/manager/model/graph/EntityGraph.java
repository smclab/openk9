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

package io.openk9.entity.manager.model.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.neo4j.driver.types.Node;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class EntityGraph {
	private String id;
	private Long graphId;
	private long tenantId;
	private String name;
	private String type;

	public static EntityGraph from(String type, Node node) {
		return new EntityGraph(
			node.get("id").asString(),
			node.id(),
			node.get("tenantId").asLong(),
			node.get("name").asString(),
			type
		);
	}

}
