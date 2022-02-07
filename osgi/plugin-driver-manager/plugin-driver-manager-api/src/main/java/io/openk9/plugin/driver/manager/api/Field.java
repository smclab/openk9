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

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

public abstract class Field {

	public abstract String getName();
	public abstract FieldType getFieldType();
	public abstract Field getChild();
	public abstract Map<String, Object> getExtra();

	@Getter
	public static class FieldObj extends Field {
		private FieldObj(String name) {
			this(name, FieldType.NULL, NIL, Collections.emptyMap());
		}

		public FieldObj(String name, Field child) {
			this(name, FieldType.NULL, child, Collections.emptyMap());
		}

		private FieldObj(String name, FieldType fieldType) {
			this(name, fieldType, NIL, Collections.emptyMap());
		}

		private FieldObj(
			String name, FieldType fieldType, Map<String, Object> extra) {
			this(name, fieldType, NIL, extra);
		}

		private FieldObj(String name, FieldType fieldType, Field child) {
			this(name, fieldType, child, Collections.emptyMap());
		}

		private FieldObj(
			String name, FieldType fieldType, Field child,
			Map<String, Object> extra) {

			this.name = name;
			this.fieldType = fieldType;
			this.child = child == null ? NIL : child;
			this.extra = extra;
		}

		private final String name;
		private final FieldType fieldType;
		private final Field child;
		private final Map<String, Object> extra;

		public static final Field NIL = new FieldObj(
			"nil", FieldType.TEXT);

	}

	public static class FieldMappings extends Field {

		public FieldMappings(Map<String, Object> mappings) {
			_mappings = mappings;
		}

		@Override
		public String getName() {
			throw new IllegalStateException();
		}

		@Override
		public FieldType getFieldType() {
			throw new IllegalStateException();
		}

		@Override
		public Field getChild() {
			throw new IllegalStateException();
		}

		@Override
		public Map<String, Object> getExtra() {
			return _mappings;
		}

		private final Map<String, Object> _mappings;

	}

	public static Field of(String name) {
		return new FieldObj(name);
	}

	public static Field of(String name, FieldType fieldType) {
		return new FieldObj(name, fieldType);
	}

	public static Field of(
		String name, FieldType fieldType, Map<String, Object> extra) {

		return new FieldObj(name, fieldType, extra);
	}

	public static Field of(String name, Field child) {
		return new FieldObj(name, child);
	}

	public static Field of(String name, FieldType fieldType, Field child) {
		return new FieldObj(name, fieldType, child);
	}

	public static Field of(
		String name, FieldType fieldType, Field child,
		Map<String, Object> extra) {

		return new FieldObj(name, fieldType, child, extra);
	}

	public static Field ofMapping(Map<String, Object> mapping) {
		return new FieldMappings(mapping);
	}

}
