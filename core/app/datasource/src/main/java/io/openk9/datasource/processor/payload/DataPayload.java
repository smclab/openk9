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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class DataPayload {
	private String ingestionId;
	private long datasourceId;
	private String contentId;
	private long parsingDate;
	private String rawContent;
	private String tenantId;
	private String[] documentTypes;
	private ResourcesPayload resources;
	private Map<String, List<String>> acl;
	private Map<String, Object> rest = new HashMap<>();
	private String indexName;

	public static DataPayload copy(DataPayload dataPayload) {
		return DataPayload.builder()
			.ingestionId(dataPayload.ingestionId)
			.datasourceId(dataPayload.datasourceId)
			.contentId(dataPayload.contentId)
			.parsingDate(dataPayload.parsingDate)
			.rawContent(dataPayload.rawContent)
			.tenantId(dataPayload.tenantId)
			.documentTypes(dataPayload.documentTypes)
			.resources(dataPayload.resources)
			.acl(dataPayload.acl)
			.rest(dataPayload.rest)
			.build();
	}

	public DataPayload rest(Map<String, Object> rest) {
		return DataPayload.builder()
			.ingestionId(ingestionId)
			.datasourceId(datasourceId)
			.contentId(contentId)
			.parsingDate(parsingDate)
			.rawContent(rawContent)
			.tenantId(tenantId)
			.documentTypes(documentTypes)
			.resources(resources)
			.acl(acl)
			.rest(rest)
			.build();
	}

	@JsonAnySetter
	public void addRest(String key, Object value) {

		if (value == null) {
			return;
		}
		else if (value instanceof Collection) {
			if (((Collection) value).isEmpty()) {
				return;
			}
		}
		else if (value instanceof Map) {
			if (((Map) value).isEmpty()) {
				return;
			}
		}

		rest.put(key, value);

	}

	@JsonAnyGetter
	public Map<String, Object> getRest() {
		return rest;
	}
}
