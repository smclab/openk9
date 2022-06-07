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

package io.openk9.auth.enrich;

import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.DatasourceContext;
import io.openk9.model.EnrichItem;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.SyncEnrichProcessor;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EnrichProcessor.class
)
public class DefaultACLEnrichProcessor implements SyncEnrichProcessor {

	@Override
	public String name() {
		return DefaultACLEnrichProcessor.class.getName();
	}

	@Override
	public Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		EnrichItem enrichItem, PluginDriverDTO pluginDriverDTO) {

		return Mono.fromSupplier(() -> {

			JsonNode aclNode = objectNode.remove("acl");

			ObjectNode aclIndexNode = objectNode
				.toObjectNode()
				.putObject("acl");

			if (aclNode == null || aclNode.isEmpty()) {
				aclIndexNode.put("public", true);
			}
			else if (aclNode.has("public")) {
				aclIndexNode.put(
					"public",
					aclNode.get("public").asBoolean(true));
			}
			else {

				String pluginName = pluginDriverDTO.getName();

				ObjectNode aclPluginIndexNode =
					aclIndexNode.putObject(pluginName);

				aclPluginIndexNode.putAll(aclNode.toObjectNode());

			}

			return objectNode;

		});


	}

}
