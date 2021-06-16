package io.openk9.entity.manager.logic;

import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityContext {
	private Entity entity;
	private EntityRequest entityRequest;
}
