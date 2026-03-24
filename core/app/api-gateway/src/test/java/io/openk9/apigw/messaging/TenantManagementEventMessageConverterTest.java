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
import static org.assertj.core.api.Assertions.assertThatCode;

import static io.openk9.event.tenant.TenantEvent.API_KEY_CREATED;
import static io.openk9.event.tenant.TenantEvent.TENANT_CREATED;
import static io.openk9.event.tenant.TenantEvent.TENANT_UPDATED;

import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.AuthorizationScheme;
import io.openk9.event.tenant.TenantEvent;
import io.openk9.event.tenant.TenantEventConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class TenantManagementEventMessageConverterTest {

	private TenantManagementEventMessageConverter converter;
	private ObjectMapper mapper;

	// -- Shared JSON payloads --

	private static final String V0_TENANT_API_KEY = """
		{
			"tenantId": "v0-tenant",
			"hostName": "v0.localhost",
			"schemaName": "v0_tenant",
			"routeAuthorizationMap": {
				"SEARCH": "API_KEY",
				"DATASOURCE": "OAUTH2"
			}
		}""";

	private static final String V0_TENANT_OAUTH2 = """
		{
			"tenantId": "v0-tenant",
			"hostName": "v0.localhost",
			"schemaName": "v0_tenant",
			"routeAuthorizationMap": {
				"SEARCH": "OAUTH2",
				"DATASOURCE": "OAUTH2"
			}
		}""";

	private static final String V1_TENANT = """
		{
			"tenantId": "v1-tenant",
			"hostName": "v1.localhost",
			"schemaName": "v1_tenant",
			"issuerUri": "http://keycloak/realms/test",
			"routeAuthorizationMap": {
				"ADMINISTRATION": "OAUTH2",
				"SEARCH": "OAUTH2",
				"INGESTION": "OAUTH2",
				"PUBLIC": "NO_AUTH"
			}
		}""";

	private static final String V1_TENANT_PARTIAL = """
		{
			"tenantId": "partial-tenant",
			"hostName": "partial.localhost",
			"schemaName": "partial_schema",
			"routeAuthorizationMap": {
				"ADMINISTRATION": "OAUTH2"
			}
		}""";

	private static final String V0_API_KEY_CREATED = """
		{
			"tenantId": "v0-tenant",
			"apiKeyHash": "some-hash",
			"checksum": "abc"
		}""";

	private static final String V1_API_KEY_CREATED = """
		{
			"tenantId": "v1-tenant",
			"apiKeyHash": "abc123hash",
			"checksum": "crc32val",
			"apiGroup": "SEARCH",
			"expirationDate": "2026-12-31T23:59:59Z"
		}""";

	private static final String MINIMAL_JSON = """
		{ "tenantId": "some-tenant" }""";

	// -- Setup and helpers --

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		converter = new TenantManagementEventMessageConverter(mapper);
	}

	private Message createMessage(String json, String eventType) {
		MessageProperties props = new MessageProperties();
		if (eventType != null) {
			props.setHeader("x-event-type", eventType);
		}
		return new Message(json.getBytes(), props);
	}

	// -----------------------------------------------------------------
	// Integration tests for fromMessage (full converter flow)
	// -----------------------------------------------------------------

	@Nested
	@DisplayName("fromMessage")
	class FromMessageTests {

		@Test
		@DisplayName(
			"Legacy TenantCreated with DATASOURCE is upcasted"
		)
		void legacyTenantCreated_isUpcasted() {
			// convert a v0 tenant event with DATASOURCE key
			Object result = converter.fromMessage(
				createMessage(V0_TENANT_API_KEY, TENANT_CREATED));

			// verify the event is deserialized as TenantCreated
			assertThat(result)
				.isInstanceOf(TenantEvent.TenantCreated.class);
			var event = (TenantEvent.TenantCreated) result;

			// verify the routeAuthorizationMap is upcasted
			// to 4 modern keys
			assertThat(event.tenantId()).isEqualTo("v0-tenant");
			assertThat(event.routeAuthorizationMap())
				.containsEntry(
					ApiGroup.ADMINISTRATION,
					AuthorizationScheme.OAUTH2)
				.containsEntry(
					ApiGroup.SEARCH,
					AuthorizationScheme.API_KEY)
				.containsEntry(
					ApiGroup.PUBLIC,
					AuthorizationScheme.API_KEY)
				.containsEntry(
					ApiGroup.INGESTION,
					AuthorizationScheme.API_KEY)
				.hasSize(4);
		}

		@Test
		@DisplayName("Modern TenantCreated is deserialized as-is")
		void modernTenantCreated_isDeserializedAsIs() {
			// convert a v1 tenant event
			Object result = converter.fromMessage(
				createMessage(V1_TENANT, TENANT_CREATED));

			// verify it passes through without transformation
			assertThat(result)
				.isInstanceOf(TenantEvent.TenantCreated.class);
			var event = (TenantEvent.TenantCreated) result;
			assertThat(event.tenantId()).isEqualTo("v1-tenant");
			assertThat(event.routeAuthorizationMap()).hasSize(4);
		}

		@Test
		@DisplayName("Legacy ApiKeyCreated is filtered out")
		void legacyApiKeyCreated_isFilteredOut() {
			// convert a v0 ApiKeyCreated (missing apiGroup
			// and expirationDate)
			Object result = converter.fromMessage(
				createMessage(
					V0_API_KEY_CREATED, API_KEY_CREATED));

			// verify it is silently discarded
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Modern ApiKeyCreated is deserialized")
		void modernApiKeyCreated_isDeserialized() {
			// convert a v1 ApiKeyCreated with all fields
			Object result = converter.fromMessage(
				createMessage(
					V1_API_KEY_CREATED, API_KEY_CREATED));

			// verify it is deserialized with all fields
			assertThat(result)
				.isInstanceOf(TenantEvent.ApiKeyCreated.class);
			var event = (TenantEvent.ApiKeyCreated) result;
			assertThat(event.tenantId()).isEqualTo("v1-tenant");
			assertThat(event.apiKeyHash()).isEqualTo("abc123hash");
			assertThat(event.apiGroup()).isEqualTo(ApiGroup.SEARCH);
			assertThat(event.expirationDate()).isNotNull();
		}

		@Test
		@DisplayName(
			"Legacy TenantUpdated with OAUTH2 SEARCH is upcasted"
		)
		void legacyTenantUpdated_oauth2Search_isUpcasted() {
			// convert a v0 TenantUpdated where SEARCH = OAUTH2
			Object result = converter.fromMessage(
				createMessage(
					V0_TENANT_OAUTH2, TENANT_UPDATED));

			assertThat(result)
				.isInstanceOf(TenantEvent.TenantUpdated.class);
			var event = (TenantEvent.TenantUpdated) result;

			// verify all routes inherit the OAUTH2 scheme
			assertThat(event.tenantId()).isEqualTo("v0-tenant");
			assertThat(event.routeAuthorizationMap())
				.containsEntry(
					ApiGroup.ADMINISTRATION,
					AuthorizationScheme.OAUTH2)
				.containsEntry(
					ApiGroup.SEARCH,
					AuthorizationScheme.OAUTH2)
				.containsEntry(
					ApiGroup.PUBLIC,
					AuthorizationScheme.OAUTH2)
				.containsEntry(
					ApiGroup.INGESTION,
					AuthorizationScheme.OAUTH2)
				.hasSize(4);
		}

		@Test
		@DisplayName(
			"Legacy TenantUpdated with API_KEY SEARCH is upcasted"
		)
		void legacyTenantUpdated_apiKeySearch_isUpcasted() {
			// convert a v0 TenantUpdated where SEARCH = API_KEY
			Object result = converter.fromMessage(
				createMessage(
					V0_TENANT_API_KEY, TENANT_UPDATED));

			assertThat(result)
				.isInstanceOf(TenantEvent.TenantUpdated.class);
			var event = (TenantEvent.TenantUpdated) result;

			// verify PUBLIC and INGESTION inherit API_KEY
			// from SEARCH
			assertThat(event.routeAuthorizationMap())
				.containsEntry(
					ApiGroup.ADMINISTRATION,
					AuthorizationScheme.OAUTH2)
				.containsEntry(
					ApiGroup.SEARCH,
					AuthorizationScheme.API_KEY)
				.containsEntry(
					ApiGroup.PUBLIC,
					AuthorizationScheme.API_KEY)
				.containsEntry(
					ApiGroup.INGESTION,
					AuthorizationScheme.API_KEY)
				.hasSize(4);
		}

		@Test
		@DisplayName("Missing x-event-type header returns null")
		void missingEventTypeHeader_returnsNull() {
			// convert a message without x-event-type header
			Object result = converter.fromMessage(
				createMessage(MINIMAL_JSON, null));

			// verify it is silently discarded
			assertThat(result).isNull();
		}

		@Test
		@DisplayName(
			"Modern map with only ADMINISTRATION is not "
			+ "treated as legacy"
		)
		void partialModernMap_isNotTreatedAsLegacy() {
			// convert a v1 tenant with a partial map
			// (only ADMINISTRATION)
			Object result = converter.fromMessage(
				createMessage(
					V1_TENANT_PARTIAL, TENANT_CREATED));

			assertThat(result)
				.isInstanceOf(TenantEvent.TenantCreated.class);
			var event = (TenantEvent.TenantCreated) result;

			// verify the map is passed through as-is
			assertThat(event.routeAuthorizationMap())
				.containsEntry(
					ApiGroup.ADMINISTRATION,
					AuthorizationScheme.OAUTH2)
				.hasSize(1);
		}
	}

	// -----------------------------------------------------------------
	// RabbitAdapter
	// -----------------------------------------------------------------

	@Nested
	@DisplayName("RabbitAdapter")
	class RabbitAdapterTests {

		@Test
		@DisplayName("Null payload does not throw")
		void nullPayload_doesNotThrow() {
			// create an adapter with a no-op consumer
			RabbitAdapter adapter =
				new RabbitAdapter(new NoOpTenantEventConsumer());

			// verify null payload is handled gracefully
			assertThatCode(() -> adapter.adapter(null))
				.doesNotThrowAnyException();
		}
	}

	// -----------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------

	private static final class NoOpTenantEventConsumer
		implements TenantEventConsumer {

		@Override
		public void handleApiKeyCreatedEvent(
			TenantEvent.ApiKeyCreated event) {}

		@Override
		public void handleApiKeyRevokedEvent(
			TenantEvent.ApiKeyRevoked event) {}

		@Override
		public void handleTenantCreatedEvent(
			TenantEvent.TenantCreated event) {}

		@Override
		public void handleTenantDeletedEvent(
			TenantEvent.TenantDeleted event) {}

		@Override
		public void handleTenantUpdatedEvent(
			TenantEvent.TenantUpdated event) {}
	}
}
