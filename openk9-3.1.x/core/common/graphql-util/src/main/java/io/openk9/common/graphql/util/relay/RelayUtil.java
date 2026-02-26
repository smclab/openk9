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

package io.openk9.common.graphql.util.relay;

import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class RelayUtil {

	public static String encodeCursor(GraphqlId graphqlId) {
		return Base64.getEncoder().encodeToString(
			Json.encode(
				Cursor
					.builder()
					.id(graphqlId.getId())
					.build()
				)
				.getBytes(StandardCharsets.UTF_8)
		);
	}

	public static Cursor decodeCursor(String cursor) {
		return Json.decodeValue(
			new String(Base64.getDecoder().decode(cursor)),
			Cursor.class
		);
	}

	public static <T extends GraphqlId> List<Edge<T>> toEdgeList(List<T> entitiesList) {

		if (entitiesList == null || entitiesList.isEmpty()) {
			return List.of();
		}

		return entitiesList.stream()
			.map(entity -> new DefaultEdge<>(
				entity, RelayUtil.encodeCursor(entity)))
			.collect(Collectors.toList());
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Cursor {
		private long id;
	}

}
