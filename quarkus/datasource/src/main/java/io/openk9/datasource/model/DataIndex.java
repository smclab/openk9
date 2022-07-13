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
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "data_index")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NamedQueries({
	@NamedQuery(
		name = "DataIndex.getDocTypes",
		query =
			"select dataIndex.docTypes " +
			"from DataIndex dataIndex " +
			"where dataIndex.id = ?1"
	)
})
public class DataIndex extends K9Entity {
	@ManyToMany(cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.DETACH,
		javax.persistence.CascadeType.REFRESH})
	@JoinTable(name = "data_index_doc_types",
		joinColumns = @JoinColumn(name = "data_index_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_types_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<DocType> docTypes = new LinkedHashSet<>();

	public void addDocType(DocType docType) {
		docTypes.add(docType);
	}

	public void removeDocType(DocType docType) {
		docTypes.remove(docType);
	}

}