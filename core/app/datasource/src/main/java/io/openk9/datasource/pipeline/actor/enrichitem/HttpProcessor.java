/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.openk9.datasource.pipeline.actor.enrichitem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.web.dto.EnricherInputDTO;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

/**
 * Performs one HTTP call to an enricher. For a synchronous enricher the
 * response body is the result; for an asynchronous one the result arrives
 * later as a {@link Callback}, routed by the owning pipeline entity through
 * the {@link CallbackToken} this actor puts in the request.
 */
public class HttpProcessor extends AbstractBehavior<HttpProcessor.Command> {

	private static final byte[] EMPTY_JSON = new byte[] {123, 125}; // "{}"

	private final boolean async;
	private final ShardingKey processKey;
	private final ActorRef<Processor.Command> pipeline;
	private final Supplier<Behavior<Http.Command>> httpFactory;
	private byte[] earlyCallback;

	public HttpProcessor(
		ActorContext<Command> context,
		boolean async,
		ShardingKey processKey,
		ActorRef<Processor.Command> pipeline,
		Supplier<Behavior<Http.Command>> httpFactory) {

		super(context);
		this.async = async;
		this.processKey = processKey;
		this.pipeline = pipeline;
		this.httpFactory = httpFactory;
	}

	/**
	 * Creates the behavior of a processor for one enricher call.
	 *
	 * @param async whether the enricher answers through a callback
	 * @param processKey the sharding key of the owning pipeline entity
	 * @param pipeline the owning pipeline entity, target of the callback
	 * @param httpFactory factory of the actor performing the HTTP request
	 * @return the behavior
	 */
	public static Behavior<Command> create(
		boolean async,
		ShardingKey processKey,
		ActorRef<Processor.Command> pipeline,
		Supplier<Behavior<Http.Command>> httpFactory) {

		return Behaviors.setup(ctx -> new HttpProcessor(
			ctx, async, processKey, pipeline, httpFactory));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}

	private Behavior<Command> onStart(Start start) {

		ResourceUri resourceUri = start.resourceUri;
		ActorRef<Response> replyTo = start.replyTo;

		if (!isValidUrl(resourceUri)) {
			replyTo.tell(new Error("Invalid URL: " + resourceUri));
			return Behaviors.stopped();
		}

		EnricherInputDTO enricherInputDTO = start.enricherInputDTO;

		if (async) {
			var token = CallbackToken.generate(processKey);

			enricherInputDTO.setReplyTo(token.encode());

			// the pipeline must know the awaited nonce before the enricher
			// can possibly call back, hence the registration precedes the POST
			pipeline.tell(new RegisterCallback(token.nonce(), getContext().getSelf()));
		}

		ActorRef<Http.Response> responseActorRef =
			getContext().messageAdapter(
				Http.Response.class,
				param -> new ResponseWrapper(param, replyTo)
			);

		ActorRef<Http.Command> commandActorRef =
			getContext().spawnAnonymous(httpFactory.get());

		commandActorRef.tell(
			new Http.POST(responseActorRef, resourceUri, enricherInputDTO));

		// the 200 and the callback travel on different connections: an
		// enricher that calls back at once can beat its own 200, so the
		// callback is kept until the response arrives
		return newReceiveBuilder()
			.onMessage(ResponseWrapper.class, this::onResponseWrapper)
			.onMessage(Callback.class, callback -> {
				earlyCallback = callback.body();
				return Behaviors.same();
			})
			.build();
	}

	private Behavior<Command> onResponseWrapper(ResponseWrapper responseWrapper) {

		Http.Response response = responseWrapper.response;
		ActorRef<Response> replyTo = responseWrapper.replyTo;

		if (!(response instanceof Http.OK ok)) {
			replyTo.tell(new Error(response.toString()));
			return Behaviors.stopped();
		}

		if (async) {
			if (earlyCallback != null) {
				replyTo.tell(new Body(earlyCallback));
				return Behaviors.stopped();
			}

			return newReceiveBuilder()
				.onMessage(Callback.class, callback -> {
					replyTo.tell(new Body(callback.body()));
					return Behaviors.stopped();
				})
				.build();
		}

		byte[] body = ok.body();

		replyTo.tell(new Body(body == null || body.length == 0 ? EMPTY_JSON : body));

		return Behaviors.stopped();
	}

	private static boolean isValidUrl(ResourceUri resourceUri) {
		var uri = resourceUri.getPath() != null
			? resourceUri.getBaseUri() + resourceUri.getPath()
			: resourceUri.getBaseUri();

		try {
			new URL(uri);
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
	}

	public sealed interface Command extends CborSerializable {}

	public record Start(
		ResourceUri resourceUri,
		EnricherInputDTO enricherInputDTO,
		ActorRef<Response> replyTo
	) implements Command {}

	/**
	 * The body posted by the asynchronous enricher, delivered by the pipeline.
	 *
	 * @param body the callback body
	 */
	public record Callback(byte[] body) implements Command {}

	private record ResponseWrapper(
		Http.Response response,
		ActorRef<Response> replyTo
	) implements Command {}

	/**
	 * Registration of the nonce this processor awaits, sent to the owning
	 * pipeline before the enricher is called.
	 *
	 * @param nonce the nonce carried by the token given to the enricher
	 * @param waiter the processor to hand the callback body to
	 */
	public record RegisterCallback(
		String nonce,
		ActorRef<Command> waiter
	) implements Processor.Command {}

	public sealed interface Response extends CborSerializable {}

	public record Error(String message) implements Response {}

	public record Body(byte[] body) implements Response {}

}
