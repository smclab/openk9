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
public class EntityRelation implements Serializable {
	@EqualsAndHashCode.Include
	private Long cacheId;
	private Long entityCacheId;
	private String ingestionId;
	private String name;
	private Long to;
}
