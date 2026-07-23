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

import io.openk9.common.model.dto.Problem;
import io.openk9.datasource.web.Problems;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Root deserializer for {@link ConfigPackage} that validates the schema version
 * <em>before</em> any entity is typed-bound, so an incompatible or future
 * package is rejected with a speaking {@code 400} naming the version, rather than
 * dying opaquely while binding an unknown {@link ConfigEntityType}.
 * <p>
 * The version check runs first (see {@link SchemaVersion}); the subsequent typed
 * binding of {@code metadata} and {@code entities} is guarded so that even a
 * compatible-version package that cannot be bound (an unknown type, a malformed
 * shape) still yields a speaking {@code 400} rather than an empty one.
 * <p>
 * A {@link WebApplicationException} carrying a built {@link Response} is thrown
 * from the <em>root</em> of the read and therefore propagates to the client with
 * its body intact: Jackson wraps (and the message-body reader then swallows) only
 * the exceptions raised by nested element deserializers, never one thrown here.
 */
public class ConfigPackageDeserializer extends JsonDeserializer<ConfigPackage> {

	private static final TypeReference<List<ConfigEntity>> ENTITIES_TYPE =
		new TypeReference<>() {};

	@Override
	public ConfigPackage deserialize(JsonParser parser, DeserializationContext context)
		throws IOException {

		ObjectMapper mapper = (ObjectMapper) parser.getCodec();
		JsonNode node = mapper.readTree(parser);

		String rawVersion = node.hasNonNull("schemaVersion")
			? node.get("schemaVersion").asText()
			: null;

		if (SchemaVersion.parse(rawVersion)
			.filter(SchemaVersion.CURRENT::accepts)
			.isEmpty()) {

			throw badRequest(Problems.unsupportedSchemaVersion(rawVersion));
		}

		ConfigPackage configPackage = new ConfigPackage();
		configPackage.setSchemaVersion(rawVersion);
		try {
			JsonNode metadataNode = node.get("metadata");
			if (metadataNode != null && !metadataNode.isNull()) {
				configPackage.setMetadata(
					mapper.treeToValue(metadataNode, ConfigMetadata.class));
			}
			JsonNode entitiesNode = node.get("entities");
			if (entitiesNode != null && !entitiesNode.isNull()) {
				configPackage.setEntities(
					mapper.convertValue(entitiesNode, ENTITIES_TYPE));
			}
		}
		catch (IllegalArgumentException | JsonProcessingException e) {
			throw badRequest(Problems.malformedPackage(describe(e)));
		}
		return configPackage;
	}

	private static WebApplicationException badRequest(Problem problem) {
		return new WebApplicationException(
			Response.status(Response.Status.BAD_REQUEST)
				.entity(problem)
				.type(MediaType.APPLICATION_JSON)
				.build());
	}

	/**
	 * A client-safe description of a binding failure: the message of a known
	 * {@link ConfigEntityDeserializer.UnknownTypeException} (which names only the
	 * offending value) when one is present in the cause chain, otherwise a generic
	 * message that never leaks internal class or package names from the underlying
	 * Jackson error.
	 */
	private static String describe(Exception e) {
		Throwable cause = e;
		while (cause != null) {
			if (cause instanceof ConfigEntityDeserializer.UnknownTypeException) {
				return cause.getMessage();
			}
			Throwable next = cause.getCause();
			cause = next == cause ? null : next;
		}
		return "the configuration package contains an entity that could not be read "
			+ "(unknown type or invalid attributes)";
	}

}
