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

package io.openk9.schemaregistry.model;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class Schema {
	private Integer id;
	private Integer version;
	private String subject;
	private String format;
	private String definition;

	public static Schema mapping(Row row, RowMetadata rowMetadata) {
		Integer id = row.get("id", Integer.class);
		Integer version = row.get("version", Integer.class);
		String subject = row.get("subject", String.class);
		String format = row.get("format", String.class);
		String definition = row.get("definition", String.class);
		return of(id, version, subject, format, definition);
	}

}
