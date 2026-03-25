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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("v0 → v1 upcasting")
class TenantEventUpcasterV0ToV1Test {

	private ObjectMapper mapper;

	// -- Shared JSON payloads --

	private static final String V0_TENANT_FULL = """
		{
			"tenantId": "t1",
			"routeAuthorizationMap": {
				"SEARCHER": "API_KEY",
				"DATASOURCE": "OAUTH2"
			}
		}""";

	private static final String V0_TENANT_NO_MAP = """
		{ "tenantId": "some-tenant" }""";

	private static final String V0_TENANT_NO_SEARCHER = """
		{
			"tenantId": "t1",
			"routeAuthorizationMap": {
				"DATASOURCE": "OAUTH2"
			}
		}""";

	private static final String V1_TENANT = """
		{
			"tenantId": "t1",
			"routeAuthorizationMap": {
				"ADMINISTRATION": "OAUTH2",
				"SEARCH": "OAUTH2"
			}
		}""";

	private static final String V0_API_KEY_NO_GROUP = """
		{
			"tenantId": "t1",
			"expirationDate": "2025-01-01T00:00:00Z"
		}""";

	private static final String V0_API_KEY_NO_EXPIRATION = """
		{
			"tenantId": "t1",
			"apiGroup": "SEARCH"
		}""";

	private static final String V0_API_KEY_EMPTY = """
		{ "tenantId": "t1" }""";

	private static final String V1_API_KEY = """
		{
			"tenantId": "t1",
			"apiGroup": "SEARCH",
			"expirationDate": "2025-01-01T00:00:00Z"
		}""";

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper();
	}

	@Nested
	@DisplayName("TenantCreated Event")
	class TenantCreatedEventTests {

		@Test
		@DisplayName(
			"DATASOURCE + SEARCHER is replaced by v1 map"
		)
		void v0_appliesTransformations() throws Exception {
			JsonNode root = mapper.readTree(V0_TENANT_FULL);

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, root);

			assertThat(result).isNotNull();
			JsonNode m = result.get("routeAuthorizationMap");
			assertThat(m.has("DATASOURCE")).isFalse();
			assertThat(m.has("SEARCHER")).isFalse();
			assertThat(m.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(m.get("SEARCH").asText())
				.isEqualTo("API_KEY");
			assertThat(m.get("INGESTION").asText())
				.isEqualTo("API_KEY");
			assertThat(m.get("PUBLIC").asText())
				.isEqualTo("API_KEY");
			assertThat(m).hasSize(4);
		}

		@Test
		@DisplayName(
			"Missing routeAuthorizationMap falls back to "
			+ "safe default"
		)
		void missingMap_fallsBackToDefault() throws Exception {
			JsonNode root = mapper.readTree(V0_TENANT_NO_MAP);

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, root);

			JsonNode m = result.get("routeAuthorizationMap");
			assertThat(m).isNotNull();
			assertThat(m.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(m.get("SEARCH").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m.get("INGESTION").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m.get("PUBLIC").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m).hasSize(4);
		}

		@Test
		@DisplayName(
			"Missing SEARCHER defaults SEARCH, INGESTION, "
			+ "PUBLIC to NO_AUTH"
		)
		void missingSearcher_defaultsToNoAuth()
			throws Exception {

			JsonNode root = mapper.readTree(
				V0_TENANT_NO_SEARCHER);

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, root);

			JsonNode m = result.get("routeAuthorizationMap");
			assertThat(m.get("ADMINISTRATION").asText())
				.isEqualTo("OAUTH2");
			assertThat(m.get("SEARCH").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m.get("INGESTION").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m.get("PUBLIC").asText())
				.isEqualTo("NO_AUTH");
			assertThat(m).hasSize(4);
		}

		@Test
		@DisplayName("schemaVersion 1 skips transformations")
		void v1_skipsTransformations() throws Exception {
			JsonNode root = mapper.readTree(V1_TENANT);

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 1, root);

			// migration skipped — PUBLIC not added
			assertThat(result).isNotNull();
			assertThat(result.get("routeAuthorizationMap")
				.has("ADMINISTRATION")).isTrue();
			assertThat(result.get("routeAuthorizationMap")
				.has("PUBLIC")).isFalse();
		}

		@Test
		@DisplayName("null root is propagated")
		void null_isPropagated() {
			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, null);

			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("Field rename: schemaName → tenantName")
	class FieldRenameTests {

		@Test
		@DisplayName("schemaName is renamed to tenantName in v0 payload")
		void schemaName_isRenamedToTenantName() throws Exception {
			// a v0 payload with schemaName field
			JsonNode root = mapper.readTree("""
				{
					"tenantId": "t1",
					"schemaName": "my_schema",
					"routeAuthorizationMap": {
						"SEARCHER": "OAUTH2",
						"DATASOURCE": "OAUTH2"
					}
				}""");

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, root);

			// schemaName should be renamed to tenantName
			assertThat(result.has("schemaName")).isFalse();
			assertThat(result.get("tenantName").asText())
				.isEqualTo("my_schema");
		}

		@Test
		@DisplayName(
			"Missing schemaName field is handled gracefully")
		void missingSchemaName_isHandledGracefully()
			throws Exception {

			// a v0 payload without schemaName
			JsonNode root = mapper.readTree("""
				{
					"tenantId": "t1",
					"routeAuthorizationMap": {}
				}""");

			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 0, root);

			// no schemaName, no tenantName — just passes through
			assertThat(result.has("schemaName")).isFalse();
			assertThat(result.has("tenantName")).isFalse();
		}

		@Test
		@DisplayName(
			"renameSchemaNameToTenantName renames the field")
		void renameSchemaNameToTenantName_renamesField()
			throws Exception {

			ObjectNode node = mapper.createObjectNode();
			node.put("tenantId", "t1");
			node.put("schemaName", "my_schema");

			JsonNode result = TenantEventUpcaster
				.renameSchemaNameToTenantName(node);

			// verify rename
			assertThat(result.has("schemaName")).isFalse();
			assertThat(result.get("tenantName").asText())
				.isEqualTo("my_schema");
		}

		@Test
		@DisplayName(
			"renameSchemaNameToTenantName with null is safe")
		void renameSchemaNameToTenantName_withNull_isSafe() {
			JsonNode result = TenantEventUpcaster
				.renameSchemaNameToTenantName(null);

			// null propagated unchanged
			assertThat(result).isNull();
		}

		@Test
		@DisplayName(
			"v1 payload preserves tenantName without transformation")
		void v1Payload_preservesTenantName() throws Exception {
			JsonNode root = mapper.readTree("""
				{
					"tenantId": "t1",
					"tenantName": "already_v1",
					"routeAuthorizationMap": {
						"ADMINISTRATION": "OAUTH2"
					}
				}""");

			// schemaVersion=1 skips the migration
			JsonNode result = TenantEventUpcaster
				.upcastV0toV1("TenantCreated", 1, root);

			// tenantName untouched
			assertThat(result.get("tenantName").asText())
				.isEqualTo("already_v1");
		}
	}

	@Nested
	@DisplayName("ApiKeyCreated Event")
	class ApiKeyCreatedEventTests {

		@Test
		@DisplayName("Missing apiGroup is not v1")
		void missingApiGroup_isNotV1() throws Exception {
			JsonNode root = mapper.readTree(V0_API_KEY_NO_GROUP);

			assertThat(TenantEventUpcaster
				.isV1ApiKeyCreated(root)).isFalse();
		}

		@Test
		@DisplayName("Missing expirationDate is not v1")
		void missingExpirationDate_isNotV1()
			throws Exception {

			JsonNode root = mapper.readTree(
				V0_API_KEY_NO_EXPIRATION);

			assertThat(TenantEventUpcaster
				.isV1ApiKeyCreated(root)).isFalse();
		}

		@Test
		@DisplayName("Both fields missing is not v1")
		void bothMissing_isNotV1() throws Exception {
			JsonNode root = mapper.readTree(V0_API_KEY_EMPTY);

			assertThat(TenantEventUpcaster
				.isV1ApiKeyCreated(root)).isFalse();
		}

		@Test
		@DisplayName("Both fields present is v1")
		void bothPresent_isV1() throws Exception {
			JsonNode root = mapper.readTree(V1_API_KEY);

			assertThat(TenantEventUpcaster
				.isV1ApiKeyCreated(root)).isTrue();
		}
	}
}
