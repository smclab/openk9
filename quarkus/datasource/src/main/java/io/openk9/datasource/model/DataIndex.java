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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "data_index")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
@AllArgsConstructor(staticName = "of")
public class DataIndex extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;
	@ManyToMany(cascade = {
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinTable(name = "data_index_doc_types",
		joinColumns = @JoinColumn(name = "data_index_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_types_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private List<DocType> docTypes = new java.util.ArrayList<>();

	@OneToOne(cascade = javax.persistence.CascadeType.ALL)
	@JoinTable(name = "data_index_datasource",
		joinColumns = @JoinColumn(name = "dataIndex_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "datasource_id", referencedColumnName = "id"))
	@JsonIgnore
	private Datasource datasource;

	public void addDocType(DocType docType) {
		docTypes.add(docType);
	}

	public void removeDocType(DocType docType) {
		docTypes.remove(docType);
	}

}