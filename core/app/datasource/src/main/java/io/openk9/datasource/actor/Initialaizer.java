package io.openk9.datasource.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

final class Initialaizer {

    public sealed interface Command {}
    private record Nothing() implements Command {}

    public static Behavior<Command> create(
        Iterable<ActorSystemBehaviorInitializer> actorSystemBehaviorInitializers) {

        return Behaviors.setup(ctx -> {

            for (ActorSystemBehaviorInitializer actorSystemBehaviorInitializer : actorSystemBehaviorInitializers) {
                actorSystemBehaviorInitializer.accept(ctx);
            }

            return Behaviors.empty();

        });

    }

}
