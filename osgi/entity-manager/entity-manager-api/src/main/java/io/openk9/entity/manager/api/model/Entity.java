package io.openk9.entity.manager.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
	private long id;
	private long tenantId;
	private String entityName;
	private String entityType;
}
