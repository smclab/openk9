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

package io.openk9.datasource.model.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.common.graphql.util.relay.GraphqlId;
import io.openk9.datasource.listener.K9EntityListener;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EntityListeners(K9EntityListener.class)
public abstract class K9Entity extends PanacheEntityBase implements GraphqlId {


	@Type(type = "io.openk9.datasource.type.TenantUserType")
	// workaround to get the UserType valuated
	@Column(name = "id", insertable = false, updatable = false)
	@JsonIgnore
	private String tenant;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@CreationTimestamp
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@UpdateTimestamp
	private OffsetDateTime modifiedDate;

	@javax.persistence.Id
	@GeneratedValue
	@org.eclipse.microprofile.graphql.Id
	public Long id;

	public Long getId() {
		return id;
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
		K9Entity k9Entity = (K9Entity) o;
		return id != null && Objects.equals(id, k9Entity.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}