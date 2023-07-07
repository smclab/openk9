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
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.collection.Set;

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
	@Setter(AccessLevel.NONE)
	@JsonIgnore
	private Map<String, Object> rest = new HashMap<>();
	private String indexName;
	private boolean last = false;
	private String scheduleId;
	private String oldIndexName = null;

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
			.indexName(dataPayload.indexName)
			.last(dataPayload.last)
			.scheduleId(dataPayload.scheduleId)
			.oldIndexName(dataPayload.oldIndexName)
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
			.indexName(indexName)
			.last(last)
			.scheduleId(scheduleId)
			.build();
	}

	@JsonAnySetter
	public void addRest(String key, Object value) {

		if (value == null) {
			return;
		}
		else {

			if (value instanceof Seq<?>) {
				value = JavaConverters.asJava((Seq<?>) value);
			}
			else if (value instanceof scala.collection.Map) {
				value = JavaConverters.mapAsJavaMap((scala.collection.Map<?, ?>) value);
			}
			else if (value instanceof Set<?>) {
				value = JavaConverters.asJava((Set<?>) value);
			}

		}

		if (value instanceof Collection) {
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
