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
public class Entity implements Serializable {
	private Long id;
	@EqualsAndHashCode.Include
	private Long cacheId;
	private Long tenantId;
	private String name;
	private String type;
}
