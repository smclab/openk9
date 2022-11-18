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
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static io.openk9.datasource.model.FieldType.BOOLEAN;
import static io.openk9.datasource.model.FieldType.SEARCH_AS_YOU_TYPE;

@Entity
@Table(name = "doc_type_field", uniqueConstraints = {
	@UniqueConstraint(
		name = "uc_doctypefield_name_doc_type_id",
		columnNames = {"fieldName", "doc_type_id"}
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

	@Column(name = "fieldName", nullable = false, updatable = false, length = 4096)
	private String fieldName;

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
	@Column(name = "field_type", nullable = false, updatable = false)
	private FieldType fieldType;

	@Column(name="exclude")
	private Boolean exclude;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinColumn(name = "analyzer", updatable = false)
	@JsonIgnore
	private Analyzer analyzer;

	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "parent_doc_type_field_id", updatable = false)
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField parentDocTypeField;

	@OneToMany(mappedBy = "parentDocTypeField", cascade = CascadeType.ALL)
	@JsonIgnore
	@ToString.Exclude
	private Set<DocTypeField> subDocTypeFields = new LinkedHashSet<>();

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


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		DocTypeField that = (DocTypeField) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}