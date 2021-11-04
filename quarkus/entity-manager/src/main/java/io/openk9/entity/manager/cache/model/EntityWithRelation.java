package io.openk9.entity.manager.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityWithRelation implements Serializable {
	private Long id;
	@EqualsAndHashCode.Include
	private Long cacheId;
	private Long tenantId;
	private Long tmpId;
	private String ingestionId;
	private String name;
	private String type;
}
