package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.processor.payload.DataPayload;

public class Ingestion {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	public sealed interface Response {}
	public record IngestionGenerated(String ingestion) implements Response {}

	public static Behavior<Command> create(DataPayload dataPayload) {
		return Behaviors.setup(ctx -> initial(ctx, dataPayload));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, DataPayload dataPayload) {

		return Behaviors.receive(Command.class)
			.onMessage(Start.class, start -> {


				return Behaviors.same();

			})
			.build();

	}

}
