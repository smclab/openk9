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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("v0 → v1 upcasting")
class TenantEventUpcasterV0ToV1Test {

	private ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper();
	}

	@Nested
	@DisplayName("upcastSecurityConfiguration")
	class UpcastSecurityConfigurationTests {

		@Test
		@DisplayName("API_KEY_ONLY value is replaced with LEGACY")
		void apiKeyOnly_isReplacedWithLegacy() {
			// build a node with the deprecated enum value
			ObjectNode node = mapper.createObjectNode();
			node.put("securityConfiguration", "API_KEY_ONLY");

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastSecurityConfiguration(node);

			// verify the value is replaced
			assertThat(result.get("securityConfiguration").asText())
				.isEqualTo("LEGACY");
		}

		@Test
		@DisplayName("Modern value is not modified")
		void modernValue_isUnchanged() {
			// build a node with a current enum value
			ObjectNode node = mapper.createObjectNode();
			node.put("securityConfiguration", "PROFILED");

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastSecurityConfiguration(node);

			// verify the value is unchanged
			assertThat(result.get("securityConfiguration").asText())
				.isEqualTo("PROFILED");
		}

		@Test
		@DisplayName("Missing field leaves node unchanged")
		void missingField_isUnchanged() {
			// build a node without securityConfiguration
			ObjectNode node = mapper.createObjectNode();
			node.put("tenantId", "some-tenant");

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastSecurityConfiguration(node);

			// verify no field was added
			assertThat(result.has("securityConfiguration")).isFalse();
			assertThat(result.get("tenantId").asText())
				.isEqualTo("some-tenant");
		}

		@Test
		@DisplayName("LEGACY value is idempotent")
		void legacyValue_isIdempotent() {
			// build a node already set to LEGACY
			ObjectNode node = mapper.createObjectNode();
			node.put("securityConfiguration", "LEGACY");

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastSecurityConfiguration(node);

			// verify the value is still LEGACY
			assertThat(result.get("securityConfiguration").asText())
				.isEqualTo("LEGACY");
		}
	}

	@Nested
	@DisplayName("upcastRouteAuthorizationMap")
	class UpcastRouteAuthorizationMapTests {

		@Test
		@DisplayName("Modern map with ADMINISTRATION is not modified")
		void modernMap_isUnchanged() {
			// build a v1 routeAuthorizationMap (4 modern keys)
			ObjectNode node = mapper.createObjectNode();
			ObjectNode authMap = mapper.createObjectNode();
			authMap.put("ADMINISTRATION", "OAUTH2");
			authMap.put("SEARCH", "OAUTH2");
			authMap.put("INGESTION", "OAUTH2");
			authMap.put("PUBLIC", "NO_AUTH");
			node.set("routeAuthorizationMap", authMap);

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastRouteAuthorizationMap(node);

			// verify the map is unchanged
			JsonNode resultMap = result.get("routeAuthorizationMap");
			assertThat(resultMap.has("DATASOURCE")).isFalse();
			assertThat(resultMap.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap.get("PUBLIC").asText())
				.isEqualTo("NO_AUTH");
		}

		@Test
		@DisplayName("Legacy map with DATASOURCE is transformed")
		void legacyMap_isTransformed() {
			// build a v0 routeAuthorizationMap (DATASOURCE marker)
			ObjectNode node = mapper.createObjectNode();
			ObjectNode authMap = mapper.createObjectNode();
			authMap.put("SEARCH", "API_KEY");
			authMap.put("DATASOURCE", "OAUTH2");
			node.set("routeAuthorizationMap", authMap);

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastRouteAuthorizationMap(node);

			// verify DATASOURCE is removed and modern keys are set
			JsonNode resultMap = result.get("routeAuthorizationMap");
			assertThat(resultMap.has("DATASOURCE")).isFalse();
			assertThat(resultMap.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap.get("PUBLIC").asText())
				.isEqualTo("API_KEY");
			assertThat(resultMap.get("INGESTION").asText())
				.isEqualTo("API_KEY");
		}

		@Test
		@DisplayName("Missing field leaves node unchanged")
		void missingField_isUnchanged() {
			// build a node without routeAuthorizationMap
			ObjectNode node = mapper.createObjectNode();
			node.put("tenantId", "some-tenant");

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastRouteAuthorizationMap(node);

			// verify no field was added
			assertThat(result.has("routeAuthorizationMap")).isFalse();
		}

		@Test
		@DisplayName(
			"Missing SEARCH key defaults all routes to OAUTH2 "
			+ "and backfills SEARCH"
		)
		void missingSearchKey_defaultsToOAuth2() {
			// build a v0 map with DATASOURCE but no SEARCH key
			ObjectNode node = mapper.createObjectNode();
			ObjectNode authMap = mapper.createObjectNode();
			authMap.put("DATASOURCE", "OAUTH2");
			node.set("routeAuthorizationMap", authMap);

			// upcast
			JsonNode result =
				TenantEventUpcaster
					.upcastRouteAuthorizationMap(node);

			// verify all 4 modern keys default to OAUTH2
			JsonNode resultMap = result.get("routeAuthorizationMap");
			assertThat(resultMap.has("DATASOURCE")).isFalse();
			assertThat(resultMap.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap.get("PUBLIC").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap.get("INGESTION").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap.get("SEARCH").asText())
				.isEqualTo("OAUTH2");
			assertThat(resultMap).hasSize(4);
		}
	}

	@Nested
	@DisplayName("isLegacyApiKeyCreated")
	class IsLegacyApiKeyCreatedTests {

		@Test
		@DisplayName("Missing apiGroup is legacy")
		void missingApiGroup_isLegacy() {
			// only expirationDate, no apiGroup
			ObjectNode node = mapper.createObjectNode();
			node.put("expirationDate", "2025-01-01T00:00:00Z");

			assertThat(TenantEventUpcaster
				.isLegacyApiKeyCreated(node)).isTrue();
		}

		@Test
		@DisplayName("Missing expirationDate is legacy")
		void missingExpirationDate_isLegacy() {
			// only apiGroup, no expirationDate
			ObjectNode node = mapper.createObjectNode();
			node.put("apiGroup", "SEARCH");

			assertThat(TenantEventUpcaster
				.isLegacyApiKeyCreated(node)).isTrue();
		}

		@Test
		@DisplayName("Both fields missing is legacy")
		void bothMissing_isLegacy() {
			// neither apiGroup nor expirationDate
			ObjectNode node = mapper.createObjectNode();
			node.put("tenantId", "t1");

			assertThat(TenantEventUpcaster
				.isLegacyApiKeyCreated(node)).isTrue();
		}

		@Test
		@DisplayName("Both fields present is modern")
		void bothPresent_isModern() {
			// both apiGroup and expirationDate present
			ObjectNode node = mapper.createObjectNode();
			node.put("apiGroup", "SEARCH");
			node.put("expirationDate", "2025-01-01T00:00:00Z");

			assertThat(TenantEventUpcaster
				.isLegacyApiKeyCreated(node)).isFalse();
		}
	}
}
