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

package io.openk9.search.enrich.api;

import io.openk9.model.EnrichItem;
import io.openk9.model.DatasourceContext;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import reactor.core.publisher.Mono;

public interface EnrichProcessor {

	Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		EnrichItem enrichItem, PluginDriverDTO pluginDriverDTO);

	default Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		PluginDriverDTO pluginDriverDTO) {

		EnrichItem enrichItem = datasourceContext
			.getEnrichItems()
			.stream()
			.filter(e -> e.getServiceName().equals(name()))
			.findFirst().orElseThrow(IllegalStateException::new);

		return process(
			objectNode, datasourceContext, enrichItem, pluginDriverDTO);

	}

	String name();

	EnrichProcessor NOTHING = new EnrichProcessor() {
		@Override
		public Mono<ObjectNode> process(
			ObjectNode objectNode, DatasourceContext datasourceContext,
			EnrichItem enrichItem, PluginDriverDTO pluginDriverDTO) {

			String pluginDriverName = pluginDriverDTO.getName();

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
