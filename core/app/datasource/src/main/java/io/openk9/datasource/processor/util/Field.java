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

package io.openk9.datasource.processor.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Field {

	private Field(String name) {
		this(name, null);
	}

	private Field(String name, String type) {
		this(name, type, Map.of());
	}

	private Field(String name, String type, Map<String, Object> extra) {
		this.name = Objects.requireNonNull(name, "name is null");
		this.type = type;
		this.extra = extra;
	}



	public void addSubFields(Collection<Field> fields) {

		if (subFields == null) {
			subFields = new LinkedList<>();
		}

		this.subFields.addAll(fields);
	}

	public void addSubField(Field field) {

		if (subFields == null) {
			subFields = new LinkedList<>();
		}

		this.subFields.add(field);

	}

	public void addI18NField(Field field) {

		if (i18nFields == null) {
			i18nFields = new LinkedList<>();
		}

		this.i18nFields.add(field);

	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<Field> getSubFields() {
		return subFields == null ? List.of() : subFields;
	}

	public List<Field> getI18NFields() {
		return i18nFields == null ? List.of() : i18nFields;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRoot() {
		return Objects.equals(name, ROOT);
	}

	public static Field of(String name) {
		return new Field(name);
	}

	public static Field of(String name, String type) {
		return new Field(name, type);
	}

	public static Field of(String name, String type, Map<String, Object> extra) {
		return new Field(name, type, extra);
	}

	public static Field createRoot() {
		return new Field(ROOT);
	}

	private final String name;
	private String type;

	private Map<String, Object> extra;
	private List<Field> subFields;
	private List<Field> i18nFields;

	public static final String ROOT = "root-" + UUID.randomUUID();

}
