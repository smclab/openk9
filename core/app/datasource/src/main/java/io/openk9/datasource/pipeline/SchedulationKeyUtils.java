package io.openk9.datasource.pipeline;

import io.openk9.datasource.pipeline.actor.Schedulation;

public class SchedulationKeyUtils {

	public static String getValue(Schedulation.SchedulationKey key) {
		return getValue(key.tenantId(), key.scheduleId());
	}

	public static String getValue(String tenantId, String scheduleId) {
		return tenantId + "#" + scheduleId;
	}

	public static Schedulation.SchedulationKey getKey(
		String tenantId, String scheduleId) {

		return new Schedulation.SchedulationKey(tenantId, scheduleId);
	}
}
