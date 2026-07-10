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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigPackage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Removes secrets from a configuration package before it leaves the system.
 * <p>
 * Redaction is field-name driven and works generically on the JSON projection
 * of each entity's typed {@code attributes}, so no per-type code is needed:
 * <ul>
 *   <li>any {@code apiKey} field (today only {@code EmbeddingModel} and
 *       {@code LargeLanguageModel} carry one);</li>
 *   <li>any denylisted key found, at any depth, inside a {@code jsonConfig}
 *       string (the seven entities that have a {@code jsonConfig}).</li>
 * </ul>
 * Each redacted value is replaced by {@link #PLACEHOLDER} and its path recorded
 * in {@link ConfigEntity#getRedactedFields()}. Re-applying the placeholder on
 * import (so it never overwrites an existing secret) is the importer's concern.
 */
@ApplicationScoped
public class ConfigRedactor {

	public static final String PLACEHOLDER = "__REDACTED__";

	private static final String API_KEY_FIELD = "apiKey";
	private static final String JSON_CONFIG_FIELD = "jsonConfig";

	private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(
		"password", "pwd", "passphrase", "secret", "token", "apikey", "api_key",
		"accesskey", "access_key", "secretkey", "secret_key", "privatekey",
		"private_key", "credential", "credentials", "clientsecret", "client_secret"
	);

	private final ObjectMapper objectMapper;

	@Inject
	public ConfigRedactor(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void redact(ConfigPackage configPackage) {
		if (configPackage == null || configPackage.getEntities() == null) {
			return;
		}
		for (ConfigEntity entity : configPackage.getEntities()) {
			redact(entity);
		}
	}

	public void redact(ConfigEntity entity) {
		Object attributes = entity.getAttributes();
		if (attributes == null || entity.getType() == null) {
			return;
		}

		ObjectNode tree = objectMapper.valueToTree(attributes);
		List<String> redacted = new ArrayList<>();

		redactApiKey(tree, redacted);
		redactJsonConfig(tree, redacted);

		if (redacted.isEmpty()) {
			return;
		}

		entity.setAttributes(bindBack(tree, entity.getType().getAttributesType()));

		List<String> redactedFields = entity.getRedactedFields() == null
			? new ArrayList<>()
			: new ArrayList<>(entity.getRedactedFields());
		redactedFields.addAll(redacted);
		entity.setRedactedFields(redactedFields);
	}

	private Object bindBack(ObjectNode tree, Class<?> attributesType) {
		try {
			return objectMapper.treeToValue(tree, attributesType);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(
				"Unable to rebind redacted attributes to " + attributesType.getSimpleName(), e);
		}
	}

	private void redactJsonConfig(ObjectNode tree, List<String> redacted) {
		JsonNode jsonConfig = tree.get(JSON_CONFIG_FIELD);
		if (jsonConfig == null || !jsonConfig.isTextual()) {
			return;
		}

		JsonNode parsed = tryParse(jsonConfig.asText());
		if (parsed == null || !parsed.isObject()) {
			return;
		}

		List<String> keys = new ArrayList<>();
		redactObject((ObjectNode) parsed, "", keys);

		if (!keys.isEmpty()) {
			tree.put(JSON_CONFIG_FIELD, writeString(parsed));
			for (String key : keys) {
				redacted.add(JSON_CONFIG_FIELD + "." + key);
			}
		}
	}

	private void redactNode(JsonNode node, String path, List<String> redacted) {
		if (node.isObject()) {
			redactObject((ObjectNode) node, path, redacted);
		}
		else if (node.isArray()) {
			ArrayNode array = (ArrayNode) node;
			for (int i = 0; i < array.size(); i++) {
				redactNode(array.get(i), path + "[" + i + "]", redacted);
			}
		}
	}

	private void redactObject(ObjectNode node, String prefix, List<String> redacted) {
		List<String> fieldNames = new ArrayList<>();
		node.fieldNames().forEachRemaining(fieldNames::add);

		for (String name : fieldNames) {
			String path = prefix.isEmpty() ? name : prefix + "." + name;
			if (SENSITIVE_JSON_KEYS.contains(name.toLowerCase(Locale.ROOT))) {
				node.put(name, PLACEHOLDER);
				redacted.add(path);
			}
			else {
				redactNode(node.get(name), path, redacted);
			}
		}
	}

	private JsonNode tryParse(String json) {
		try {
			return objectMapper.readTree(json);
		}
		catch (JsonProcessingException e) {
			return null;
		}
	}

	private String writeString(JsonNode node) {
		try {
			return objectMapper.writeValueAsString(node);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Unable to re-serialize redacted jsonConfig", e);
		}
	}

	private static void redactApiKey(ObjectNode tree, List<String> redacted) {
		JsonNode apiKey = tree.get(API_KEY_FIELD);
		if (apiKey != null && apiKey.isTextual()) {
			tree.put(API_KEY_FIELD, PLACEHOLDER);
			redacted.add(API_KEY_FIELD);
		}
	}

}
