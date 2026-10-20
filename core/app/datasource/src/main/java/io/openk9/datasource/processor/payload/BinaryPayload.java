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

package io.openk9.datasource.processor.payload;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class BinaryPayload {
	private String id;
	private String name;
	private String contentType;
	// Persistent URL of the binary at the source, written by the connector
	// when it exists: the only way to retrieve the binary after indexing.
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String sourceUrl;
	// page, caption, generated and free keys, all optional.
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, Object> metadata;
	// Pre-signed GET URL injected by the datasource before invoking an
	// enricher that reads binaries; absent on the bus, minted per invocation
	// and stripped before indexing (bearer credential), so it is serialized
	// only when present.
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String stagingUrl;
}
