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
public class Entity implements Serializable, Comparable<Entity> {
	private Long id;
	@EqualsAndHashCode.Include
	private Long cacheId;
	private Long tenantId;
	private String name;
	private String type;

	@Override
	public int compareTo(Entity other) {
		Entity p1 = this;
		int res = String.CASE_INSENSITIVE_ORDER.compare(
			p1.getName(), other.getName());
		if (res != 0) {
			return res;
		}
		res = String.CASE_INSENSITIVE_ORDER.compare(
			p1.getType(), other.getType());
		if (res != 0) {
			return res;
		}
		return Long.compare(p1.getTenantId(), other.getTenantId());
	}
}
