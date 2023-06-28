package io.openk9.datasource.model;

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
public class ScheduleId {

	private final String value;

	public ScheduleId(UUID uuid) {
		this.value = uuid.toString();
	}

}
