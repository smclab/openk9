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
import java.util.Map;
import java.util.Objects;
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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import io.openk9.datasource.index.IndexName;
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
import org.eclipse.microprofile.graphql.Ignore;
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
@NamedQuery(
	name = DataIndex.DATA_INDICES_WITH_DOC_TYPES_BY_DATASOURCE,
	query = "from DataIndex di " +
			"join di.datasource d " +
			"left join fetch di.docTypes where d.id = :datasourceId"
)
public class DataIndex extends K9Entity {

	public static final String DATA_INDICES_WITH_DOC_TYPES_BY_DATASOURCE =
		"dataIndicesWithDocTypes";

	private static final boolean DEFAULT_KNN_INDEX = false;
	private static final int DEFAULT_CHUNK_WINDOW_SIZE = 0;
	private static final String DEFAULT_EMBEDDING_JSON_CONFIG = "{}";
	private static final EmbeddingOuterClass.ChunkType DEFAULT_CHUNK_TYPE =
		EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT;

	@Transient
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private IndexName indexName;

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

	public static IndexName getIndexName(String tenantId, DataIndex dataIndex) {
		return new IndexName(OpenSearchUtils.indexNameSanitizer(
			String.format("%s-%s", tenantId, dataIndex.getName())));
	}

	@Transient
	@JsonIgnore
	@Ignore
	private Map<String, Object> settingsMap;

	@Column(name = "knn_index", updatable = false)
	@Immutable
	private Boolean knnIndex = DEFAULT_KNN_INDEX;

	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "embedding_doc_type_field_id", referencedColumnName = "id")
	private DocTypeField embeddingDocTypeField;

	@Column(name = "chunk_type")
	@Enumerated(EnumType.STRING)
	private EmbeddingOuterClass.ChunkType chunkType = DEFAULT_CHUNK_TYPE;

	@Column(name = "chunk_window_size")
	private Integer chunkWindowSize = DEFAULT_CHUNK_WINDOW_SIZE;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "embedding_json_config")
	private String embeddingJsonConfig = DEFAULT_EMBEDDING_JSON_CONFIG;

	public void setChunkType(EmbeddingOuterClass.ChunkType chunkType) {
		this.chunkType =
			Objects.requireNonNullElse(chunkType, DEFAULT_CHUNK_TYPE);
	}

	public void setChunkWindowSize(Integer chunkWindowSize) {
		this.chunkWindowSize =
			Objects.requireNonNullElse(chunkWindowSize, DEFAULT_CHUNK_WINDOW_SIZE);
	}

	public void setEmbeddingJsonConfig(String embeddingJsonConfig) {
		this.embeddingJsonConfig =
			Objects.requireNonNullElse(embeddingJsonConfig, DEFAULT_EMBEDDING_JSON_CONFIG);
	}

}