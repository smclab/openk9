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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
@ToString
@NamedQueries({
	@NamedQuery(
		name = Scheduler.FETCH_SCHEDULATION_QUERY,
		query = "select s " +
			"from Scheduler s " +
			"join fetch s.datasource d " +
			"left join fetch d.enrichPipeline ep " +
			"left join fetch ep.enrichPipelineItems epi " +
			"left join fetch epi.enrichItem " +
			"left join fetch s.oldDataIndex " +
			"left join fetch s.newDataIndex " +
			"where s.scheduleId = :scheduleId"
	),
	@NamedQuery(
		name = Scheduler.FETCH_RUNNING_QUERY,
		query = "from Scheduler s where s.status in ('STARTED', 'ERROR')"
	)

})
public class Scheduler extends K9Entity {

	public static final String FETCH_SCHEDULATION_QUERY = "Scheduler.fetchSchedulation";
	public static final String FETCH_RUNNING_QUERY = "Scheduler.fetchRunning";

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
