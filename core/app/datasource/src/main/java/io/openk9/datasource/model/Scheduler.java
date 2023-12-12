package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

	public static final String FETCH_BY_SCHEDULE_ID = "Scheduler.fetchSchedulation";
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

	public enum SchedulerStatus {
		STARTED,
		FINISHED,
		CANCELLED,
		ERROR
	}
}
