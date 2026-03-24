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
 * Transforms legacy tenant event JSON payloads into the current
 * schema before Jackson deserialization.
 *
 * <h3>Why upcasting exists</h3>
 * <p>The API Gateway consumes tenant events from a RabbitMQ
 * <em>stream queue</em> with {@code x-stream-offset: first},
 * replaying the full history on every startup. This means old
 * events (published under earlier schema versions) coexist with
 * current ones. The upcaster ensures all payloads conform to
 * the current schema before deserialization.
 *
 * <h3>Cumulative migration chain</h3>
 * <p>Migrations run as a sequential chain: {@code v0→v1},
 * then {@code v1→v2}, and so on. Each migration receives the
 * message's {@code x-schema-version} and skips itself if the
 * message is already at or past that version. A {@code v0}
 * message passes through every migration cumulatively; a
 * {@code v1} message skips the first, and so forth.
 *
 * <h3>Adding a new migration</h3>
 * <ol>
 *   <li>Increment
 *       {@link TenantEvent#CURRENT_SCHEMA_VERSION}.</li>
 *   <li>Write the new {@code upcastVNtoVN+1()} method with
 *       the guard clause
 *       {@code if (root == null || schemaVersion >= N+1)
 *       return root;}.</li>
 *   <li>Append the call in {@link #upcast}.</li>
 * </ol>
 *
 * <p>A migration may return {@code null} to signal that the
 * event should be discarded (e.g. a legacy
 * {@code ApiKeyCreated} missing required fields).
 */
@Slf4j
final class TenantEventUpcaster {

	private TenantEventUpcaster() {}

	/**
	 * Run the full migration chain on the given event payload.
	 *
	 * <p>Every migration is called in sequence. Each one
	 * inspects {@code schemaVersion} and skips itself when
	 * not applicable.
	 *
	 * @param eventType     the {@code x-event-type} header
	 * @param schemaVersion the {@code x-schema-version} header
	 *                      ({@code 0} if absent — legacy)
	 * @param root          the raw JSON payload
	 * @return the (possibly transformed) root node, or
	 *         {@code null} if the event must be discarded
	 */
	static JsonNode upcast(
		String eventType, int schemaVersion, JsonNode root) {

		root = upcastV0toV1(eventType, schemaVersion, root);
		// future: root = upcastV1toV2(eventType, schemaVersion, root);
		return root;
	}

	// -- v0 → v1 --
	// TenantCreated/TenantUpdated: routeAuthorizationMap had
	//   DATASOURCE and SEARCHER keys. DATASOURCE is replaced
	//   by ADMINISTRATION. SEARCHER is replaced by SEARCH,
	//   PUBLIC, and INGESTION (all copy the SEARCHER value).
	// ApiKeyCreated: lacked apiGroup and expirationDate fields;
	//   these events are discarded.

	/**
	 * Migrate a v0 payload to v1.
	 *
	 * @return the transformed node, or {@code null} to discard
	 */
	static JsonNode upcastV0toV1(
		String eventType, int schemaVersion, JsonNode root) {

		if (root == null || schemaVersion >= 1) {
			return root;
		}

		return switch (eventType) {
			case TENANT_CREATED, TENANT_UPDATED ->
				upcastRouteAuthorizationMap(root);
			case API_KEY_CREATED -> {
				if (!isV1ApiKeyCreated(root)) {
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

	/**
	 * Replaces the v0 route map (DATASOURCE + SEARCHER) with
	 * the v1 map (ADMINISTRATION, SEARCH, INGESTION, PUBLIC).
	 * If the v0 map is missing or incomplete, falls back to a
	 * legacy default: ADMINISTRATION=OAUTH2, rest=NO_AUTH.
	 */
	static JsonNode upcastRouteAuthorizationMap(JsonNode root) {
		if (!(root instanceof ObjectNode node)) {
			return root;
		}

		// read SEARCHER value before clearing, default NO_AUTH
		String searchScheme = "NO_AUTH";
		JsonNode mapNode = node.get("routeAuthorizationMap");

		if (mapNode instanceof ObjectNode oldMap) {
			JsonNode searcher = oldMap.get("SEARCHER");
			if (searcher != null) {
				searchScheme = searcher.asText();
			}
		}

		// build the v1 map from scratch
		ObjectNode v1Map = node.putObject("routeAuthorizationMap");
		v1Map.put("ADMINISTRATION", "OAUTH2");
		v1Map.put("SEARCH", searchScheme);
		v1Map.put("INGESTION", searchScheme);
		v1Map.put("PUBLIC", searchScheme);

		return node;
	}

	static boolean isV1ApiKeyCreated(JsonNode root) {
		return root.has("apiGroup")
			&& root.has("expirationDate");
	}

}
