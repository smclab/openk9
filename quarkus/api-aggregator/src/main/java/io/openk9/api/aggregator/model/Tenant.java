/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.api.aggregator.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = "security_tenant")
@Table(indexes = {
	@Index(name = "idx_tenant_realm_name", columnList = "realmName")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class Tenant extends PanacheEntity {
	private String realmName;
	private String virtualHost;
	private String clientId;
	private String clientSecret;
	private boolean active;

	public static Uni<Tenant> findByRealmName(String name){
		return find("realmName", name).firstResult();
	}

	public static Uni<Long> countByRealmName(String name){
		return find("realmName", name).count();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		Tenant tenant = (Tenant) o;
		return id != null && Objects.equals(id, tenant.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
