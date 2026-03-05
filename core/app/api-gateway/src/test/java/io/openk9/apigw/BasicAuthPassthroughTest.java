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

package io.openk9.apigw;

import static org.assertj.core.api.Assertions.assertThat;

import io.openk9.apigw.filter.SelfSignedMPJwtGlobalPreFilter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Tests the API Gateway behaviour when {@code io.openk9.apigw.reject-basic-auth}
 * is set to {@code false}.
 * <p>
 * In this mode the gateway acts as a transparent proxy for Basic Auth
 * credentials: it resolves the tenant but delegates authentication
 * entirely to the downstream services.
 */
@Testcontainers
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = "io.openk9.apigw.reject-basic-auth=false")
@ActiveProfiles("test")
class BasicAuthPassthroughTest {

	@TestConfiguration
	static class TestConfig {}

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit =
		new RabbitMQContainer(DockerImageName.parse("rabbitmq:4"))
			.withExposedPorts(5672);

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = (PostgreSQLContainer)
		new PostgreSQLContainer("postgres:17")
			.withDatabaseName("apigw")
			.waitingFor(Wait.forListeningPort());

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ApplicationContext applicationContext;

	private static final String BASIC_ADMIN =
		"Basic YWRtaW46YWRtaW4="; // admin:admin

	private static final String ALABASTA_HOST = "alabasta.localhost";
	private static final String SABAODY_HOST = "sabaody.localhost";
	private static final String UNKNOWN_HOST = "unknown.localhost";

	private static final String ALABASTA_VALID_JWT =
		"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
		+ ".eyJpc3MiOiJodHRwOi8vYWxhYmFzdGEubG9jYWxob3N0OjkwMDAi"
		+ "LCJzdWIiOiJhZG1pbiIsIm5hbWUiOiJKb2huIERvZSIsImFkbWlu"
		+ "Ijp0cnVlLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6OTk5OTk5OTk5"
		+ "OSwiZ3JvdXBzIjpbIms5LWFkbWluIl0sInRlbmFudElkIjoiYWxh"
		+ "YmFzdGEifQ.MGX1UGrM8j5E0TzTzbK8OLBClmnBjtDdzBtgKK8LGwY";

	// ---- Bean activation checks ----

	@Nested
	@DisplayName("Bean activation")
	class BeanActivation {

		@Test
		@DisplayName("SelfSignedMPJwtGlobalPreFilter should NOT be loaded")
		void jwtFilterDisabled() {
			assertThat(applicationContext.getBeansOfType(
				SelfSignedMPJwtGlobalPreFilter.class)).isEmpty();
		}
	}

	// ---- Basic Auth pass-through ----

	@Nested
	@DisplayName("Basic Auth pass-through")
	class BasicAuthPassthrough {

		@Test
		@DisplayName("GET /api/datasource/* with Basic Auth should succeed")
		void datasourceBasicAuth() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("POST /api/datasource/graphql with Basic Auth should succeed")
		void datasourceGraphqlBasicAuth() {
			webTestClient.post()
				.uri("/api/datasource/graphql")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue("{\"query\":\"{ bucket { id } }\"}")
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("/api/datasource/buckets/current with Basic Auth should succeed")
		void datasourcePublicRouteWithBasicAuth() {
			webTestClient.get()
				.uri("/api/datasource/buckets/current")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("/api/searcher/* with Basic Auth should succeed")
		void searcherBasicAuth() {
			webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Basic Auth credential is forwarded to downstream")
		void basicAuthForwardedToDownstream() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> {
					// The mock downstream echoes back the tenantId
					// resolved by the gateway.
					assertThat(body).contains("\"tenantId\":\"alabasta\"");
				});
		}
	}

	// ---- No-auth requests ----

	@Nested
	@DisplayName("No-auth requests")
	class NoAuth {

		@Test
		@DisplayName("Public route without any auth should succeed")
		void publicRouteNoAuth() {
			webTestClient.get()
				.uri("/api/datasource/buckets/current")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Protected route without any auth should succeed (permitAll)")
		void protectedRouteNoAuth() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
		}
	}

	// ---- Tenant resolution still works ----

	@Nested
	@DisplayName("Tenant resolution")
	class TenantResolution {

		@Test
		@DisplayName("Unknown host should be rejected even in basic-auth mode")
		void unknownHostRejected() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, UNKNOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> {
					// Tenant resolution fails → no tenantId
					assertThat(body).contains("\"tenantId\":\"unknown\"");
				});
		}

		@Test
		@DisplayName("Different tenants get correct tenantId header")
		void tenantIsolation() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body ->
					assertThat(body).contains("\"tenantId\":\"sabaody\""));
		}
	}

	// ---- Tampering and edge cases ----

	@Nested
	@DisplayName("Tampering and edge cases")
	class TamperingTests {

		@Test
		@DisplayName("Bearer JWT should still work (no JWT re-signing)")
		void bearerJwtPassesThrough() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Multiple Authorization headers should be rejected")
		void multipleAuthHeaders() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, BASIC_ADMIN)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Empty Authorization header should not crash")
		void emptyAuthHeader() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "")
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Malformed Basic (no space after scheme) should not crash")
		void malformedBasicNoSpace() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "Basicbadvalue")
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Basic with invalid base64 should pass through (downstream validates)")
		void basicInvalidBase64() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "Basic !!!not-base64!!!")
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Unknown authorization scheme should pass through")
		void unknownScheme() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "Custom some-token")
				.exchange()
				.expectStatus().isOk();
		}

		@Test
		@DisplayName("Basic Auth to actuator should still be accessible")
		void actuatorEndpoint() {
			webTestClient.get()
				.uri("/actuator/health")
				.exchange()
				.expectStatus().isOk();
		}
	}
}
