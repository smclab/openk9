package io.openk9.entity.manager.dto;

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
	private Map<String, List<String>> acl;
	private Map<String, Object> rest = new HashMap<>();
	private List<EntityRequest> entities;
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
			.acl(dataPayload.acl)
			.rest(dataPayload.rest)
			.entities(dataPayload.entities)
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
			.acl(acl)
			.rest(rest)
			.entities(entities)
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