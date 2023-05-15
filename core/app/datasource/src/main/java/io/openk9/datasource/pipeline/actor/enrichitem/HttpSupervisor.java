package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;

import java.time.Duration;
import java.time.LocalDateTime;

public class HttpSupervisor extends AbstractBehavior<HttpSupervisor.Command> {

	public HttpSupervisor(ActorContext<Command> context) {
		super(context);
		ClusterSingleton clusterSingleton =
			ClusterSingleton.get(context.getSystem());

		this.tokenActorRef = clusterSingleton.init(
			SingletonActor.of(
				Token.create(Duration.ofMinutes(15).toMillis()),
				"token")
		);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Callback.class, this::onCallback)
			.onMessage(Call.class, this::onCall)
			.onMessage(ResponseWrapper.class, wrapper -> {

				HttpProcessor.Response response = wrapper.response;

				ActorRef<Response> replyTo = wrapper.replyTo;

				if (response instanceof HttpProcessor.Body) {
					HttpProcessor.Body ok = (HttpProcessor.Body) response;
					replyTo.tell(new Body(ok.body()));
				}
				else {
					HttpProcessor.Error error = (HttpProcessor.Error) response;
					replyTo.tell(new Error(error.message()));
				}

				return Behaviors.same();

			})
			.build();
	}

	private Behavior<Command> onCall(Call call) {

		ActorRef<HttpProcessor.Command> actorRef =
			getContext().spawnAnonymous(
				HttpProcessor.create(call.async, tokenActorRef));

		ActorRef<HttpProcessor.Response> responseActorRef =
			getContext().messageAdapter(
				HttpProcessor.Response.class,
				response -> new ResponseWrapper(response, call.replyTo));

		actorRef.tell(new HttpProcessor.Start(call.url, call.jsonObject, call.expiredDate, responseActorRef));

		return Behaviors.same();

	}

	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.setup(HttpSupervisor::new))
			.onFailure(SupervisorStrategy.resume());
	}

	private Behavior<Command> onCallback(Callback callback) {
		tokenActorRef.tell(new Token.Callback(callback.tokenId, callback.jsonObject));
		return Behaviors.same();
	}

	private final ActorRef<Token.Command> tokenActorRef;
	public sealed interface Command {}
	public record Call(
		boolean async, String url, byte[] jsonObject,
		LocalDateTime expiredDate, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String tokenId, byte[] jsonObject) implements Command {}
	private record ResponseWrapper(HttpProcessor.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record Body(byte[] jsonObject) implements Response {}
	public record Error(String error) implements Response {}


}
