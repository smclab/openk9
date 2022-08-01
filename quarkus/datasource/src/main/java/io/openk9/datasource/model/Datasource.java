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
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
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
@Cacheable
public class Datasource extends K9Entity {

	@Column(name = "scheduling", nullable = false)
	@Cron(type = CronType.QUARTZ)
	private String scheduling;

	@Column(name = "last_ingestion_date")
	private OffsetDateTime lastIngestionDate;

	@Column(name = "schedulable")
	private Boolean schedulable = false;

	@ToString.Exclude
	@OneToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "data_index_id")
	@JsonIgnore
	private DataIndex dataIndex;

	@ToString.Exclude
	@OneToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "entity_index_id")
	@JsonIgnore
	private EntityIndex entityIndex;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "enrich_pipeline_id")
	@JsonIgnore
	private EnrichPipeline enrichPipeline;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "plugin_driver_id")
	@JsonIgnore
	private PluginDriver pluginDriver;

	@ManyToMany(cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.DETACH,
		javax.persistence.CascadeType.REFRESH})
	@JoinTable(name = "datasource_tenants",
		joinColumns = @JoinColumn(name = "datasource_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "tenants_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Tenant> tenants = new LinkedHashSet<>();

	@Lob
	@Column(name = "json_config")
	private String jsonConfig;

	public void addTenant(Tenant tenant) {
		this.tenants.add(tenant);
		tenant.getDatasources().add(this);
	}

	public void removeTenant(Tenant tenant) {
		this.tenants.remove(tenant);
		tenant.getDatasources().remove(this);
	}

}