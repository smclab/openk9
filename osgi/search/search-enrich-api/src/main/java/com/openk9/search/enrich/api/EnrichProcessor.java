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

package com.openk9.search.enrich.api;

import com.openk9.datasource.model.EnrichItem;
import com.openk9.datasource.util.DatasourceContext;
import com.openk9.json.api.JsonNode;
import com.openk9.json.api.ObjectNode;
import reactor.core.publisher.Mono;

public interface EnrichProcessor {

	Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		EnrichItem enrichItem, String pluginDriverName);

	default Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		String pluginDriverName) {

		EnrichItem enrichItem = datasourceContext
			.getEnrichItems()
			.stream()
			.filter(e -> e.getServiceName().equals(name()))
			.findFirst().orElseThrow(IllegalStateException::new);

		return process(
			objectNode, datasourceContext, enrichItem, pluginDriverName);

	}

	String name();

	EnrichProcessor NOTHING = new EnrichProcessor() {
		@Override
		public Mono<ObjectNode> process(
			ObjectNode objectNode, DatasourceContext datasourceContext,
			EnrichItem enrichItem, String pluginDriverName) {

			if (objectNode.hasNonNull(pluginDriverName)) {

				JsonNode driverData =
					objectNode.remove(pluginDriverName);

				driverData
					.toObjectNode()
					.stream()
					.forEach(e -> objectNode.put(e.getKey(), e.getValue()));

			}

			return Mono.just(objectNode);
		}

		@Override
		public String name() {
			return "NOTHING";
		}
	};

}
