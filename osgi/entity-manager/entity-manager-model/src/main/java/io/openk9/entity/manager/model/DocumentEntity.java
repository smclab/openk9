package io.openk9.entity.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
	private long id;
	private long tenantId;
	private String name;
	private String type;
	private double score;
}
