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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "enrich_pipeline")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EnrichPipeline extends K9Entity {
	@ManyToMany
	@JoinTable(name = "enrich_pipeline_enrich_items",
		joinColumns = @JoinColumn(name = "enrich_pipeline_id"),
		inverseJoinColumns = @JoinColumn(name = "enrich_items_id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<EnrichItem> enrichItems = new LinkedHashSet<>();

	public void addEnrichItem(EnrichItem enrichItem) {
		enrichItems.add(enrichItem);
	}

	public void removeEnrichItem(EnrichItem enrichItem) {
		enrichItems.remove(enrichItem);
	}

}