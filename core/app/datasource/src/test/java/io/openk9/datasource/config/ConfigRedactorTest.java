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

import java.util.Map;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.RuleDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests (no Quarkus, no Docker) for secret redaction: the known
 * {@code apiKey} field and denylisted keys nested in {@code jsonConfig} are
 * replaced by the placeholder and recorded, while non-secret values and
 * secret-free entities are left untouched.
 */
class ConfigRedactorTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ConfigRedactor redactor = new ConfigRedactor(objectMapper);

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

	private static ConfigEntity entity(ConfigEntityType type, Object attributes) {
		return new ConfigEntity("ref-1", type, "key", attributes, Map.of(), null);
	}

}
