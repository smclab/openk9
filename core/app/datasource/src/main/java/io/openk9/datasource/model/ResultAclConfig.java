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

package io.openk9.datasource.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.graphql.Ignore;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tenant-wide configuration of the search mode that applies the ACLs to the
 * results instead of the query.
 * <p>
 * When the mode is enabled, a document the user is not authorized to read is
 * still returned, but only with its {@link #visibleFields}. Users holding one
 * of the {@link #superUserRoles super-user roles} read every result in full.
 * A tenant has at most one configuration, named {@link #DEFAULT_NAME}; a
 * missing configuration is equivalent to a disabled one.
 */
@Entity
@Table(name = "result_acl_config")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ResultAclConfig extends K9Entity {

	public static final String DEFAULT_NAME = "Default Result ACL Configuration";

	private static final String ROLE_SEPARATOR = "\n";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = false;

	/**
	 * The super-user roles, one per line; see {@link #toRoleList(String)}.
	 * GraphQL exposes them as a list through a dedicated resolver.
	 */
	@Ignore
	@Column(name = "super_user_roles")
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	private String superUserRoles;

	@JoinTable(
		name = "result_acl_config_visible_fields",
		joinColumns = @JoinColumn(
			name = "result_acl_config_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(
			name = "doc_type_field_id", referencedColumnName = "id")
	)
	@ManyToMany
	@JsonIgnore
	@ToString.Exclude
	private Set<DocTypeField> visibleFields = new LinkedHashSet<>();

	/**
	 * Splits the stored super-user roles into a list.
	 *
	 * @param roles the roles as stored, one per line
	 * @return the roles, empty when none is stored
	 */
	public static List<String> toRoleList(String roles) {
		if (roles == null || roles.isBlank()) {
			return List.of();
		}

		return Arrays.stream(roles.split(ROLE_SEPARATOR))
			.filter(role -> !role.isBlank())
			.toList();
	}

	/**
	 * Joins the super-user roles into their stored form, one per line.
	 * <p>
	 * A role must therefore not contain a line break; the DTO validation
	 * rejects such values.
	 *
	 * @param roles the roles
	 * @return the stored form, {@code null} when there are no roles
	 */
	public static String toRoleText(List<String> roles) {
		return roles == null || roles.isEmpty()
			? null
			: String.join(ROLE_SEPARATOR, roles);
	}

}
