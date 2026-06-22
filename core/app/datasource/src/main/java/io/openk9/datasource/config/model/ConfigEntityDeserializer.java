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

package io.openk9.datasource.config.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Binds {@link ConfigEntity#getAttributes()} to the concrete DTO class declared
 * by {@link ConfigEntityType}, so the deserialized package is genuinely typed
 * (ADR-0003 §3c) without forcing the heterogeneous DTOs into a common hierarchy.
 */
public class ConfigEntityDeserializer extends JsonDeserializer<ConfigEntity> {

	private static final TypeReference<Map<String, List<String>>> REFERENCES_TYPE =
		new TypeReference<>() {};
	private static final TypeReference<List<String>> REDACTED_FIELDS_TYPE =
		new TypeReference<>() {};

	@Override
	public ConfigEntity deserialize(JsonParser parser, DeserializationContext context)
		throws IOException {

		ObjectMapper mapper = (ObjectMapper) parser.getCodec();
		JsonNode node = mapper.readTree(parser);

		ConfigEntity entity = new ConfigEntity();

		if (node.hasNonNull("ref")) {
			entity.setRef(node.get("ref").asText());
		}

		if (node.hasNonNull("key")) {
			entity.setKey(node.get("key").asText());
		}

		ConfigEntityType type = null;
		if (node.hasNonNull("type")) {
			type = ConfigEntityType.valueOf(node.get("type").asText());
			entity.setType(type);
		}

		JsonNode attributesNode = node.get("attributes");
		if (type != null && attributesNode != null && !attributesNode.isNull()) {
			entity.setAttributes(
				mapper.treeToValue(attributesNode, type.getAttributesType()));
		}

		JsonNode referencesNode = node.get("references");
		if (referencesNode != null && !referencesNode.isNull()) {
			entity.setReferences(mapper.convertValue(referencesNode, REFERENCES_TYPE));
		}

		JsonNode redactedNode = node.get("redactedFields");
		if (redactedNode != null && !redactedNode.isNull()) {
			entity.setRedactedFields(
				mapper.convertValue(redactedNode, REDACTED_FIELDS_TYPE));
		}

		return entity;
	}

}
