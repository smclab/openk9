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

package io.openk9.plugin.driver.manager.api;

import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class Field {

	private Field(String name) {
		this(name, FieldType.NULL, NIL, Collections.emptyMap());
	}

	public Field(String name, Field child) {
		this(name, FieldType.NULL, child, Collections.emptyMap());
	}

	private Field(String name, FieldType fieldType) {
		this(name, fieldType, NIL, Collections.emptyMap());
	}

	private Field(
		String name, FieldType fieldType, Map<String, Object> extra) {
		this(name, fieldType, NIL, extra);
	}

	private Field(String name, FieldType fieldType, Field child) {
		this(name, fieldType, child, Collections.emptyMap());
	}

	private Field(
		String name, FieldType fieldType, Field child,
		Map<String, Object> extra) {

		this.name = name;
		this.fieldType = fieldType;
		this.child = child == null ? NIL : child;
		this.extra = extra;

	}

	public static Field of(String name) {
		return new Field(name);
	}

	public static Field of(String name, FieldType fieldType) {
		return new Field(name, fieldType);
	}

	public static Field of(
		String name, FieldType fieldType, Map<String, Object> extra) {

		return new Field(name, fieldType, extra);
	}

	public static Field of(String name, Field child) {
		return new Field(name, child);
	}

	public static Field of(String name, FieldType fieldType, Field child) {
		return new Field(name, fieldType, child);
	}

	public static Field of(
		String name, FieldType fieldType, Field child,
		Map<String, Object> extra) {

		return new Field(name, fieldType, child, extra);

	}

	private final String name;
	private final FieldType fieldType;
	private Field child;
	private Map<String, Object> extra;

	public static final Field NIL = new Field(
		"nil", FieldType.TEXT);

}
