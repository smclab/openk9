package io.openk9.entity.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EntityManagerRequest {
	private long tenantId;
	private String ingestionId;
	private List<EntityRequest> entities;
}
