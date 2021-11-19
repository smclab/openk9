package io.openk9.entity.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EntityManagerRequest implements Serializable {
	private long tenantId;
	private String ingestionId;
	private String contentId;
	private long datasourceId;
}
