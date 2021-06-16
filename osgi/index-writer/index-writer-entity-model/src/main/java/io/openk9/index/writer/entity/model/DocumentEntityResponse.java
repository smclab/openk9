package io.openk9.index.writer.entity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DocumentEntityResponse {
	private long id;
	private long tenantId;
	private String name;
	private String type;
	@EqualsAndHashCode.Exclude
	private float score;
}
