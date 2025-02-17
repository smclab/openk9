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

import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

	@ManyToOne(cascade = {
			jakarta.persistence.CascadeType.REFRESH,
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.DETACH
		}
	)
	@ToString.Exclude
	@JsonIgnore
	@JoinColumn(name = "doc_type_field_id")
	private DocTypeField docTypeField;

	@ToString.Exclude
	@ManyToMany(
		mappedBy = "suggestionCategories",
		cascade = {
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.REFRESH,
			jakarta.persistence.CascadeType.DETACH
		}
	)
	@JsonIgnore
	private Set<Bucket> buckets;

}