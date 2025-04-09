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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.openk9.datasource.TestUtils;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.form.FormField;
import io.openk9.datasource.web.dto.form.FormFieldValidator;
import io.openk9.datasource.web.dto.form.FormFieldValue;
import io.openk9.datasource.web.dto.form.FormType;
import io.openk9.datasource.web.dto.form.PluginDriverFormDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.core.json.Json;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class HttpPluginDriverClientTest {

	private static final HttpPluginDriverInfo pluginDriverInfo = HttpPluginDriverInfo.builder()
		.host(WireMockPluginDriver.HOST)
		.port(WireMockPluginDriver.PORT)
		.build();

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@InjectWireMock
	WireMockServer wireMockServer;

	@Test
	@RunOnVertxContext
	void should_invoke_success(UniAsserter asserter) {

		asserter.assertThat(
			() -> httpPluginDriverClient.invoke(
				pluginDriverInfo,
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
				pluginDriverInfo,
				HttpPluginDriverContext.builder().build()
			),
			err -> {
				Assertions.assertInstanceOf(ValidationException.class, err);
				wireMockServer.removeStub(invalidInvokeRequest);
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_health_up(UniAsserter asserter) throws IOException {

		PluginDriverHealthDTO expected;

		try (InputStream in = TestUtils.getResourceAsStream(WireMockPluginDriver.HEALTH_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), PluginDriverHealthDTO.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(pluginDriverInfo),
			res -> Assertions.assertEquals(expected, res)
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_health_unkonwn_when_response_body_is_invalid(UniAsserter asserter) {

		var invalidBodyStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.HEALTH_PATH)
			.willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(pluginDriverInfo),
			res -> {
				Assertions.assertEquals(PluginDriverHealthDTO.builder()
					.status(PluginDriverHealthDTO.Status.UNKOWN)
					.build(), res);
				wireMockServer.removeStub(invalidBodyStub);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_health_fail_when_response_status_is_not_200(UniAsserter asserter) {

		var invalidStatusStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.HEALTH_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(500)
				.withStatusMessage("PluginDriver internal error")
			)
		);

		asserter.assertFailedWith(
			() -> httpPluginDriverClient.getHealth(pluginDriverInfo),
			err -> {
				Assertions.assertInstanceOf(ValidationException.class, err);
				Assertions.assertTrue(err.getMessage().contains("Unexpected Response"));
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
			() -> httpPluginDriverClient.getSample(pluginDriverInfo),
			res -> {
				Assertions.assertEquals(expected, res);
				Assertions.assertTrue(res.getDatasourcePayload().containsKey("sample"));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form(UniAsserter asserter) throws IOException {

		PluginDriverFormDTO expected;

		try (InputStream in = TestUtils.getResourceAsStream(WireMockPluginDriver.FORM_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), PluginDriverFormDTO.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getForm(pluginDriverInfo),
			res -> {
				Assertions.assertEquals(expected, res);
				Assertions.assertTrue(res
					.fields()
					.contains(FormField.builder()
						.label("Title tag")
						.name("titleTag")
						.type(FormType.TEXT)
						.size(10)
						.required(true)
						.info("")
						.value(
							FormFieldValue.builder()
								.value("title::text")
								.isDefault(true)
								.build()
						)
						.validator(FormFieldValidator.builder()
							.min(0)
							.max(100)
							.regex("/[[:alnum:]]+/")
							.build())
						.build())
				);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form_fail_when_response_status_is_not_200(UniAsserter asserter) {

		var invalidStatusStub = wireMockServer.stubFor(WireMock
			.get(HttpPluginDriverClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(500)
				.withStatusMessage("PluginDriver internal error")
			)
		);

		asserter.assertFailedWith(
			() -> httpPluginDriverClient.getForm(pluginDriverInfo),
			err -> {
				Assertions.assertInstanceOf(ValidationException.class, err);
				Assertions.assertTrue(err.getMessage().contains("Unexpected Response"));
				wireMockServer.removeStub(invalidStatusStub);
			}
		);

	}

}