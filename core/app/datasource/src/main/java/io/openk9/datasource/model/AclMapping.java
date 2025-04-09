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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "acl_mapping")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AclMapping {

	@EmbeddedId
	private PluginDriverDocTypeFieldKey key;

	@ToString.Exclude
	@ManyToOne
	@MapsId("pluginDriverId")
	@JoinColumn(name = "plugin_driver_id")
	private PluginDriver pluginDriver;

	@ManyToOne
	@MapsId("docTypeFieldId")
	@JoinColumn(name = "doc_type_field_id")
	@ToString.Exclude
	private DocTypeField docTypeField;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_field", nullable = false)
	private UserField userField;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		AclMapping that = (AclMapping) o;
		return key != null && Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

}