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

import com.cronutils.model.CronType;
import com.cronutils.validation.Cron;
import io.openk9.datasource.model.mapper.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "datasource")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Datasource extends K9Entity {

	@OneToOne
	@JoinColumn(name = "data_index_id")
	private DataIndex dataIndex;

	@OneToOne
	@JoinColumn(name = "entity_index_id")
	private EntityIndex entityIndex;

	@ManyToOne
	@JoinColumn(name = "enrich_pipeline_id")
	private EnrichPipeline enrichPipeline;

	@ManyToMany
	@JoinTable(name = "datasource_tenants",
		joinColumns = @JoinColumn(name = "datasource_id"),
		inverseJoinColumns = @JoinColumn(name = "tenants_id"))
	@ToString.Exclude
	private Set<Tenant> tenants = new LinkedHashSet<>();

	@Column(name = "scheduling", nullable = false)
	@Cron(type = CronType.QUARTZ)
	private String scheduling;

	@Column(name = "last_ingestion_date")
	private OffsetDateTime lastIngestionDate;

	@Column(name = "schedulable")
	private Boolean schedulable = false;

	@ManyToOne
	@JoinColumn(name = "plugin_driver_id")
	private PluginDriver pluginDriver;

}