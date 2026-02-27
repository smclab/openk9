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

import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.AuthorizationScheme;
import io.openk9.event.tenant.TenantEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class TenantManagementEventMessageConverterTest {

	private TenantManagementEventMessageConverter converter;
	private ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		converter = new TenantManagementEventMessageConverter(mapper);
	}

	@Test
	@DisplayName("Should transform legacy TenantCreated event (with DATASOURCE)")
	void testLegacyTenantCreatedUpcasting() {
		String legacyJson = """
			{
				"tenantId": "legacy-tenant",
				"hostName": "legacy.localhost",
				"schemaName": "legacy_tenant",
				"routeAuthorizationMap": {
					"SEARCH": "API_KEY",
					"DATASOURCE": "OAUTH2"
				}
			}
			""";

		MessageProperties props = new MessageProperties();
		props.setHeader("x-event-type", "TenantCreated");
		Message message = new Message(legacyJson.getBytes(), props);

		Object result = converter.fromMessage(message);

		assertThat(result).isInstanceOf(TenantEvent.TenantCreated.class);
		TenantEvent.TenantCreated event = (TenantEvent.TenantCreated) result;

		assertThat(event.tenantId()).isEqualTo("legacy-tenant");
		
		// mapping: ADMINISTRATION = OAUTH2, PUBLIC = SEARCH (API_KEY), INGESTION = SEARCH (API_KEY)
		assertThat(event.routeAuthorizationMap())
			.containsEntry(ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2)
			.containsEntry(ApiGroup.SEARCH, AuthorizationScheme.API_KEY)
			.containsEntry(ApiGroup.PUBLIC, AuthorizationScheme.API_KEY)
			.containsEntry(ApiGroup.INGESTION, AuthorizationScheme.API_KEY)
			.hasSize(4);
	}

	@Test
	@DisplayName("Should ignore legacy ApiKeyCreated event (missing apiGroup or expirationDate)")
	void testLegacyApiKeyCreatedFiltering() {
		String legacyJson = """
			{
				"tenantId": "legacy-tenant",
				"apiKeyHash": "some-hash",
				"checksum": "abc"
			}
			""";

		MessageProperties props = new MessageProperties();
		props.setHeader("x-event-type", "ApiKeyCreated");
		Message message = new Message(legacyJson.getBytes(), props);

		Object result = converter.fromMessage(message);

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Should deserialize modern TenantCreated event correctly")
	void testModernTenantCreatedDeserialization() {
		String modernJson = """
			{
				"tenantId": "modern-tenant",
				"hostName": "modern.localhost",
				"schemaName": "modern_tenant",
				"issuerUri": "http://keycloak/realms/modern",
				"routeAuthorizationMap": {
					"ADMINISTRATION": "OAUTH2",
					"SEARCH": "OAUTH2",
					"INGESTION": "OAUTH2",
					"PUBLIC": "NO_AUTH"
				}
			}
			""";

		MessageProperties props = new MessageProperties();
		props.setHeader("x-event-type", "TenantCreated");
		Message message = new Message(modernJson.getBytes(), props);

		Object result = converter.fromMessage(message);

		assertThat(result).isInstanceOf(TenantEvent.TenantCreated.class);
		TenantEvent.TenantCreated event = (TenantEvent.TenantCreated) result;
		assertThat(event.tenantId()).isEqualTo("modern-tenant");
		assertThat(event.routeAuthorizationMap()).hasSize(4);
	}
}
