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

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class HttpPluginDriverContext {
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

	@JsonAnyGetter
	public Map<String, Object> getRest() {
		return datasourceConfig;
	}

}
