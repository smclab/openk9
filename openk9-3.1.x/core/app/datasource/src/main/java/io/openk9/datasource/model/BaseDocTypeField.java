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

import static io.openk9.datasource.model.FieldType.BOOLEAN;
import static io.openk9.datasource.model.FieldType.I18N;
import static io.openk9.datasource.model.FieldType.SEARCH_AS_YOU_TYPE;

import io.openk9.datasource.model.util.K9Entity;

public abstract class BaseDocTypeField extends K9Entity {

	public abstract Boolean getSearchable();
	public abstract Double getBoost();
	public abstract FieldType getFieldType();
	public abstract Boolean getExclude();
	public abstract Boolean getSortable();

	public Float getFloatBoost() {
		return getBoost().floatValue();
	}

	public boolean isKeyword() {
		return getFieldType() == FieldType.KEYWORD;
	}

	public boolean isText() {
		return switch (getFieldType()) {
			case TEXT, CONSTANT_KEYWORD, ANNOTATED_TEXT, KEYWORD -> true;
			default -> false;
		};
	}

	public boolean isNumeric() {
		return switch (getFieldType()) {
			case LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, HALF_FLOAT, SCALED_FLOAT -> true;
			default -> false;
		};
	}

	public boolean isDate() {
		return switch (getFieldType()) {
			case DATE, DATE_NANOS, DATE_RANGE -> true;
			default -> false;
		};
	}

	public boolean isBoolean() {
		return BOOLEAN == getFieldType();
	}

	public boolean isI18N() {
		return I18N == getFieldType();
	}

	public boolean isAutocomplete() {
		return SEARCH_AS_YOU_TYPE == getFieldType();
	}

	public boolean isDefaultExclude() {
		return getExclude() != null && getExclude();
	}

	public boolean isDefaultBoost() {
		return getBoost() != null && getBoost() > 0;
	}

	public boolean isSearchable() {
		return getSearchable() != null && getSearchable();
	}

	public boolean isSortable() {
		return getSortable() != null && getSortable();
	}

	public boolean isSearchableAnd(FieldType fieldType) {
		return isSearchable() && getFieldType() == fieldType;
	}

	public boolean isSearchableAndAutocomplete() {
		return isSearchable() && isAutocomplete();
	}

	public boolean isSearchableAndDate() {
		return isSearchable() && isDate();
	}

	public boolean isSearchableAndText() {
		return isSearchable() && isText();
	}

	public boolean isSearchableAndI18N() {
		return isSearchable() && isI18N();
	}

}
