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

package io.openk9.datasource.plugindriver;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openk9.datasource.jackson.serializer.TemporalAccessorToMillisecondsSerializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class HttpPluginDriverContext {

	// The fields of the invocation: a configuration key with one of these
	// names must not shadow them at the top level.
	private static final Set<String> RESERVED_KEYS = Set.of(
		"datasourceId", "tenantId", "scheduleId", "timestamp", "datasourceConfig");

	private long datasourceId;
	private String tenantId;
	private String scheduleId;
	@JsonSerialize(using = TemporalAccessorToMillisecondsSerializer.class)
	private OffsetDateTime timestamp;
	@Builder.Default
	private Map<String, Object> datasourceConfig = new HashMap<>();

	@JsonAnySetter
	public void addRest(String key, Object value) {
		datasourceConfig.put(key, value);
	}

	/**
	 * Returns the datasource configuration that is also sent at the top level
	 * of the invocation, next to the system fields, for the connectors that
	 * read it there.
	 * <p>
	 * Keys named as a system field are left out, so that the configuration
	 * cannot replace the values of the invocation; they stay readable in
	 * {@code datasourceConfig}.
	 *
	 * @return a copy of the configuration without the reserved keys
	 */
	@JsonAnyGetter
	public Map<String, Object> getRest() {
		var rest = new HashMap<>(datasourceConfig);
		rest.keySet().removeAll(RESERVED_KEYS);

		return rest;
	}

}
