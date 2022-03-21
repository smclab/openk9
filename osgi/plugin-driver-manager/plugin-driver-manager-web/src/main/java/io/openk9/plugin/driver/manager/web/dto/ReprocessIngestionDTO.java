package io.openk9.plugin.driver.manager.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ReprocessIngestionDTO {
	private long tenantId;
	private String documentTypeName;
}
