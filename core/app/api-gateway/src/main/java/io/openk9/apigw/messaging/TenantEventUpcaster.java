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

package io.openk9.apigw.messaging;

import static io.openk9.event.tenant.TenantEvent.API_KEY_CREATED;
import static io.openk9.event.tenant.TenantEvent.TENANT_CREATED;
import static io.openk9.event.tenant.TenantEvent.TENANT_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Transforms legacy (v0) tenant management event JSON into the
 * current (v1) schema before Jackson deserialization.
 *
 * <p>Returns {@code null} when the event should be silently
 * discarded (e.g. legacy ApiKeyCreated without required fields).
 */
@Slf4j
final class TenantEventUpcaster {

	private TenantEventUpcaster() {}

	/**
	 * Upcast the raw JSON for the given event type.
	 *
	 * @return the (possibly transformed) root node, or
	 *         {@code null} if the event must be skipped
	 */
	static JsonNode upcast(String eventType, JsonNode root) {
		return switch (eventType) {
			case TENANT_CREATED, TENANT_UPDATED ->
				upcastTenantEvent(root);
			case API_KEY_CREATED -> {
				if (isLegacyApiKeyCreated(root)) {
					log.warn(
						"Ignoring legacy ApiKeyCreated event "
						+ "for tenant: {}",
						root.path("tenantId")
							.asText("unknown"));
					yield null;
				}
				yield root;
			}
			default -> root;
		};
	}

	// -- v0 → v1 transformations --

	private static JsonNode upcastTenantEvent(JsonNode root) {
		if (!(root instanceof ObjectNode node)) {
			return root;
		}
		upcastRouteAuthorizationMap(node);
		return node;
	}

	static JsonNode upcastRouteAuthorizationMap(ObjectNode node) {
		if (!node.has("routeAuthorizationMap")) {
			return node;
		}
		JsonNode authMapNode = node.get("routeAuthorizationMap");
		if (!(authMapNode instanceof ObjectNode authMap)) {
			return node;
		}
		if (!authMap.has("DATASOURCE")) {
			return node;
		}

		JsonNode searchScheme = authMap.get("SEARCH");
		if (searchScheme == null) {
			log.warn(
				"Legacy event missing SEARCH key in "
				+ "routeAuthorizationMap, defaulting PUBLIC "
				+ "and INGESTION to OAUTH2");
		}
		String searchSchemeStr =
			(searchScheme != null)
				? searchScheme.asText()
				: "OAUTH2";

		authMap.put("ADMINISTRATION", "OAUTH2");
		authMap.put("PUBLIC", searchSchemeStr);
		authMap.put("INGESTION", searchSchemeStr);
		authMap.remove("DATASOURCE");
		if (!authMap.has("SEARCH")) {
			authMap.put("SEARCH", searchSchemeStr);
		}

		return node;
	}

	static boolean isLegacyApiKeyCreated(JsonNode root) {
		return !root.has("apiGroup")
			|| !root.has("expirationDate");
	}

}
