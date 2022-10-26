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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
@Cacheable
public class DocType extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@OneToMany(
		mappedBy = "docType",
		cascade = javax.persistence.CascadeType.ALL,
		fetch = FetchType.LAZY
	)
	@ToString.Exclude
	@JsonIgnore
	private Set<DocTypeField> docTypeFields = new LinkedHashSet<>();

	@ToString.Exclude
	@OneToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
	@JoinColumn(name = "doc_type_template_id", referencedColumnName = "id")
	@JsonIgnore
	private DocTypeTemplate _docTypeTemplate;

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