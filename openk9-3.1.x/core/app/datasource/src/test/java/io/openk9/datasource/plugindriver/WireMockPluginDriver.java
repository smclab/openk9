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

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import io.openk9.datasource.TestUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WireMockPluginDriver implements QuarkusTestResourceLifecycleManager {

	public static final String HEALTH_JSON_FILE = "/plugindriver/health.json";
	public static final String SAMPLE_JSON_FILE = "/plugindriver/sample.json";
	public static final String FORM_JSON_FILE = "/plugindriver/form.json";
	public static final int PORT = 7777;
	public static final String HOST = "localhost";

	private WireMockServer wireMockServer;

	@Override
	public Map<String, String> start() {
		wireMockServer = new WireMockServer(PORT);

		wireMockServer.start();

		stubHealth();
		stubSample();
		stubForm();
		stubInvoke();

		return Map.of();
	}

	@Override
	public void stop() {
		if (Objects.nonNull(wireMockServer)) {
			wireMockServer.stop();
		}
	}

	@Override
	public void inject(TestInjector testInjector) {
		testInjector.injectIntoFields(
			wireMockServer,
			new TestInjector.AnnotatedAndMatchesType(
				InjectWireMock.class,
				WireMockServer.class
			)
		);
	}

	private void stubInvoke() {
		wireMockServer.stubFor(WireMock
			.post(urlEqualTo(HttpPluginDriverClient.INVOKE_PATH))
			.willReturn(ok())
		);
	}

	private void stubForm() {
		try (InputStream is = TestUtils.getResourceAsStream(FORM_JSON_FILE)) {
			String form = new String(is.readAllBytes());

			wireMockServer.stubFor(WireMock
				.get(urlEqualTo(HttpPluginDriverClient.FORM_PATH))
				.willReturn(okJson(form))
			);

		}
		catch (IOException e) {
			fail("Could not configure Wiremock server. Caused by: " + e.getMessage());
		}
	}

	private void stubSample() {
		try (InputStream is = TestUtils.getResourceAsStream(SAMPLE_JSON_FILE)) {
			String sample = new String(is.readAllBytes());

			wireMockServer.stubFor(WireMock
				.get(urlEqualTo(HttpPluginDriverClient.SAMPLE_PATH))
				.willReturn(okJson(sample))
			);

		}
		catch (IOException e) {
			fail("Could not configure Wiremock server. Caused by: " + e.getMessage());
		}
	}

	private void stubHealth() {
		try (InputStream is = TestUtils.getResourceAsStream(HEALTH_JSON_FILE)) {
			String health = new String(is.readAllBytes());

			wireMockServer.stubFor(WireMock
				.get(urlEqualTo(HttpPluginDriverClient.HEALTH_PATH))
				.willReturn(okJson(health))
			);

		}
		catch (IOException e) {
			fail("Could not configure Wiremock server. Caused by: " + e.getMessage());
		}
	}

}
