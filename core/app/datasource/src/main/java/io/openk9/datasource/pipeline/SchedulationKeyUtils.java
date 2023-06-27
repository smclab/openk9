package io.openk9.datasource.pipeline;

import io.openk9.datasource.model.ScheduleId;
import io.openk9.datasource.pipeline.actor.Schedulation;

import java.util.UUID;

public class SchedulationKeyUtils {

	public static String getValue(Schedulation.SchedulationKey key) {
		return getValue(key.tenantId(), key.scheduleId().getValue());
	}

	public static String getValue(String tenantId, String scheduleId) {
		return tenantId + "#" + scheduleId;
	}

	public static Schedulation.SchedulationKey getKey(
		String tenantId, String scheduleId) {

		return new Schedulation.SchedulationKey(
			tenantId, new ScheduleId(UUID.fromString(scheduleId)));
	}
}
