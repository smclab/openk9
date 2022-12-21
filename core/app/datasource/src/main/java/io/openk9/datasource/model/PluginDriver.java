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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "plugin_driver")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class PluginDriver extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private PluginDriverType type;

	@Lob
	@Column(name = "json_config")
	private String jsonConfig;

	@OneToMany(mappedBy = "pluginDriver", cascade = javax.persistence.CascadeType.ALL)
	@ToString.Exclude
	@JsonIgnore
	private Set<AclMapping> aclMappings
		= new LinkedHashSet<>();

	public enum PluginDriverType {
		HTTP
	}

}