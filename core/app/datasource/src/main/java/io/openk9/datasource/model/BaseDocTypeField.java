package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;

import static io.openk9.datasource.model.FieldType.BOOLEAN;
import static io.openk9.datasource.model.FieldType.SEARCH_AS_YOU_TYPE;

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

}
