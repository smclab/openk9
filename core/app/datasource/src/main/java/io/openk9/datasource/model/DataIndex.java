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

import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.util.OpenSearchUtils;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "data_index")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DataIndex extends K9Entity {

	@Column(
		name = "name", nullable = false, unique = true, updatable = false)
	@Immutable
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	})
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

	@Transient
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private String indexName;

	@Column(name = "knn_index", updatable = false)
	@Immutable
	private Boolean knnIndex = false;

	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "embedding_doc_type_field_id", referencedColumnName = "id")
	private DocTypeField embeddingDocTypeField;

	@Column(name = "chunk_type")
	@Enumerated(EnumType.STRING)
	private EmbeddingOuterClass.ChunkType chunkType;

	@Column(name = "chunk_window_size")
	private Integer chunkWindowSize;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "embedding_json_config")
	private String embeddingJsonConfig;

	public String getIndexName() throws UnknownTenantException {
		if (indexName == null) {
			initIndexName();
		}

		return indexName;
	}

	@PostLoad
	protected void initIndexName() throws UnknownTenantException {
		String tenantId = getTenant();

		// This is a workaround needed when a new DataIndex is being created.
		// The tenant is not identified, likely because the entity has not
		// been persisted yet. Therefore, it is obtained from an entity that
		// is already in the persistence context, typically, the first
		// DocType associated with the new DataIndex, or alternatively,
		// the Datasource associated with it.
		if (tenantId == null) {
			var iterator = docTypes.iterator();
			if (iterator.hasNext()) {
				var docType = iterator.next();
				tenantId = docType.getTenant();
			}
			else {
				var ds = getDatasource();
				if (ds != null) {
					tenantId = ds.getTenant();
				}
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