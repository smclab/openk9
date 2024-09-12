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
import io.openk9.datasource.util.OpenSearchUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "data_index")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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
	private Set<DocType> docTypes = new LinkedHashSet<>();

	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(optional = false)
	@JoinColumn(name = "datasource_id", referencedColumnName = "id")
	private Datasource datasource;

	@JsonIgnore
	@ToString.Exclude
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vector_index_id", referencedColumnName = "id")
	private VectorIndex vectorIndex;

	@Transient
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private String indexName;

	public void addDocType(DocType docType) {
		docTypes.add(docType);
	}

	public void removeDocType(DocType docType) {
		docTypes.remove(docType);
	}

	public String getIndexName() throws UnknownTenantException {
		if (indexName == null) {
			setupIndexName();
		}

		return indexName;
	}

	@PostLoad
	@PostUpdate
	protected void setupIndexName() throws UnknownTenantException {
		String tenantId = getTenant();

		// This is a workaround needed when a new DataIndex is being created.
		// The tenant is not identified, probably because the entity has not
		// been persisted yet. Therefore, it is obtained from an entity that
		// is already in the persistence context, in this case, the first
		// DocType associated with the new DataIndex.
		if (tenantId == null) {
			var iterator = docTypes.iterator();
			if (iterator.hasNext()) {
				var docType = iterator.next();
				tenantId = docType.getTenant();
			}
			if (tenantId == null) {
				throw new UnknownTenantException(
					String.format("Cannot identify the tenant for DataIndex: %s", getName()));
			}
		}

		this.indexName = OpenSearchUtils.indexNameSanitizer(
			String.format("%s-%s", tenantId, getName())
		);
	}
}