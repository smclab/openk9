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

package io.openk9.ingestion.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
public class BinaryDTO {
	@Schema(format = "uuid",
			description = "Unique string that identifies by datasource the binary inside Openk9.")
	private String id;
	@Schema(description = "Name associated with binary resource.")
	private String name;
	@Schema(description = "Content Type of the binary resource.")
	private String contentType;
	@Schema(format = "byte",
			description = "Base64 encoded string of binary resource.")
	private String data;
	@Schema(format = "uuid",
			description = "Unique id that identifies the binary inside Openk9.")
	private String resourceId;
}