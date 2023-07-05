package io.openk9.datasource.pipeline.service;

import akka.actor.typed.ActorRef;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.MessageGateway;

public record MessageGatewayService(ActorRef<MessageGateway.Command> channelManager) {

	public void queueSpawn(String tenantId, String scheduleId) {
		channelManager.tell(
			new MessageGateway.Register(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

	public void queueDestroy(String tenantId, String scheduleId) {
		channelManager.tell(
			new MessageGateway.Deregister(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

}
