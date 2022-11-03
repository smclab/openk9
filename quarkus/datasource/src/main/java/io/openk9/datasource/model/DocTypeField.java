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
import io.openk9.datasource.model.util.AnalyzerType;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static io.openk9.datasource.model.FieldType.BOOLEAN;
import static io.openk9.datasource.model.FieldType.SEARCH_AS_YOU_TYPE;

@Entity
@Table(name = "doc_type_field", uniqueConstraints = {
	@UniqueConstraint(
		name = "uc_doctypefield_name_doc_type_id",
		columnNames = {"name", "doc_type_id"}
	)
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class DocTypeField extends K9Entity {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;
	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
	@JoinColumn(name = "doc_type_id")
	@JsonIgnore
	private DocType docType;

	@Column(name = "searchable")
	private Boolean searchable = false;

	@Column(name = "boost")
	private Double boost;

	@Enumerated(EnumType.STRING)
	@Column(name = "field_type", nullable = false)
	private FieldType fieldType;

	@Enumerated(EnumType.STRING)
	@Column(name = "analyzerType")
	private AnalyzerType analyzerType;

	@Column(name="exclude")
	private Boolean exclude;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "analyzer")
	@JsonIgnore
	private Analyzer analyzer;



	public Float getFloatBoost() {
		return boost.floatValue();
	}

	public boolean isKeyword() {
		return fieldType == FieldType.KEYWORD;
	}

	public boolean isText() {
		return switch (fieldType) {
			case TEXT, CONSTANT_KEYWORD, ANNOTATED_TEXT, KEYWORD -> true;
			default -> false;
		};
	}

	public boolean isNumeric() {
		return switch (fieldType) {
			case LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, HALF_FLOAT, SCALED_FLOAT -> true;
			default -> false;
		};
	}

	public boolean isDate() {
		return switch (fieldType) {
			case DATE, DATE_NANOS, DATE_RANGE -> true;
			default -> false;
		};
	}

	public boolean isBoolean() {
		return BOOLEAN == fieldType;
	}

	public boolean isAutocomplete() {
		return SEARCH_AS_YOU_TYPE == fieldType;
	}

	public boolean isDefaultExclude() {
		return exclude != null && exclude;
	}

	public boolean isSearchableAndDate() {
		return getSearchable() && isDate();
	}

}