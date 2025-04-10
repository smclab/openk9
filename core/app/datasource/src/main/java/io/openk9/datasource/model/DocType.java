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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "doc_type")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class DocType extends K9Entity {

	public static final String DEFAULT_NAME = "default";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@OneToMany(
		mappedBy = "docType",
		cascade = jakarta.persistence.CascadeType.ALL,
		fetch = FetchType.LAZY,
		orphanRemoval = true
	)
	@ToString.Exclude
	@JsonIgnore
	private Set<DocTypeField> docTypeFields = new LinkedHashSet<>();

	@ToString.Exclude
	@OneToOne(
		fetch = jakarta.persistence.FetchType.LAZY, cascade = {
		CascadeType.DETACH,
		CascadeType.MERGE,
		CascadeType.PERSIST,
		CascadeType.REFRESH
	})
	@JoinColumn(name = "doc_type_template_id", referencedColumnName = "id")
	@JsonIgnore
	private DocTypeTemplate docTypeTemplate;

	public boolean addDocTypeField(
		Collection<DocTypeField> docTypeFields, DocTypeField docTypeField) {
		if (docTypeFields.add(docTypeField)) {
			docTypeField.setDocType(this);
			return true;
		}
		return false;
	}

	public boolean removeDocTypeField(
		Collection<DocTypeField> docTypeFields, DocTypeField docTypeField) {

		if (docTypeFields.remove(docTypeField)) {
			docTypeField.setDocType(null);
			return true;
		}

		return false;

	}

	public boolean removeDocTypeField(Collection<DocTypeField> docTypeFields, long docTypeFieldId) {

		Iterator<DocTypeField> iterator = docTypeFields.iterator();

		while (iterator.hasNext()) {
			DocTypeField docTypeField = iterator.next();
			if (docTypeField.getId() == docTypeFieldId) {
				iterator.remove();
				docTypeField.setDocType(null);
				return true;
			}
		}
		return false;

	}

}