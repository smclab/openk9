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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;

import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.Json;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity(name = PluginDriver.ENTITY_NAME)
@Table(name = PluginDriver.TABLE_NAME)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PluginDriver extends K9Entity {

	public static final String ENTITY_NAME = "PluginDriver";
	public static final String TABLE_NAME = "plugin_driver";

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private PluginDriverType type;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@Embedded
	private ResourceUri resourceUri;

	@OneToMany(mappedBy = "pluginDriver", cascade = CascadeType.ALL)
	@ToString.Exclude
	@JsonIgnore
	private Set<AclMapping> aclMappings
		= new LinkedHashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "provisioning")
	private Provisioning provisioning = Provisioning.USER;

	public enum PluginDriverType {
		HTTP
	}

	public enum Provisioning {
		SYSTEM,
		USER
	}

	public static HttpPluginDriverInfo parseHttpInfo(String jsonConfig) {
		return Json.decodeValue(jsonConfig, HttpPluginDriverInfo.class);
	}

	@JsonIgnore
	public HttpPluginDriverInfo getHttpPluginDriverInfo() {
		if (Objects.requireNonNull(type) == PluginDriverType.HTTP) {
			return parseHttpInfo(jsonConfig);
		}
		throw new InvalidPluginDriverType();
	}

}