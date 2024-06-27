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

import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tenant_binding")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TenantBinding extends K9Entity {
	@Column(name = "virtual_host", nullable = false, unique = true)
	private String virtualHost;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_binding_bucket_id")
	@ToString.Exclude
	private Bucket bucket;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "embedding_model_id")
	@ToString.Exclude
	private EmbeddingModel embeddingModel;
}