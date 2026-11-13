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

package io.openk9.datasource.plugindriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.openk9.datasource.client.exception.InvalidUriException;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.client.exception.UnexpectedResponseStatusException;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.client.HttpPluginDriverClient;
import io.openk9.datasource.client.exception.FormEndpointException;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.web.dto.HealthDTO;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class HttpPluginDriverClientTest {

	private static final Logger log = Logger.getLogger(HttpPluginDriverClientTest.class);

	private static final ResourceUri resourceUri = ResourceUri.builder()
		.baseUri(WireMockPluginDriver.HOST + ":" + WireMockPluginDriver.PORT)
		.build();

	// Fails on a key that appears twice in the same JSON object.
	private static final ObjectMapper STRICT_MAPPER = JsonMapper.builder()
		.enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
		.build();

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@InjectWireMock
	WireMockServer wireMockServer;

	@ConfigProperty(name = "openk9.datasource.client.plugin-driver.validation.regexes")
	List<String> regexValidations;

	@Test
	@RunOnVertxContext
	void should_invoke_success(UniAsserter asserter) {

		asserter.assertThat(
			() -> httpPluginDriverClient.invoke(
				resourceUri,
				HttpPluginDriverContext.builder().build()
			),
			res -> Assertions.assertEquals(200, res.statusCode())
		);

	}

	@Test
	@RunOnVertxContext
	void should_post_invoke_fail_when_status_code_is_not_200(UniAsserter asserter) {

		var invalidInvokeRequest = wireMockServer.stubFor(WireMock
			.post(HttpPluginDriverClient.INVOKE_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(400)
				.withStatusMessage("Invalid Request")
			)
		);

		asserter.assertFailedWith(
			() -> httpPluginDriverClient.invoke(
				resourceUri,
				HttpPluginDriverContext.builder().build()
			),
			err -> {
				Assertions.assertInstanceOf(UnexpectedResponseStatusException.class, err);
				wireMockServer.removeStub(invalidInvokeRequest);
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_invoke_with_system_fields_not_overridden_by_datasource_config(
		UniAsserter asserter) {

		// a datasource configuration with every key named as a system field
		var scheduleId = UUID.randomUUID().toString();
		var timestamp = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		var datasourceConfig = new HashMap<String, Object>();
		datasourceConfig.put("folder", "/e2e-data");
		datasourceConfig.put("datasourceId", 99);
		datasourceConfig.put("tenantId", "from-form");
		datasourceConfig.put("scheduleId", "from-form");
		datasourceConfig.put("timestamp", 99);
		datasourceConfig.put("datasourceConfig", "from-form");

		asserter.assertThat(
			() -> httpPluginDriverClient.invoke(
				resourceUri,
				HttpPluginDriverContext.builder()
					.datasourceId(1L)
					.tenantId("demo")
					.scheduleId(scheduleId)
					.timestamp(timestamp)
					.datasourceConfig(datasourceConfig)
					.build()
			),
			res -> {
				// the body the plugin driver received, without duplicated keys
				var body = wireMockServer.findAll(WireMock
						.postRequestedFor(WireMock.urlEqualTo(HttpPluginDriverClient.INVOKE_PATH))
						.withRequestBody(WireMock.containing(scheduleId)))
					.getFirst()
					.getBodyAsString();
				var json = readStrictly(body);

				// the system fields keep the values of the invocation
				Assertions.assertEquals(1, json.get("datasourceId"));
				Assertions.assertEquals("demo", json.get("tenantId"));
				Assertions.assertEquals(scheduleId, json.get("scheduleId"));
				Assertions.assertEquals(
					timestamp.toInstant().toEpochMilli(),
					((Number) json.get("timestamp")).longValue());

				// the other keys of the configuration stay at the top level
				Assertions.assertEquals("/e2e-data", json.get("folder"));

				// the whole configuration stays readable in datasourceConfig
				Assertions.assertEquals(datasourceConfig, json.get("datasourceConfig"));
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_health_up(UniAsserter asserter) throws IOException {

		HealthDTO expected;

		try (InputStream in = TestUtils.getResourceAsStream(WireMockPluginDriver.HEALTH_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), HealthDTO.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(resourceUri),
			res -> Assertions.assertEquals(expected, res)
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_health_unknown_when_response_body_is_invalid(UniAsserter asserter) {

		var invalidBodyStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.HEALTH_PATH)
			.willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(resourceUri),
			res -> {
				Assertions.assertEquals(HealthDTO.builder()
					.status(HealthDTO.Status.UNKNOWN)
					.build(), res);
				wireMockServer.removeStub(invalidBodyStub);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_health_unknown_when_response_status_is_not_200(UniAsserter asserter) {

		var invalidStatusStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.HEALTH_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(500)
				.withStatusMessage("PluginDriver internal error")
			)
		);

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(resourceUri),
			res -> {
				Assertions.assertEquals(HealthDTO.builder()
					.status(HealthDTO.Status.UNKNOWN)
					.build(), res);
				wireMockServer.removeStub(invalidStatusStub);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_sample(UniAsserter asserter) throws IOException {

		IngestionPayload expected;

		try (InputStream in = TestUtils.getResourceAsStream(WireMockPluginDriver.SAMPLE_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), IngestionPayload.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getSample(resourceUri),
			res -> {
				Assertions.assertEquals(expected, res);
				Assertions.assertTrue(res.getDatasourcePayload().containsKey("sample"));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form(UniAsserter asserter) throws IOException {

		FormTemplate expectedFormTemplate;

		try (InputStream in = TestUtils.getResourceAsStream(WireMockPluginDriver.FORM_JSON_FILE)) {
			expectedFormTemplate = Json.decodeValue(
				new String(in.readAllBytes()),
				FormTemplate.class
			);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getForm(resourceUri),
			res -> {

				if (log.isDebugEnabled()) {
					log.debugf(
						"GET /form response: \n%s", Json.encodePrettily(res));
				}

				Assertions.assertEquals(expectedFormTemplate, res);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form_fail_when_endpoint_not_found(
		UniAsserter asserter) {

		// setup: connector does not expose /form (404)
		var notFoundStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(404)
				.withStatusMessage("Not Found")
			)
		);

		asserter.assertFailedWith(
			() -> httpPluginDriverClient.getForm(resourceUri),
			err -> {
				Assertions.assertInstanceOf(
					FormEndpointException.class, err);
				Assertions.assertInstanceOf(
					RuntimeException.class,
					err.getCause());

				wireMockServer.removeStub(notFoundStub);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form_fail_when_response_is_invalid(
		UniAsserter asserter) {

		// setup: connector returns 200 with unparseable body
		var invalidBodyStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(200)
				.withHeader("Content-Type", "application/json")
				.withBody("not json")
			)
		);

		asserter.assertFailedWith(
			() -> httpPluginDriverClient.getForm(resourceUri),
			err -> {
				Assertions.assertInstanceOf(
					FormEndpointException.class, err);
				Assertions.assertInstanceOf(
					ValidationException.class,
					err.getCause());

				wireMockServer.removeStub(invalidBodyStub);
			}
		);

	}

	@Test
	void should_not_fail_when_base_uri_conforms_regex_pattern() {

		ResourceUri resourceUriTest = ResourceUri.builder()
			.baseUri("http://openk9-test-connector:5555")
			.build();

		Assertions.assertDoesNotThrow(() ->
			httpPluginDriverClient.validateBaseUri(resourceUriTest, regexValidations)
				.await()
				.indefinitely()
		);

	}

	@Test
	void should_fail_when_base_uri_does_not_conform_regex_pattern() {

		ResourceUri resourceUriTest = ResourceUri.builder()
			.baseUri("http://crawler-test:5555")
			.build();

		Assertions.assertThrowsExactly(InvalidUriException.class, () ->
			httpPluginDriverClient.validateBaseUri(resourceUriTest, regexValidations)
				.await()
				.indefinitely()
		);

	}

	@Test
	void should_fail_with_invalid_base_uri() {

		ResourceUri resourceUriTest = ResourceUri.builder()
			.baseUri("openk9.test")
			.build();

		Assertions.assertThrowsExactly(InvalidUriException.class, () ->
			httpPluginDriverClient.validateBaseUri(resourceUriTest, regexValidations)
				.await()
				.indefinitely()
		);

	}

	private static Map<String, Object> readStrictly(String body) {
		try {
			return STRICT_MAPPER.readValue(body, new TypeReference<>() {});
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}