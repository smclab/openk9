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

package io.openk9.datasource.model.init;

import java.util.regex.Pattern;

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PluginDriversTest {

	// RFC 1123 defines valid DNS subdomain names: lowercase alphanumeric and
	// hyphens, must start and end with an alphanumeric character.
	// Kubernetes enforces this same rule on resource metadata.name fields,
	// so preset values must comply to be usable as K8s resource names.
	private static final Pattern RFC_1123_PATTERN =
		Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*");

	// Verifies that PresetPluginDrivers values do not contain protocol
	// prefixes (e.g. "http://") and are valid K8s resource name components.
	@ParameterizedTest
	@EnumSource(value = Preset.class, names = "UNRECOGNIZED", mode = EnumSource.Mode.EXCLUDE)
	void presetPluginDriverValues_areRfc1123Compliant(Preset preset) {
		String value = PresetPluginDrivers.getPluginDriver(preset);

		Assertions.assertNotNull(value, "Preset " + preset + " has no mapping");
		Assertions.assertFalse(
			value.contains("://"),
			"Preset " + preset + " value must not contain a protocol prefix, got: " + value
		);
		Assertions.assertTrue(
			RFC_1123_PATTERN.matcher(value).matches(),
			"Preset " + preset + " value is not RFC 1123 compliant: " + value
		);
	}

	// The protocol is stripped from PresetPluginDrivers (for K8s naming)
	// but PluginDrivers must re-add it when building HTTP resource URIs.
	@Test
	void pluginDriverDTO_resourceUri_includesProtocolAndPort() {
		var dto = PluginDrivers.getPluginDriverDTO("luxio", Preset.CRAWLER);

		String baseUri = dto.getResourceUri().getBaseUri();

		Assertions.assertTrue(
			baseUri.startsWith("http://"),
			"Resource URI must start with http://, got: " + baseUri
		);
		Assertions.assertTrue(
			baseUri.endsWith(":5000"),
			"Resource URI must end with port :5000, got: " + baseUri
		);
		Assertions.assertEquals(
			"http://openk9-web-connector-luxio:5000",
			baseUri
		);
	}

	@Test
	void pluginDriverDTO_withoutSchemaName_doesNotAppendTenant() {
		var dto = PluginDrivers.getPluginDriverDTO(Preset.CRAWLER);

		String baseUri = dto.getResourceUri().getBaseUri();

		Assertions.assertEquals("http://openk9-web-connector:5000", baseUri);
	}

}
