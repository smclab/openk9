package io.openk9.datasource.actor;

import akka.actor.typed.ActorSystem;

public interface ActorSystemInitializer {
	void init(ActorSystem<?> actorSystem);
}
