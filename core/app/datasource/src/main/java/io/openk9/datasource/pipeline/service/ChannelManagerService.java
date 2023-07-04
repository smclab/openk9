package io.openk9.datasource.pipeline.service;

import akka.actor.typed.ActorRef;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.ChannelManager;

public record ChannelManagerService(ActorRef<ChannelManager.Command> channelManager) {

	public void queueSpawn(String tenantId, String scheduleId) {
		channelManager.tell(
			new ChannelManager.QueueSpawn(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

	public void queueDestroy(String tenantId, String scheduleId) {
		channelManager.tell(
			new ChannelManager.QueueDestroy(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

}
