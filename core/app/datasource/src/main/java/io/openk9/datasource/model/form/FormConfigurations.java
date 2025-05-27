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

package io.openk9.datasource.model.form;

import java.util.Collection;
import java.util.LinkedList;

import io.smallrye.graphql.api.CustomScalar;
import io.smallrye.graphql.api.CustomStringScalar;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.eclipse.microprofile.graphql.Description;

/**
 * A collection of form configurations, each associated with a specific type identifier.
 *
 * <p>This class serves as a container for multiple form configurations primarily used
 * for UI form generation and rendering. Each configuration is identified by a unique
 * type string and contains the corresponding form template definition with field
 * specifications, validation rules, and display properties.
 *
 * <p>Currently used for QueryParserConfig entity forms, with plans to extend
 * to other entity configuration forms in the future.
 *
 * <p>This class implements {@link CustomStringScalar} to enable serialization
 * and deserialization of form configurations as JSON strings in GraphQL operations.
 *
 * @see FormConfiguration
 * @see FormTemplate
 */
@Getter
@CustomScalar("FormConfigurations")
public class FormConfigurations implements CustomStringScalar {

	private final Collection<FormConfiguration> configurations;

	/**
	 * Constructs a FormConfigurations instance with the provided collection of configurations.
	 *
	 * <p>This constructor is primarily used when creating instances programmatically.
	 *
	 * @param configurations a collection of form configurations, each containing a type
	 *                       identifier and its associated form template
	 */
	public FormConfigurations(Collection<FormConfiguration> configurations) {
		this.configurations = configurations;
	}

	/**
	 * Constructs a FormConfigurations instance from a JSON array string.
	 *
	 * <p>This constructor is utilized by the {@link CustomStringScalar} handler to deserialize
	 * JSON string representations into FormConfigurations objects. The JSON string
	 * should represent an array of objects, where each object contains 'type' and 'form'
	 * properties matching the {@link FormConfiguration} record structure.
	 *
	 * @param stringValue the JSON array as a string representation containing form configurations
	 * @see CustomStringScalar
	 */
	public FormConfigurations(String stringValue) {
		var jsonArray = (JsonArray) Json.decodeValue(stringValue);
		Collection<FormConfiguration> configurations = new LinkedList<>();

		for (Object obj : jsonArray.getList()) {
			var jsonObj = (JsonObject) obj;
			var configuration = jsonObj.mapTo(FormConfiguration.class);
			configurations.add(configuration);
		}

		this.configurations = configurations;
	}

	@Override
	public String stringValueForSerialization() {
		return Json.encode(configurations);
	}

	/**
	 * Represents a single form configuration with its associated type identifier.
	 *
	 * <p>Each form configuration consists of a unique type identifier
	 * and a form template that defines the structure, fields, etc.
	 * for the corresponding UI form.
	 *
	 * @param type the unique identifier for this form configuration type, used to
	 *             distinguish between different kinds of forms
	 * @param form the form template containing the complete field definitions,
	 *             validation rules, and structural information for UI rendering
	 */
	@Description("Represents a single form configuration with its associated type identifier.")
	public record FormConfiguration(String type, FormTemplate form) {}
}
