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
import java.util.EnumSet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToOne;

import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@ToString
@NamedEntityGraphs({
	@NamedEntityGraph(
		name = Scheduler.DATA_INDEXES_ENTITY_GRAPH,
		attributeNodes = {
			@NamedAttributeNode(value = Scheduler_.DATASOURCE),
			@NamedAttributeNode(
				value = Scheduler_.OLD_DATA_INDEX,
				subgraph = "dataIndex-subgraph"
			),
			@NamedAttributeNode(
				value = Scheduler_.NEW_DATA_INDEX,
				subgraph = "dataIndex-subgraph"
			)
		},
		subgraphs = {
			@NamedSubgraph(
				name = "dataIndex-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = DataIndex_.EMBEDDING_DOC_TYPE_FIELD,
						subgraph = "docTypeField-subgraph"
					)
				}
			),
			@NamedSubgraph(
				name = "docTypeField-subgraph",
				attributeNodes = {
					@NamedAttributeNode(value = DocTypeField_.DOC_TYPE),
					@NamedAttributeNode(value = DocTypeField_.ANALYZER),
					@NamedAttributeNode(value = DocTypeField_.PARENT_DOC_TYPE_FIELD),
				}
			)
		}
	),
	@NamedEntityGraph(
		name = Scheduler.ENRICH_ITEMS_ENTITY_GRAPH,
		attributeNodes = {
			@NamedAttributeNode(
				value = Scheduler_.DATASOURCE,
				subgraph = "datasource-subgraph"
			),
			@NamedAttributeNode(
				value = Scheduler_.OLD_DATA_INDEX,
				subgraph = "dataIndex-subgraph"
			),
			@NamedAttributeNode(
				value = Scheduler_.NEW_DATA_INDEX,
				subgraph = "dataIndex-subgraph"
			)
		},
		subgraphs = {
			@NamedSubgraph(
				name = "datasource-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = Datasource_.ENRICH_PIPELINE,
						subgraph = "enrichPipeline-subgraph"
					)
				}
			),
			@NamedSubgraph(
				name = "enrichPipeline-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = EnrichPipeline_.ENRICH_PIPELINE_ITEMS,
						subgraph = "enrichPipelineItems-subgraph"
					)
				}
			),
			@NamedSubgraph(
				name = "enrichPipelineItems-subgraph",
				attributeNodes = {
					@NamedAttributeNode(value = EnrichPipelineItem_.ENRICH_ITEM)
				}
			),
			@NamedSubgraph(
				name = "dataIndex-subgraph",
				attributeNodes = {
					@NamedAttributeNode(
						value = DataIndex_.EMBEDDING_DOC_TYPE_FIELD,
						subgraph = "docTypeField-subgraph")
				}
			),
			@NamedSubgraph(
				name = "docTypeField-subgraph",
				attributeNodes = {
					@NamedAttributeNode(value = DocTypeField_.DOC_TYPE),
					@NamedAttributeNode(value = DocTypeField_.ANALYZER),
					@NamedAttributeNode(value = DocTypeField_.PARENT_DOC_TYPE_FIELD),
				}
			)
		}
	)
})
@NamedQueries({
	@NamedQuery(
		name = Scheduler.FETCH_BY_ID,
		query = "from Scheduler s where s.id = :schedulerId"
	),
	@NamedQuery(
		name = Scheduler.FETCH_BY_SCHEDULE_ID,
		query = "from Scheduler s where s.scheduleId = :scheduleId"
	),
	@NamedQuery(
		name = Scheduler.FETCH_RUNNING,
		query = "from Scheduler s where s.status in " + Scheduler.RUNNING_STATES
	),
})
public class Scheduler extends K9Entity {

	public static final String FETCH_BY_ID = "Scheduler.fetchSchedulerById";
	public static final String FETCH_BY_SCHEDULE_ID = "Scheduler.fetchSchedulerByScheduleId";
	public static final String FETCH_RUNNING = "Scheduler.fetchRunning";
	public static final String ENRICH_ITEMS_ENTITY_GRAPH = "Scheduler.fetchEnrichItems";
	public static final String DATA_INDEXES_ENTITY_GRAPH = "Scheduler.fetchDataIndexes";
	public static final String RUNNING_STATES = "('RUNNING', 'ERROR', 'STALE')";
	public static final EnumSet<SchedulerStatus> RUNNING_STATES_SET = EnumSet.of(
		SchedulerStatus.RUNNING, SchedulerStatus.ERROR, SchedulerStatus.STALE
	);

	@Column(name = "schedule_id", nullable = false, unique = true)
	private String scheduleId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "datasource_id", referencedColumnName = "id")
	@JsonIgnore
	@ToString.Exclude
	private Datasource datasource;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "old_data_index_id", referencedColumnName = "id")
	@JsonIgnore
	@ToString.Exclude
	private DataIndex oldDataIndex;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "new_data_index_id", referencedColumnName = "id")
	@JsonIgnore
	@ToString.Exclude
	private DataIndex newDataIndex;
	@Enumerated(EnumType.STRING)
	private SchedulerStatus status;
	@Column(name = "last_ingestion_date")
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	private OffsetDateTime lastIngestionDate;
	@Column(name = "error_description")
	private String errorDescription;
	@Column(name = "reindex")
	private boolean reindex;

	public enum SchedulerStatus {
		RUNNING,
		FINISHED,
		CANCELLED,
		ERROR,
		STALE,
		FAILURE
	}

	public DataIndex getDataIndex() {
		if (newDataIndex == null) {
			return oldDataIndex;
		}

		return newDataIndex;
	}

}
