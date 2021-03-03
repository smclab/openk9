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

package com.openk9.schemaregistry.avro.internal.validator;

import com.openk9.schemaregistry.exception.InvalidSchemaException;
import com.openk9.schemaregistry.model.Schema;
import com.openk9.schemaregistry.validator.SchemaValidator;
import org.apache.avro.SchemaParseException;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(
	immediate = true,
	property = {
		"format=avro"
	},
	service = SchemaValidator.class
)
public class AvroValidator implements SchemaValidator {
	/**
	 * Unique Avro schema format identifier.
	 */
	public static final String AVRO_FORMAT = "avro";

	public boolean isValid(String definition) {
		boolean result = true;
		try {
			new org.apache.avro.Schema.Parser().parse(definition);
		}
		catch (SchemaParseException ex) {
			result = false;
		}
		return result;
	}

	public void validate(String definition) {
		try {
			new org.apache.avro.Schema.Parser().parse(definition);
		}
		catch (SchemaParseException ex) {
			throw new InvalidSchemaException((ex.getMessage()));
		}
	}

	public Schema match(List<Schema> schemas, String definition) {
		Schema result = null;
		org.apache.avro.Schema source =
			new org.apache.avro.Schema.Parser().parse(definition);
		for (Schema s : schemas) {
			org.apache.avro.Schema target =
				new org.apache.avro.Schema.Parser().parse(s.getDefinition());
			if (target.equals(source)) {
				result = s;
				break;
			}
		}
		return result;
	}

	public String getFormat() {
		return AVRO_FORMAT;
	}
}
