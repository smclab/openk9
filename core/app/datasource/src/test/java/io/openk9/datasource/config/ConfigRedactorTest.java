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

package io.openk9.datasource.config;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.RuleDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for secret redaction. Boots Quarkus so the CDI-managed
 * {@link ConfigRedactor} is wired with the jsonConfig denylist sourced from
 * {@code application.properties}
 * ({@code openk9.datasource.config.redaction.keys}): the known {@code apiKey}
 * field and denylisted keys nested in {@code jsonConfig} are replaced by the
 * placeholder and recorded, while non-secret values and secret-free entities
 * are left untouched.
 */
@QuarkusTest
class ConfigRedactorTest {

	@Inject
	ObjectMapper objectMapper;

	@Inject
	ConfigRedactor redactor;

	@ConfigProperty(name = "openk9.datasource.config.redaction.keys")
	List<String> redactionKeys;

	@Test
	void shouldSourceDenylistFromApplicationProperties() {
		// 1. the denylist is loaded from application.properties, not hardcoded
		assertTrue(redactionKeys.contains("password"));
		assertTrue(redactionKeys.contains("client_secret"));

		// 2. the CDI-wired redactor redacts a base key drawn from that list
		DatasourceDTO datasource = DatasourceDTO.builder()
			.name("props-connector")
			.jsonConfig("{\"url\":\"https://x\",\"password\":\"p\"}")
			.build();
		ConfigEntity entity = entity(ConfigEntityType.DATASOURCE, datasource);

		redactor.redact(entity);

		assertTrue(entity.getRedactedFields().contains("jsonConfig.password"));
	}

	@Test
	void shouldRedactApiKey() {
		EmbeddingModelDTO model = EmbeddingModelDTO.builder()
			.name("openai-embeddings")
			.apiUrl("https://api.openai.com")
			.apiKey("sk-super-secret")
			.vectorSize(1536)
			.build();

		ConfigEntity entity = entity(ConfigEntityType.EMBEDDING_MODEL, model);

		redactor.redact(entity);

		EmbeddingModelDTO redacted =
			assertInstanceOf(EmbeddingModelDTO.class, entity.getAttributes());
		assertEquals(ConfigRedactor.PLACEHOLDER, redacted.getApiKey());
		assertEquals("https://api.openai.com", redacted.getApiUrl());
		assertTrue(entity.getRedactedFields().contains("apiKey"));
	}

	@Test
	void shouldRedactDenylistedKeysInsideJsonConfigRecursively() throws Exception {
		DatasourceDTO datasource = DatasourceDTO.builder()
			.name("github-connector")
			.jsonConfig("{\"url\":\"https://x\",\"password\":\"p\","
				+ "\"auth\":{\"token\":\"t\"}}")
			.build();

		ConfigEntity entity = entity(ConfigEntityType.DATASOURCE, datasource);

		redactor.redact(entity);

		DatasourceDTO redacted =
			assertInstanceOf(DatasourceDTO.class, entity.getAttributes());
		JsonNode jsonConfig = objectMapper.readTree(redacted.getJsonConfig());

		assertEquals("https://x", jsonConfig.get("url").asText());
		assertEquals(ConfigRedactor.PLACEHOLDER, jsonConfig.get("password").asText());
		assertEquals(
			ConfigRedactor.PLACEHOLDER, jsonConfig.get("auth").get("token").asText());

		assertTrue(entity.getRedactedFields().contains("jsonConfig.password"));
		assertTrue(entity.getRedactedFields().contains("jsonConfig.auth.token"));
	}

	@Test
	void shouldLeaveSecretFreeEntityUntouched() {
		RuleDTO rule = RuleDTO.builder()
			.name("a-rule")
			.lhs("lhs")
			.rhs("rhs")
			.build();

		ConfigEntity entity = entity(ConfigEntityType.RULE, rule);

		redactor.redact(entity);

		assertNull(entity.getRedactedFields());
	}

	@Test
	void shouldRedactKeySuppliedOnlyViaConfig() throws Exception {
		// a redactor whose denylist adds a custom key absent from the base list
		ConfigRedactor custom = new ConfigRedactor();
		custom.objectMapper = objectMapper;
		custom.redactionKeys = List.of("password", "x_api_token");

		DatasourceDTO datasource = DatasourceDTO.builder()
			.name("custom-connector")
			.jsonConfig("{\"url\":\"https://x\",\"password\":\"p\","
				+ "\"x_api_token\":\"t\"}")
			.build();

		ConfigEntity entity = entity(ConfigEntityType.DATASOURCE, datasource);

		// redact with the config-driven denylist
		custom.redact(entity);

		DatasourceDTO redacted =
			assertInstanceOf(DatasourceDTO.class, entity.getAttributes());
		JsonNode jsonConfig = objectMapper.readTree(redacted.getJsonConfig());

		// both the base key and the config-only key are redacted, others untouched
		assertEquals("https://x", jsonConfig.get("url").asText());
		assertEquals(ConfigRedactor.PLACEHOLDER, jsonConfig.get("password").asText());
		assertEquals(
			ConfigRedactor.PLACEHOLDER, jsonConfig.get("x_api_token").asText());

		assertTrue(entity.getRedactedFields().contains("jsonConfig.password"));
		assertTrue(entity.getRedactedFields().contains("jsonConfig.x_api_token"));
	}

	private static ConfigEntity entity(ConfigEntityType type, Object attributes) {
		return new ConfigEntity("ref-1", type, "key", attributes, Map.of(), null);
	}

}
