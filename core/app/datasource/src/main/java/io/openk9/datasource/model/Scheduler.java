package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.ScheduleIdConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class Scheduler extends K9Entity {

	@Convert(converter = ScheduleIdConverter.class)
	@Column(name = "schedule_id", nullable = false, unique = true)
	private ScheduleId scheduleId;
	@ManyToOne
	@JoinColumn(name = "datasource_id", referencedColumnName = "id")
	private Datasource datasource;
	@OneToOne
	@JoinColumn(name = "old_data_index_id", referencedColumnName = "id")
	private DataIndex oldDataIndex;
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "new_data_index_id", referencedColumnName = "id")
	private DataIndex newDataIndex;
	@Enumerated(EnumType.STRING)
	private SchedulerStatus status;

	public enum SchedulerStatus {
		STARTED,
		FINISHED
	}
}
