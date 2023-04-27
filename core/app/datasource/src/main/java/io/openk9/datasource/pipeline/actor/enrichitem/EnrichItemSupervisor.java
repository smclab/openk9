package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.model.EnrichItem;
import io.vertx.core.json.JsonObject;

public class EnrichItemSupervisor {

	public sealed interface Command {}
	public record Execute(EnrichItem enrichItem, JsonObject dataPayload, ActorRef<Response> replyTo) implements Command {}
	private record HttpSupervisorWrapper(HttpSupervisor.Response response, ActorRef<Response> replyTo) implements Command {}
	private record GroovySupervisorWrapper(GroovyActor.Response response, ActorRef<Response> replyTo) implements Command {}
	private record GroovyValidatorWrapper(
		GroovyActor.Response response, EnrichItem enrichItem,
		JsonObject dataPayload, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record Body(byte[] body) implements Response {}
	public record Error(String error) implements Response {}

	public static Behavior<Command> create(ActorRef<HttpSupervisor.Command> httpSupervisor) {
		return Behaviors.setup(ctx -> initial(httpSupervisor, ctx));
	}

	private static Behavior<Command> initial(
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorContext<Command> ctx) {
		return Behaviors.receive(Command.class)
			.onMessage(Execute.class, execute -> onExecute(httpSupervisor, execute, ctx))
			.onMessage(HttpSupervisorWrapper.class, hrw -> onHttpResponse(hrw, ctx))
			.onMessage(GroovySupervisorWrapper.class, grw -> onGroovyResponse(grw, ctx))
			.onMessage(GroovyValidatorWrapper.class, gvw -> onGroovyValidatorResponse(gvw, httpSupervisor, ctx))
			.build();
	}

	private static Behavior<Command> onGroovyValidatorResponse(
		GroovyValidatorWrapper gvw,
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorContext<Command> ctx) {

		GroovyActor.Response response = gvw.response;
		EnrichItem enrichItem = gvw.enrichItem;
		JsonObject dataPayload = gvw.dataPayload;

		if (response instanceof GroovyActor.GroovyValidateResponse) {
			GroovyActor.GroovyValidateResponse groovyValidateResponse =
				(GroovyActor.GroovyValidateResponse)response;

			if (groovyValidateResponse.valid()) {

				ActorRef<HttpSupervisor.Response> responseActorRef =
					ctx.messageAdapter(
						HttpSupervisor.Response.class,
						hsr -> new HttpSupervisorWrapper(hsr, gvw.replyTo)
					);

				httpSupervisor.tell(
					new HttpSupervisor.Call(
						enrichItem.getType() == EnrichItem.EnrichItemType.HTTP_ASYNC,
						enrichItem.getServiceName(),
						dataPayload.toBuffer().getBytes(),
						responseActorRef
					)
				);

				return Behaviors.same();

			}

		}
		else if (response instanceof GroovyActor.GroovyError groovyError) {
			gvw.replyTo.tell(new Error(groovyError.error()));
		}
		else {
			gvw.replyTo.tell(new Error("Unexpected Groovy Response"));
		}


		return Behaviors.stopped();

	}

	private static Behavior<Command> onGroovyResponse(
		GroovySupervisorWrapper grw, ActorContext<Command> ctx) {

		GroovyActor.Response response = grw.response;

		ActorRef<Response> replyTo = grw.replyTo;

		if (response instanceof GroovyActor.GroovyResponse groovyResponse) {
			replyTo.tell(new Body(groovyResponse.response()));
		}
		else if (response instanceof GroovyActor.GroovyError groovyError) {
			replyTo.tell(new Error(groovyError.error()));
		}

		return Behaviors.stopped();
	}

	private static Behavior<Command> onHttpResponse(
		HttpSupervisorWrapper hrw, ActorContext<Command> ctx) {

		HttpSupervisor.Response response = hrw.response;
		ActorRef<Response> replyTo = hrw.replyTo;

		if (response instanceof HttpSupervisor.Body body) {
			replyTo.tell(new Body(body.jsonObject()));
		}
		else if (response instanceof HttpSupervisor.Error error) {
			replyTo.tell(new Error(error.error()));
		}

		return Behaviors.stopped();

	}

	private static Behavior<Command> onExecute(
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		Execute execute, ActorContext<Command> ctx) {

		EnrichItem enrichItem = execute.enrichItem;
		JsonObject dataPayload = execute.dataPayload;
		ActorRef<Response> replyTo = execute.replyTo;

		ctx.getLog().info(
			"Execute enrichItemId: {}, type: {}, replyTo: {}", enrichItem.getId(), enrichItem.getType(), replyTo);

		String jsonConfig = enrichItem.getJsonConfig();

		JsonObject enrichItemConfig =
			jsonConfig == null || jsonConfig.isBlank()
				? new JsonObject()
				: new JsonObject(jsonConfig);

		JsonObject payload = JsonObject.of(
			"payload", dataPayload,
			"enrichItemConfig", enrichItemConfig
		);

		switch (enrichItem.getType()) {
			case HTTP_ASYNC, HTTP_SYNC -> onHttpEnrichItem(enrichItem, payload, replyTo, httpSupervisor, ctx);
			case GROOVY_SCRIPT -> onGroovyEnrichItem(enrichItem, payload, replyTo, ctx);
		}

		return Behaviors.same();
	}

	private static void onGroovyEnrichItem(
		EnrichItem enrichItem, JsonObject dataPayload, ActorRef<Response> replyTo,
		ActorContext<Command> ctx) {

		ActorRef<GroovyActor.Response> responseActorRef =
			ctx.messageAdapter(
				GroovyActor.Response.class,
				response -> new GroovySupervisorWrapper(response, replyTo)
			);

		ActorRef<GroovyActor.Command> groovyActor =
			ctx.spawnAnonymous(GroovyActor.create());

		groovyActor.tell(
			new GroovyActor.Execute(
				dataPayload, enrichItem.getScript(),
				responseActorRef)
		);

	}

	private static void onHttpEnrichItem(
		EnrichItem enrichItem, JsonObject dataPayload,
		ActorRef<Response> replyTo, ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorContext<Command> ctx) {

		ActorRef<HttpSupervisor.Response> responseActorRef =
			ctx.messageAdapter(
				HttpSupervisor.Response.class,
				response -> new HttpSupervisorWrapper(response, replyTo)
			);

		String validationScript = enrichItem.getScript();

		if (validationScript != null && !validationScript.isBlank()) {

			ActorRef<GroovyActor.Response> groovyValidatorRef =
				ctx.messageAdapter(
					GroovyActor.Response.class,
					response -> new GroovyValidatorWrapper(response, enrichItem, dataPayload, replyTo)
				);

			ActorRef<GroovyActor.Command> groovyActor =
				ctx.spawnAnonymous(GroovyActor.create());

			groovyActor.tell(new GroovyActor.Validate(dataPayload, validationScript, groovyValidatorRef));

			return;

		}

		httpSupervisor.tell(
			new HttpSupervisor.Call(
				enrichItem.getType() == EnrichItem.EnrichItemType.HTTP_ASYNC,
				enrichItem.getServiceName(),
				dataPayload.toBuffer().getBytes(),
				responseActorRef
			)
		);

	}

}
