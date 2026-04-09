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

package io.openk9.k8sclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KubernetesObjectMapperCustomizerTest {

	// Verifies that the customizer disables FAIL_ON_UNKNOWN_PROPERTIES,
	// which prevents Jackson from rejecting ArgoCD responses that include
	// fields not modeled in the generated CRD classes (e.g. "operation").
	@Test
	void customize_disablesFailOnUnknownProperties() {
		var objectMapper = new ObjectMapper();

		Assertions.assertTrue(
			objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
			"Precondition: default ObjectMapper should fail on unknown properties"
		);

		new KubernetesObjectMapperCustomizer().customize(objectMapper);

		Assertions.assertFalse(
			objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
			"After customization, unknown properties should be ignored"
		);
	}

	@Test
	void customize_allowsDeserializationWithUnknownFields() throws Exception {
		var objectMapper = new ObjectMapper();
		new KubernetesObjectMapperCustomizer().customize(objectMapper);

		// JSON with an unknown field "operation" — simulates ArgoCD response
		String json = """
			{"name": "test-app", "operation": {"sync": {}}}""";

		Assertions.assertDoesNotThrow(
			() -> objectMapper.readValue(json, SimpleResource.class),
			"Deserialization should succeed despite unknown 'operation' field"
		);

		var result = objectMapper.readValue(json, SimpleResource.class);
		Assertions.assertEquals("test-app", result.name);
	}

	static class SimpleResource {

		public String name;

	}

}
