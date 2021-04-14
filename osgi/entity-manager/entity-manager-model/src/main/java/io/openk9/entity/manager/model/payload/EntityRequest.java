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
public class EntityRequest {
	private String name;
	private String type;
	private List<RelationRequest> relations;
	private long tmpId;
}
