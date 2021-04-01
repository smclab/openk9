package io.openk9.entity.manager.model.payload;

import io.openk9.entity.manager.model.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	private Entity entity;
	private long tmpId;
}
