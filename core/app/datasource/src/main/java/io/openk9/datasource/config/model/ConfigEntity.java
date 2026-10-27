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

package io.openk9.datasource.config.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A single configuration entity inside a {@link ConfigPackage}.
 *
 * <ul>
 *   <li>{@code ref} — local handle, unique within the package, used to wire
 *       references without leaking environment-specific database ids.</li>
 *   <li>{@code type} — drives the typed binding of {@code attributes} via
 *       {@link ConfigEntityType}.</li>
 *   <li>{@code key} — identity for by-name matching on import (typically the
 *       entity name; composite-keyed entities are matched in the importer).</li>
 *   <li>{@code attributes} — the typed DTO carrying the scalar configuration;
 *       its concrete class is given by {@code type}.</li>
 *   <li>{@code references} — outgoing relationships, each mapping a relationship
 *       name to the handles of the target entities.</li>
 *   <li>{@code redactedFields} — names of fields whose secret value was replaced
 *       by the redaction placeholder.</li>
 * </ul>
 * An exported package always writes every attribute, a {@code null} included: a
 * key that is missing from {@code attributes} is a field the package does not
 * manage, as opposed to a key set to {@code null}, which asks for it to be reset.
 * The typed {@code attributes} cannot tell the two apart, so the keys that were
 * present are kept in {@code attributeKeys} when the entity is read.
 */
@Data
@NoArgsConstructor
@JsonDeserialize(using = ConfigEntityDeserializer.class)
public class ConfigEntity {

	private String ref;
	private ConfigEntityType type;
	private String key;
	private Object attributes;
	private Map<String, List<String>> references;
	private List<String> redactedFields;

	/**
	 * The keys the package wrote in {@code attributes}, or {@code null} when the
	 * entity was not read from a package, in which case every attribute counts as
	 * written. It is never serialized.
	 */
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<String> attributeKeys;

	/**
	 * Builds an entity whose every attribute counts as written.
	 */
	public ConfigEntity(
		String ref, ConfigEntityType type, String key, Object attributes,
		Map<String, List<String>> references, List<String> redactedFields) {

		this.ref = ref;
		this.type = type;
		this.key = key;
		this.attributes = attributes;
		this.references = references;
		this.redactedFields = redactedFields;
	}

}
