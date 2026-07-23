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
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.graphql.Description;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity
@Table(name = "embedding_model")
@NamedQuery(
	name = EmbeddingModel.FETCH_CURRENT,
	query = "from EmbeddingModel em where em.tenantBinding is not null"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingModel extends K9Entity {

	public static final String FETCH_CURRENT = "EmbeddingModel.FetchCurrent";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "api_url")
	private String apiUrl;

	@Column(name = "api_key")
	private String apiKey;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@OneToOne(mappedBy = "embeddingModel")
	@JsonIgnore
	private TenantBinding tenantBinding;

	@Column(name = "vector_size")
	private Integer vectorSize = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "vector_data_type", nullable = false)
	private VectorDataType vectorDataType = VectorDataType.FLOAT32;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "provider", column = @Column(name = "provider")),
		@AttributeOverride(name = "model", column = @Column(name = "model"))
	})
	private ProviderModel providerModel;

	@Transient
	private boolean enabled = false;

	@PostLoad
	void postLoad() {
		this.enabled = tenantBinding != null;
	}

	public void setProviderModel(ProviderModel providerModel) {
		this.providerModel = Objects.requireNonNullElse(providerModel, new ProviderModel());
	}

	public void setVectorDataType(VectorDataType vectorDataType) {
		this.vectorDataType = Objects.requireNonNullElse(
			vectorDataType, VectorDataType.FLOAT32);
	}

	/**
	 * Tenant-global type of the vector written to the index. The same value
	 * drives the quantization performed by the embedding module and the
	 * {@code knn_vector} mapping created on OpenSearch.
	 */
	public enum VectorDataType {

		@Description("Full precision 32-bit float vectors (default, no quantization).")
		FLOAT32,

		@Description(
			"Signed 8-bit integer vectors; maps to a byte knn_vector "
			+ "(lucene engine, cosinesimil space).")
		BYTE,

		@Description(
			"Packed binary vectors; maps to a binary knn_vector "
			+ "(faiss engine, hamming space). Requires a vectorSize multiple of 8.")
		BINARY
	}
}
