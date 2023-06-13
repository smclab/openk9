package io.openk9.datasource.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ScheduleId {

	private final String value;

	public ScheduleId(UUID uuid) {
		this.value = uuid.toString();
	}

}
