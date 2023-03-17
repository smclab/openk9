package io.openk9.datasource.pipeline.actor.common;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.pipeline.util.Util;
import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import java.time.Duration;

public class Http {

	public sealed interface Command {}
	public record GET(ActorRef<Response> replyTo, String url)
		implements Command {}
	public record POST(ActorRef<Response> replyTo, String url, JsonObject body)
		implements Command {}
	public sealed interface Response { byte[] body();}
	public record OK(byte[] body) implements Response {}
	public record ERROR(int statusCode, String statusMessage, byte[] body) implements Response {}

	public static Behavior<Command> create() {
		return Behaviors.setup(
			ctx -> initial(Arc.container().select(WebClient.class).get(), ctx));
	}

	public static Behavior<Command> create(WebClient webClient) {
		return Behaviors.setup(ctx -> initial(webClient, ctx));
	}

	private static Behavior<Command> initial(WebClient webClient, ActorContext<Command> ctx) {

		Duration durationFromActorContext =
			Util.getDurationFromActorContext(
				ctx, "openk9.pipeline.http.timeout",
				() -> Duration.ofSeconds(10));

		long timeout = durationFromActorContext.toMillis();

		return Behaviors
			.receive(Command.class)
			.onMessage(GET.class, get -> onGet(webClient, timeout, get, ctx))
			.onMessage(POST.class, post -> onPost(webClient, timeout, post, ctx))
			.build();
	}

	private static Behavior<Command> onPost(
		WebClient webClient, long timeout, POST post, 
		ActorContext<Command> ctx) {

		_handleResponse(
			ctx, post.replyTo(),
			webClient
				.postAbs(post.url())
				.timeout(timeout)
				.sendJsonObject(post.body())
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onGet(
		WebClient webClient, long timeout, GET get, ActorContext<Command> ctx) {

		_handleResponse(
			ctx, get.replyTo(), 
			webClient
				.getAbs(get.url())
				.timeout(timeout)
				.send()
		);

		return Behaviors.same();

	}

	private static void _handleResponse(
		ActorContext<Command> ctx, ActorRef<Response> responseActorRef,
		Uni<HttpResponse<Buffer>> httpResponseUni) {

		httpResponseUni
			.onItem()
			.transform(resp -> {

				Buffer body = resp.body();

				byte[] bytes = body == null ? new byte[0] : body.getBytes();

				if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
					return new OK(bytes);
				}
				else {
					return new ERROR(resp.statusCode(), resp.statusMessage(), bytes);
				}
			})
			.onFailure()
			.invoke(t -> responseActorRef.tell(new ERROR(500, t.getMessage(), t.getMessage().getBytes())))
			.subscribe()
			.with(responseActorRef::tell);

	}

}
