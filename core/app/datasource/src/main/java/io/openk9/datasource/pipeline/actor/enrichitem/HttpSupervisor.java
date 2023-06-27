package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import io.openk9.datasource.pipeline.actor.Schedulation;

import java.time.LocalDateTime;

public class HttpSupervisor extends AbstractBehavior<HttpSupervisor.Command> {



	public HttpSupervisor(
		ActorContext<Command> context, Schedulation.SchedulationKey key) {

		super(context);
		ClusterSharding clusterSharding = ClusterSharding.get(context.getSystem());
		this.tokenActorRef = clusterSharding.entityRefFor(Token.ENTITY_TYPE_KEY, key.value());
	}

	public static Behavior<Command> create(Schedulation.SchedulationKey key) {
		return Behaviors
			.<Command>supervise(Behaviors.setup(ctx -> new HttpSupervisor(ctx, key)))
			.onFailure(SupervisorStrategy.resume());
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

	private Behavior<Command> onCallback(Callback callback) {
		tokenActorRef.tell(new Token.Callback(callback.tokenId, callback.jsonObject));
		return Behaviors.same();
	}

	private final RecipientRef<Token.Command> tokenActorRef;

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
