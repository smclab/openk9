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

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "enrich_pipeline_item")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
@Cacheable
public class EnrichPipelineItem
	extends PanacheEntityBase {

	@EmbeddedId
	private EnrichPipelineItemKey key;

	@ToString.Exclude
	@ManyToOne
	@MapsId("enrichPipelineId")
	@JoinColumn(name = "enrich_pipeline_id")
	private EnrichPipeline enrichPipeline;

	@ManyToOne
	@MapsId("enrichItemId")
	@JoinColumn(name = "enrich_item_id")
	@ToString.Exclude
	private EnrichItem enrichItem;

	@Column(name = "weight", nullable = false)
	private Float weight;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		EnrichPipelineItem that = (EnrichPipelineItem) o;
		return key != null && Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}
}