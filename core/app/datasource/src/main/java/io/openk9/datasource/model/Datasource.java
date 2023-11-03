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
import org.eclipse.microprofile.graphql.Description;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Description("Chron quartz expression to define scheduling of datasource")
	@Column(name = "scheduling", nullable = false)
	@Cron(type = CronType.QUARTZ)
	private String scheduling;

	@Description("Last ingestion date of data for current datasource")
	@Column(name = "last_ingestion_date")
	private OffsetDateTime lastIngestionDate;

	@Description("If true set datasource as schedulable")
	@Column(name = "schedulable", nullable = false)
	private Boolean schedulable = false;

	@ToString.Exclude
	@OneToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
	@JoinColumn(name = "data_index_id", referencedColumnName = "id")
	@JsonIgnore
	private DataIndex dataIndex;

	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datasource")
	@JsonIgnore
	private Set<DataIndex> dataIndexes;

	@ToString.Exclude
	@ManyToOne(cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "enrich_pipeline_id")
	@JsonIgnore
	private EnrichPipeline enrichPipeline;

	@ToString.Exclude
	@ManyToOne(
		fetch = FetchType.LAZY,
		cascade = {
			javax.persistence.CascadeType.PERSIST,
			javax.persistence.CascadeType.MERGE,
			javax.persistence.CascadeType.REFRESH,
			javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "plugin_driver_id")
	@JsonIgnore
	private PluginDriver pluginDriver;

	@ManyToMany(cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.DETACH,
		javax.persistence.CascadeType.REFRESH})
	@JoinTable(name = "datasource_buckets",
		joinColumns = @JoinColumn(name = "datasource_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "buckets_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Bucket> buckets = new LinkedHashSet<>();

	@OneToMany(mappedBy = "datasource")
	@ToString.Exclude
	@JsonIgnore
	private Set<Scheduler> schedulers = new LinkedHashSet<>();

	@Lob
	@Column(name = "json_config")
	private String jsonConfig;

	@Description("If true execute reindex on datasource")
	@Column(name = "reindex", nullable = false)
	private Boolean reindex = false;

}