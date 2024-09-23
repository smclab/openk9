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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "suggestion_category")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class SuggestionCategory extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "priority", nullable = false)
	private Float priority;

	@Column(name = "multi_select", nullable = false)
	private boolean multiSelect = false;


	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@JoinTable(name = "suggestion_category_doc_type_fields",
		joinColumns = @JoinColumn(name = "suggestion_category_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_type_fields_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<DocTypeField> docTypeFields = new LinkedHashSet<>();

	@ToString.Exclude
	@ManyToOne(
		cascade = {
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.REFRESH,
			jakarta.persistence.CascadeType.DETACH
		}
	)
	@JsonIgnore
	@JoinColumn(name = "bucket_id")
	private Bucket bucket;

	public void addDocTypeField(DocTypeField docTypeField) {
		docTypeFields.add(docTypeField);
	}

	public void removeDocTypeField(DocTypeField docTypeField) {
		docTypeFields.remove(docTypeField);
	}

}