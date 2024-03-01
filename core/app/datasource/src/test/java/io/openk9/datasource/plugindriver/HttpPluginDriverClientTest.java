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

import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.form.PluginDriverFormDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class HttpPluginDriverClientTest {

	private static final HttpPluginDriverInfo pluginDriverInfo = HttpPluginDriverInfo.builder()
		.host(WireMockPluginDriver.HOST)
		.port(WireMockPluginDriver.PORT)
		.build();

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Test
	@RunOnVertxContext
	void should_invoke(UniAsserter asserter) {

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
	void should_get_health_up(UniAsserter asserter) throws IOException {

		PluginDriverHealthDTO expected;

		try (InputStream in = getResourceAsStream(WireMockPluginDriver.HEALTH_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), PluginDriverHealthDTO.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getHealth(pluginDriverInfo),
			res -> Assertions.assertEquals(expected, res)
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_sample(UniAsserter asserter) throws IOException {

		Buffer expected;

		try (InputStream in = getResourceAsStream(WireMockPluginDriver.SAMPLE_JSON_FILE)) {
			expected = Buffer.buffer(in.readAllBytes());
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getSample(pluginDriverInfo),
			res -> Assertions.assertEquals(expected, res.body())
		);

	}

	@Test
	@RunOnVertxContext
	void should_get_form(UniAsserter asserter) throws IOException {

		PluginDriverFormDTO expected;

		try (InputStream in = getResourceAsStream(WireMockPluginDriver.FORM_JSON_FILE)) {
			expected = Json.decodeValue(new String(in.readAllBytes()), PluginDriverFormDTO.class);
		}

		asserter.assertThat(
			() -> httpPluginDriverClient.getForm(pluginDriverInfo),
			res -> Assertions.assertEquals(expected, res)
		);

	}

	private static InputStream getResourceAsStream(String path) {
		return HttpPluginDriverClientTest.class
			.getClassLoader()
			.getResourceAsStream(path);
	}

}