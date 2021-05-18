package io.openk9.index.writer.entity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntityRequest {
	private long id;
	private long tenantId;
	private String name;
	private String type;
}
