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

package io.openk9.tenantmanager.model;

import io.openk9.common.graphql.util.relay.GraphqlId;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "background_process")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
public class BackgroundProcess implements GraphqlId {

	@Setter(AccessLevel.NONE)
	@Id
	@GeneratedValue(
		strategy = GenerationType.SEQUENCE,
		generator = "hibernate_sequence"
	)
	@SequenceGenerator(
		name = "hibernate_sequence",
		allocationSize = 1
	)
	@Column(name = "id", nullable = false)
	private Long id;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@CreationTimestamp
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@UpdateTimestamp
	private OffsetDateTime modifiedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Column(name = "message")
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	private String message;

	@Column(name = "process_id", nullable = false)
	private UUID processId;

	@Column(name = "name", nullable = false)
	private String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		BackgroundProcess that = (BackgroundProcess) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public enum Status {
		IN_PROGRESS, FINISHED, FAILED, ROOLBACK
	}

}