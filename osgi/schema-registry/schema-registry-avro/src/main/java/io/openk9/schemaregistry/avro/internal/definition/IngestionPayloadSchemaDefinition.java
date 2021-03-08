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

package io.openk9.schemaregistry.avro.internal.definition;

import io.openk9.model.IngestionPayload;
import io.openk9.schemaregistry.register.SchemaDefinition;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = SchemaDefinition.class
)
public class IngestionPayloadSchemaDefinition implements SchemaDefinition {

	@Override
	public Integer getVersion() {
		return 1;
	}

	@Override
	public String getSubject() {
		return IngestionPayload.class.getName();
	}

	@Override
	public String getFormat() {
		return "avro";
	}

	@Override
	public String getDefinition() {

		return SchemaBuilder
			.record("IngestionPayload")
			.namespace(IngestionPayload.class.getPackage().getName())
			.fields()
			.requiredLong("tenantId")
			.requiredLong("datasourceId")
			.requiredString("rawContent")
			.name("date")
				.type(LogicalTypes.localTimestampMillis().addToSchema(Schema.create(Schema.Type.LONG))).noDefault()
			.endRecord()
			.toString();

	}
}
