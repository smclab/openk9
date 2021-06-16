package io.openk9.entity.manager.logic;

import io.openk9.entity.manager.model.payload.EntityRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {
	private String ingestionId;
	private long tenantId;
	private long datasourceId;
	private String contentId;
	private EntityRequest current;
	private List<EntityRequest> rest;
	private String content;
}
