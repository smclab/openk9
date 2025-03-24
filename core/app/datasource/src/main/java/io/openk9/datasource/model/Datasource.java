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

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import io.openk9.datasource.listener.K9EntityListener;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.validation.ValidQuartzCron;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.graphql.Description;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "datasource")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EntityListeners(K9EntityListener.class)
public class Datasource extends K9Entity {

	private static final Boolean DEFAULT_PURGEABLE = false;
	private static final String DEFAULT_PURGING = "0 0 1 * * ?";
	private static final String DEFAULT_PURGE_MAX_AGE = "2d";
	private static final Boolean DEFAULT_REINDEXABLE = false;
	private static final String DEFAULT_REINDEXING = "0 0 1 * * ?";
	private static final Boolean DEFAULT_SCHEDULABLE = false;
	private static final String DEFAULT_SCHEDULING = "0 */30 * ? * * *";

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.DETACH,
		jakarta.persistence.CascadeType.REFRESH
	}
	)
	@JoinTable(name = "datasource_buckets",
		joinColumns = @JoinColumn(name = "datasource_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "buckets_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Bucket> buckets = new LinkedHashSet<>();
	@ToString.Exclude
	@OneToOne(
		fetch = jakarta.persistence.FetchType.LAZY,
		cascade = {
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.DETACH,
			jakarta.persistence.CascadeType.REFRESH
		}
	)
	@JoinColumn(name = "data_index_id", referencedColumnName = "id")
	@JsonIgnore
	private DataIndex dataIndex;
	@ToString.Exclude
	@OneToMany(
		cascade = {
			CascadeType.PERSIST,
			CascadeType.MERGE,
			CascadeType.DETACH,
			CascadeType.REFRESH
		},
		mappedBy = "datasource"
	)
	@JsonIgnore
	private Set<DataIndex> dataIndexes;
	@Column(name = "description", length = 4096)
	private String description;
	@ToString.Exclude
	@ManyToOne(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@JoinColumn(name = "enrich_pipeline_id")
	@JsonIgnore
	private EnrichPipeline enrichPipeline;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;
	@Description("Last ingestion date of data for current datasource")
	@Column(name = "last_ingestion_date")
	private OffsetDateTime lastIngestionDate;
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@ToString.Exclude
	@ManyToOne(
		fetch = FetchType.LAZY,
		cascade = {
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.REFRESH,
			jakarta.persistence.CascadeType.DETACH
		}
	)
	@JoinColumn(name = "plugin_driver_id")
	@JsonIgnore
	private PluginDriver pluginDriver;
	@Description("If true set active the purge job scheduling")
	@Column(name = "purgeable")
	private Boolean purgeable = DEFAULT_PURGEABLE;
	@Description("Chron quartz expression to define purging for this datasource")
	@Column(name = "purging")
	@ValidQuartzCron
	private String purging = DEFAULT_PURGING;
	@Description("The duration to identify orphaned Dataindex.")
	@Column(name = "purge_max_age")
	private String purgeMaxAge = DEFAULT_PURGE_MAX_AGE;
	@Description("If true set datasource as reindexable")
	@Column(name = "reindexable", nullable = false)
	private Boolean reindexable = DEFAULT_REINDEXABLE;
	@Description("Chron quartz expression to define reindexing of datasource")
	@Column(name = "reindexing", nullable = false)
	@ValidQuartzCron
	private String reindexing = DEFAULT_REINDEXING;
	@Description("If true set datasource as schedulable")
	@Column(name = "schedulable", nullable = false)
	private Boolean schedulable = DEFAULT_SCHEDULABLE;
	@OneToMany(mappedBy = "datasource")
	@ToString.Exclude
	@JsonIgnore
	private Set<Scheduler> schedulers = new LinkedHashSet<>();
	@Description("Chron quartz expression to define scheduling of datasource")
	@Column(name = "scheduling", nullable = false)
	@ValidQuartzCron
	private String scheduling = DEFAULT_SCHEDULING;
	@Column(name = "pipeline_type")
	@Enumerated(EnumType.STRING)
	private PipelineType pipelineType;


	public void setPurgeable(Boolean purgeable) {
		this.purgeable = Objects.requireNonNullElse(purgeable, DEFAULT_PURGEABLE);
	}

	public void setPurgeMaxAge(String purgeMaxAge) {
		this.purgeMaxAge = Objects.requireNonNullElse(purgeMaxAge, DEFAULT_PURGE_MAX_AGE);
	}

	public void setPurging(String purging) {
		this.purging = Objects.requireNonNullElse(purging, DEFAULT_PURGING);
	}

	public void setReindexable(Boolean reindexable) {
		this.reindexable = Objects.requireNonNullElse(reindexable, DEFAULT_REINDEXABLE);
	}

	public void setReindexing(String reindexing) {
		this.reindexing = Objects.requireNonNullElse(reindexing, DEFAULT_REINDEXING);
	}

	public void setSchedulable(Boolean schedulable) {
		this.schedulable = Objects.requireNonNullElse(schedulable, DEFAULT_SCHEDULABLE);
	}

	public void setScheduling(String scheduling) {
		this.scheduling = Objects.requireNonNullElse(scheduling, DEFAULT_SCHEDULING);
	}
}