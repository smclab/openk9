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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "type")),
		@AttributeOverride(name = "model", column = @Column(name = "model"))
	})
	private ModelType modelType;

	@Transient
	private boolean enabled = false;

	@PostLoad
	void postLoad() {
		this.enabled = tenantBinding != null;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = Objects.requireNonNullElse(modelType, new ModelType());
	}
}
