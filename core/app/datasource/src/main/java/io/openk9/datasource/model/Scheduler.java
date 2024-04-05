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
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
@ToString
@NamedEntityGraphs({
	@NamedEntityGraph(
		name = Scheduler.ENRICH_ITEMS_ENTITY_GRAPH,
		attributeNodes = {
			@NamedAttributeNode(
				value = "datasource",
				subgraph = "datasource-subgraph"
			),
			@NamedAttributeNode(value = "oldDataIndex"),
			@NamedAttributeNode(value = "newDataIndex")
		},
		subgraphs = {
			@NamedSubgraph(
				name = "datasource-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = "enrichPipeline",
						subgraph = "enrichPipeline-subgraph"
					)
				}
			),
			@NamedSubgraph(
				name = "enrichPipeline-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = "enrichPipelineItems",
						subgraph = "enrichPipelineItems-subgraph"
					)
				}
			),
			@NamedSubgraph(
				name = "enrichPipelineItems-subgraph",
				attributeNodes = {
					@NamedAttributeNode(value = "enrichItem")
				}
			)
		}
	)
})
@NamedQueries({
	@NamedQuery(
		name = Scheduler.FETCH_BY_SCHEDULE_ID,
		query = "from Scheduler s where s.scheduleId = :scheduleId"
	),
	@NamedQuery(
		name = Scheduler.FETCH_RUNNING_QUERY,
		query = "from Scheduler s where s.status in ('STARTED', 'ERROR')"
	)
})
public class Scheduler extends K9Entity {

	public static final String FETCH_BY_SCHEDULE_ID = "Scheduler.fetchScheduling";
	public static final String FETCH_RUNNING_QUERY = "Scheduler.fetchRunning";
	public static final String ENRICH_ITEMS_ENTITY_GRAPH = "Scheduler.fetchEnrichItems";

	@Column(name = "schedule_id", nullable = false, unique = true)
	private String scheduleId;
	@ManyToOne
	@JoinColumn(name = "datasource_id", referencedColumnName = "id")
	@JsonIgnore
	private Datasource datasource;
	@OneToOne
	@JoinColumn(name = "old_data_index_id", referencedColumnName = "id")
	@JsonIgnore
	private DataIndex oldDataIndex;
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "new_data_index_id", referencedColumnName = "id")
	@JsonIgnore
	private DataIndex newDataIndex;
	@Enumerated(EnumType.STRING)
	private SchedulerStatus status;
	@Column(name = "last_ingestion_date")
	private OffsetDateTime lastIngestionDate;
	public enum SchedulerStatus {
		STARTED,
		FINISHED,
		CANCELLED,
		ERROR
	}
}
