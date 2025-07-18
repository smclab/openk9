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

import java.time.OffsetDateTime;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;

import io.openk9.common.graphql.util.relay.GraphqlId;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class K9Entity implements GraphqlId {

	@jakarta.persistence.Id
	@GeneratedValue(
		strategy = GenerationType.SEQUENCE,
		generator = "hibernate_sequence"
	)
	@SequenceGenerator(
		name = "hibernate_sequence",
		allocationSize = 1
	)
	@org.eclipse.microprofile.graphql.Id
	public Long id;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@CreationTimestamp
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@UpdateTimestamp
	private OffsetDateTime modifiedDate;

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