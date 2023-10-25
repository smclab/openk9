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
import io.openk9.datasource.model.util.DocTypeFieldUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "doc_type_field", uniqueConstraints = {
	@UniqueConstraint(name = "field_name_doc_type_id_parent_doc_type_field_id", columnNames = {
		"field_name", "doc_type_id", "parent_doc_type_field_id"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class DocTypeField extends BaseDocTypeField {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "field_name", nullable = false, updatable = false, length = 4096)
	private String fieldName;

	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "doc_type_id")
	@JsonIgnore
	private DocType docType;

	@Column(name = "searchable")
	private Boolean searchable = false;

	@Column(name = "sortable")
	private Boolean sortable = false;

	@Column(name = "boost")
	private Double boost;

	@Enumerated(EnumType.STRING)
	@Column(name = "field_type", nullable = false, updatable = false)
	private FieldType fieldType;

	@Column(name="exclude")
	private Boolean exclude;

	@ToString.Exclude
	@ManyToOne(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinColumn(name = "analyzer")
	@JsonIgnore
	private Analyzer analyzer;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "parent_doc_type_field_id", updatable = false)
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField parentDocTypeField;

	@OneToMany(mappedBy = "parentDocTypeField", cascade = CascadeType.ALL)
	@JsonIgnore
	@ToString.Exclude
	private Set<DocTypeField> subDocTypeFields = new LinkedHashSet<>();

	@Lob
	@Column(name = "json_config")
	private String jsonConfig;

	@OneToMany(mappedBy = "docTypeField", cascade = javax.persistence.CascadeType.ALL)
	@ToString.Exclude
	@JsonIgnore
	private Set<AclMapping> aclMappings = new LinkedHashSet<>();

	@Transient
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private String path;

	public Set<DocTypeField> getChildren() {
		return subDocTypeFields;
	}

	public Set<DocTypeField> getDocTypeFieldAndChildren() {
		Set<DocTypeField> docTypeFields = new LinkedHashSet<>();
		docTypeFields.add(this);
		if (subDocTypeFields != null) {
			docTypeFields.addAll(subDocTypeFields);
		}
		return docTypeFields;
	}

	public String getPath() {
		if (path == null) {
			refreshPath();
		}

		return path;
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

	@PostLoad
	@PostPersist
	@PostUpdate
	protected void refreshPath() {
		this.path = DocTypeFieldUtils.fieldPath(this);
	}

}