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

import java.time.OffsetDateTime;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.openk9.common.graphql.util.relay.GraphqlId;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@Builder
@ToString
@RequiredArgsConstructor
@RegisterForReflection
@AllArgsConstructor
public class Tenant implements GraphqlId {

	@Id
	@Column(name = "id", nullable = false)
	private Long id;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@CreationTimestamp
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@UpdateTimestamp
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	private OffsetDateTime modifiedDate;

	@Column(name = "schema_name", nullable = false)
	private String schemaName;

	@Column(name = "liquibase_schema_name", nullable = false)
	private String liquibaseSchemaName;

	@Column(name = "virtual_host", nullable = false, unique = true)
	private String virtualHost;

	@Column(name = "client_id", nullable = false)
	private String clientId;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "realm_name", nullable = false)
	private String realmName;

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