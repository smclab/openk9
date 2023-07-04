package io.openk9.datasource.actor;

import akka.actor.typed.javadsl.ActorContext;

import java.util.function.Consumer;

public interface ActorSystemBehaviorInitializer extends Consumer<ActorContext<?>> {
}
