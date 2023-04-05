package io.openk9.datasource.pipeline.actor;

import akka.actor.AllDeadLetters;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class DeadLetterListener {

	public static Behavior<AllDeadLetters> create() {
		return Behaviors.setup(ctx ->
			Behaviors
				.receive(AllDeadLetters.class)
				.onAnyMessage(param -> {

					ctx.getLog().info(
						"message: " + param.message() +
						"sender: " + param.sender() +
						"recipient: " + param.recipient()
					);

					return Behaviors.same();

				}).build()
		);
	}

}
