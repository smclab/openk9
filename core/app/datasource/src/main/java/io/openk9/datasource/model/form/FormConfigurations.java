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

import io.smallrye.graphql.api.CustomScalar;
import io.smallrye.graphql.api.CustomStringScalar;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.graphql.Description;

/**
 * A collection of form configurations, each associated with a specific type identifier.
 *
 * <p>This record serves as a container for multiple form configurations primarily used
 * for UI form generation and rendering. Each configuration is identified by a unique
 * type string and contains the corresponding form template definition with field
 * specifications, validation rules, and display properties.
 *
 * <p>Currently used for QueryParserConfig entity forms, with plans to extend
 * to other entity configuration forms in the future.
 *
 * @param configurations collection of form configurations, each with a type and template
 * @see FormConfiguration
 * @see FormTemplate
 */
public record FormConfigurations(Collection<FormConfiguration> configurations) {

	/**
	 * Represents a single form configuration with its associated type identifier.
	 */
	@CustomScalar("FormConfiguration")
	@Description("Represents a single form configuration with its associated type identifier.")
	public static class FormConfiguration implements CustomStringScalar {

		private final String stringValue;
		private final String type;
		private final FormTemplate form;

		/**
		 * @param type the unique identifier for this form configuration type
		 * @param form the form template containing the field definitions and structure
		 */
		public FormConfiguration(String type, FormTemplate form) {

			this.type = type;
			this.form = form;

			var jsonObj = JsonObject.of(type, form);
			this.stringValue = jsonObj.encode();
		}

		/**
		 * @param stringValue string representation of the json that map the form.
		 */
		public FormConfiguration(String stringValue) {
			var jsonObj = (JsonObject) Json.decodeValue(stringValue);

			var firstEntry = jsonObj.getMap().entrySet().iterator().next();
			this.type = firstEntry.getKey();
			this.form = ((JsonObject) firstEntry.getValue()).mapTo(FormTemplate.class);
			this.stringValue = stringValue;

		}

		@Override
		public String stringValueForSerialization() {
			return stringValue;
		}
	}
}

