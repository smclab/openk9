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

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiGatewaySecurityTest {

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

    @LocalServerPort
    private int port;

	// credentials
    private static final String INVALID_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vdW5rbm93bklzc3Vlci5sb2NhbGhvc3QiLCJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.azSxTv3dA4-360UsoOwxtHkJaVcGYdM_y8YcJbckNRI";
	private static final String ALABASTA_VALID_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vYWxhYmFzdGEubG9jYWxob3N0OjkwMDAiLCJzdWIiOiJhZG1pbiIsIm5hbWUiOiJKb2huIERvZSIsImFkbWluIjp0cnVlLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6OTk5OTk5OTk5OSwiZ3JvdXBzIjpbIms5LWFkbWluIl0sInRlbmFudElkIjoiYWxhYmFzdGEifQ.MGX1UGrM8j5E0TzTzbK8OLBClmnBjtDdzBtgKK8LGwY";

	private static final String INVALID_API_KEY = "ApiKey sk_9a6ef1042afe82404b60a6ffc6f9f265bc827114a6fbea9bcd8935e6d7efb2a3_a54f5667";
	private static final String ALABASTA_VALID_API_KEY = "ApiKey sk_f5b4136fd3449f94f7d60b11180bd7b63eef073b84e3b34d6098b0c71f3cc930_a9d05b90";
	private static final String SABAODY_VALID_API_KEY = "ApiKey sk_814f53a1a84a67dbe128ba2b072fbb268bf84b04234ea286aa237262c33f38fa_bd0a32e2";
	private static final String LOGUETOWN_VALID_API_KEY = "ApiKey sk_e5d2dc591d339bd6dbc26c9e63ee79e1ff97de58f1a17803feba35f2ac3e1efb_4679e9c9";

	// virtual hosts
    private static final String ALABASTA_HOST = "alabasta.localhost";
    private static final String SABAODY_HOST = "sabaody.localhost";
    private static final String LOGUETOWN_HOST = "loguetown.localhost";
    private static final String UNKNOWN_HOST = "unknown.localhost";

    @Nested
    @DisplayName("Alabasta Tenant Security Tests")
    class AlabastaTenantTests {

		@Test
		@DisplayName("DatasourcePublic route without authorization header should succeed")
		void testDatasourcePublicValid() {
			webTestClient.get()
				.uri("/api/datasource/buckets/current")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
		}

		@Test
        @DisplayName("Datasource route with valid OAuth2 token should succeed")
        void testDatasourceWithValidOAuth2() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Datasource route with invalid OAuth2 token should fail")
        void testDatasourceWithInvalidOAuth2() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, INVALID_JWT_TOKEN)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Datasource route without OAuth2 token should fail")
        void testDatasourceWithoutOAuth2() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Datasource route with API key instead of OAuth2 should fail")
        void testDatasourceWithApiKeyInsteadOfOAuth2() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY)
				.exchange()
				.expectStatus().isForbidden();
        }

        @Test
        @DisplayName("Searcher route with valid API key should succeed")
        void testSearcherWithValidApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Searcher route with invalid API key should fail")
        void testSearcherWithInvalidApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, INVALID_API_KEY)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Searcher route without API key should fail")
        void testSearcherWithoutApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Searcher route with OAuth2 token instead of API key should fail")
        void testSearcherWithOAuth2InsteadOfApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.exchange()
				.expectStatus().isForbidden();
        }

        @Test
        @DisplayName("Undefined route should allow no-auth access")
        void testUndefinedRouteNoAuth() {
            webTestClient.get()
				.uri("/undefined/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Root route should allow no-auth access")
        void testRootRouteNoAuth() {
            webTestClient.get()
				.uri("/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Sabaody Tenant Security Tests")
    class SabaodyTenantTests {

        @Test
        @DisplayName("Datasource route with valid API key should succeed")
        void testDatasourceWithValidApiKey() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, SABAODY_VALID_API_KEY)
				.exchange()
				.expectAll(
					res -> res.expectStatus().isOk(),
					res -> res.expectBody().jsonPath("$.tenantId").isEqualTo("sabaody")
				);
        }

        @Test
        @DisplayName("Datasource route with invalid API key should fail")
        void testDatasourceWithInvalidApiKey() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, INVALID_API_KEY)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Datasource route without API key should fail")
        void testDatasourceWithoutApiKey() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Searcher route with valid API key should succeed")
        void testSearcherWithValidApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, SABAODY_VALID_API_KEY)
				.exchange()
				.expectAll(
					res -> res.expectStatus().isOk(),
					res -> res.expectBody().jsonPath("$.tenantId").isEqualTo("sabaody")
				);
        }

        @Test
        @DisplayName("Searcher route with invalid API key should fail")
        void testSearcherWithInvalidApiKey() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, INVALID_API_KEY)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Undefined route should allow no-auth access")
        void testUndefinedRouteNoAuth() {
            webTestClient.get()
				.uri("/undefined/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.exchange()
				.expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Loguetown Tenant Security Tests")
    class LoguetownTenantTests {

        @Test
        @DisplayName("Datasource route with valid API key should succeed")
        void testDatasourceWithValidApiKey() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, LOGUETOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, LOGUETOWN_VALID_API_KEY)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Datasource route with invalid API key should fail")
        void testDatasourceWithInvalidApiKey() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, LOGUETOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, INVALID_API_KEY)
				.exchange()
				.expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Searcher route should allow no-auth access (not configured)")
        void testSearcherNoAuth() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, LOGUETOWN_HOST)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Undefined route should allow no-auth access")
        void testUndefinedRouteNoAuth() {
            webTestClient.get()
				.uri("/undefined/test")
				.header(HttpHeaders.HOST, LOGUETOWN_HOST)
				.exchange()
				.expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Security Header Conflict Tests")
    class SecurityHeaderConflictTests {
		@Test
		@DisplayName("Authorization header scheme validation - Bearer vs ApiKey")
		void testAuthorizationSchemeValidation() {
			// Test JWT where API key is expected
			webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN) // Bearer scheme
				.exchange()
				.expectStatus().isForbidden(); // Should expect ApiKey scheme

			// Test API key where JWT is expected
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY) // ApiKey scheme
				.exchange()
				.expectStatus().isForbidden(); // Should expect Bearer scheme
		}

		@Test
		@DisplayName("Invalid authorization scheme should be rejected")
		void testInvalidAuthorizationScheme() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "Custom some-token")
				.exchange()
				.expectStatus().isUnauthorized();
		}

		@Test
		@DisplayName("Malformed authorization header should be rejected")
		void testMalformedAuthorizationHeader() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, "InvalidFormat")
				.exchange()
				.expectStatus().isUnauthorized();
		}

		@Test
		@DisplayName("Empty authorization scheme should be rejected")
		void testEmptyAuthorizationScheme() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, "ApiKey ")
				.exchange()
				.expectStatus().isBadRequest();
		}

        @Test
        @DisplayName("Alabasta datasource with multiple authorization header should be rejected")
        void testAlabastaDatasourceWithBothHeaders() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY)
				.exchange()
				.expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Alabasta searcher with multiple authorization header should be rejected")
        void testAlabastaSearcherWithBothHeaders() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY)
				.exchange()
				.expectStatus().isBadRequest();
        }

    }

    @Nested
    @DisplayName("Unknown Tenant Tests")
    class UnknownTenantTests {

        @Test
        @DisplayName("Unknown tenant should reject all requests")
        void testUnknownTenantDatasource() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, UNKNOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY) // any valid API key
				.exchange()
				.expectStatus()
				.isUnauthorized();
        }

        @Test
        @DisplayName("Unknown tenant searcher should reject requests")
        void testUnknownTenantSearcher() {
            webTestClient.get()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, UNKNOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN) // any valid JWT
				.exchange()
				.expectStatus()
				.isUnauthorized();
        }

    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("POST request to Alabasta datasource with OAuth2")
        void testPostRequestAlabastaDatasource() {
            webTestClient.post()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue("{\"test\": \"data\"}")
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("PUT request to Sabaody searcher with API key")
        void testPutRequestSabaodySearcher() {
            webTestClient.put()
				.uri("/api/searcher/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, SABAODY_VALID_API_KEY)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue("{\"update\": \"data\"}")
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("DELETE request to Loguetown datasource with API key")
        void testDeleteRequestLoguetownDatasource() {
            webTestClient.delete()
				.uri("/api/datasource/test/123")
				.header(HttpHeaders.HOST, LOGUETOWN_HOST)
				.header(HttpHeaders.AUTHORIZATION, LOGUETOWN_VALID_API_KEY)
				.exchange()
				.expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Route Precedence Tests")
    class RoutePrecedenceTests {

        @Test
        @DisplayName("Datasource nested path should use datasource security")
        void testDatasourceNestedPath() {
            webTestClient.get()
				.uri("/api/datasource/nested/deep/path")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Searcher nested path should use searcher security")
        void testSearcherNestedPath() {
            webTestClient.get()
				.uri("/api/searcher/nested/deep/path")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_API_KEY)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Path that starts with datasource but longer should use catch-all")
        void testDatasourceLikePath() {
            webTestClient.get()
				.uri("/api/datasources/test") // Note the 's' at the end
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk(); // Should use no-auth catch-all
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty path should use catch-all no-auth")
        void testEmptyPath() {
            webTestClient.get()
				.uri("/")
				.header(HttpHeaders.HOST, ALABASTA_HOST)
				.exchange()
				.expectStatus().isOk();
        }

        @Test
        @DisplayName("Empty API key header should fail")
        void testEmptyApiKeyHeader() {
            webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, "")
				.exchange()
				.expectStatus().isUnauthorized();
        }

		@Test
		@DisplayName("Invalid checksum API key header should fail")
		void testInvalidChecksumApiKeyHeader() {
			webTestClient.get()
				.uri("/api/datasource/test")
				.header(HttpHeaders.HOST, SABAODY_HOST)
				.header(HttpHeaders.AUTHORIZATION, "ApiKey sk_9a6efxxxxxxxxx404b60a6ffc6f9f265bc827114a6fbea9bcd8935e6d7efb2a3_a54f5667")
				.exchange()
				.expectStatus().isBadRequest();
		}

	}

    @Nested
    @DisplayName("Performance and Load Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Multiple concurrent requests to same tenant")
        void testConcurrentRequestsSameTenant() {
            // This test would benefit from parallel execution
            for (int i = 0; i < 10; i++) {
                webTestClient.get()
					.uri("/api/datasource/test" + i)
					.header(HttpHeaders.HOST, ALABASTA_HOST)
					.header(HttpHeaders.AUTHORIZATION, ALABASTA_VALID_JWT_TOKEN)
					.exchange()
					.expectStatus().isOk();
            }
        }

        @Test
        @DisplayName("Switching between different tenants quickly")
        void testTenantSwitching() {
            List<Tuple2<String, String>> confs = List.of(
				Tuples.of(ALABASTA_HOST, ALABASTA_VALID_JWT_TOKEN),
				Tuples.of(SABAODY_HOST, SABAODY_VALID_API_KEY),
				Tuples.of(LOGUETOWN_HOST, LOGUETOWN_VALID_API_KEY)
			);

            for (Tuple2<String, String> conf : confs) {
                webTestClient.get()
					.uri("/api/datasource/test")
					.header(HttpHeaders.HOST, conf.getT1())
					.header(HttpHeaders.AUTHORIZATION, conf.getT2())
					.exchange()
					.expectStatus().isOk();
            }
        }
    }
}