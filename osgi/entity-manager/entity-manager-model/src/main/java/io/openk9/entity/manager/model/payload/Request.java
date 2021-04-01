package io.openk9.entity.manager.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
	private long tenantId;
	private long datasourceId;
	private String contentId;
	private List<EntityRequest> entities;
	private String content;
}
