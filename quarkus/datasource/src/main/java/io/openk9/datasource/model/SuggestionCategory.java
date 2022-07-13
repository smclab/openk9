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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "suggestion_category")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class SuggestionCategory extends K9Entity {
	@ManyToMany(cascade = {
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.DETACH})
	@JoinTable(name = "suggestion_category_doc_type_fields",
		joinColumns = @JoinColumn(name = "suggestion_category_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_type_fields_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<DocTypeField> docTypeFields = new LinkedHashSet<>();

	@ManyToOne
	@JoinColumn(name = "tenant_id")
	private Tenant tenant;

	public void addDocTypeField(DocTypeField docTypeField) {
		docTypeFields.add(docTypeField);
	}

	public void removeDocTypeField(DocTypeField docTypeField) {
		docTypeFields.remove(docTypeField);
	}

}