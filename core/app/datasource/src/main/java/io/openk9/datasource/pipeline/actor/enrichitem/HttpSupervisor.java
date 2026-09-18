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

import java.util.function.Supplier;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.util.CborSerializable;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class HttpSupervisor extends AbstractBehavior<HttpSupervisor.Command> {

	private final ShardingKey processKey;
	private final ActorRef<Processor.Command> pipeline;
	private final Supplier<Behavior<Http.Command>> httpFactory;

	public HttpSupervisor(
		ActorContext<Command> context,
		ShardingKey processKey,
		ActorRef<Processor.Command> pipeline,
		Supplier<Behavior<Http.Command>> httpFactory) {

		super(context);
		this.processKey = processKey;
		this.pipeline = pipeline;
		this.httpFactory = httpFactory;
	}

	/**
	 * Creates the supervisor of the HTTP calls issued by one pipeline entity.
	 *
	 * @param processKey the sharding key of the pipeline entity
	 * @param pipeline the pipeline entity, target of asynchronous callbacks
	 * @param httpFactory factory of the actor performing the HTTP requests
	 * @return the behavior
	 */
	public static Behavior<Command> create(
		ShardingKey processKey,
		ActorRef<Processor.Command> pipeline,
		Supplier<Behavior<Http.Command>> httpFactory) {

		return Behaviors
			.<Command>supervise(Behaviors.setup(ctx -> new HttpSupervisor(
				ctx, processKey, pipeline, httpFactory)))
			.onFailure(SupervisorStrategy.resume());
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Call.class, this::onCall)
			.onMessage(ResponseWrapper.class, wrapper -> {

				HttpProcessor.Response response = wrapper.response;

				ActorRef<Response> replyTo = wrapper.replyTo;

				if (response instanceof HttpProcessor.Body ok) {
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

		ActorRef<HttpProcessor.Command> httpProcessor =
			getContext().spawnAnonymous(HttpProcessor.create(
				call.async, processKey, pipeline, httpFactory));

		ActorRef<HttpProcessor.Response> httpProcessorAdapter =
			getContext().messageAdapter(
				HttpProcessor.Response.class,
				response -> new ResponseWrapper(response, call.replyTo));

		httpProcessor.tell(new HttpProcessor.Start(
			call.url,
			call.jsonObject,
			httpProcessorAdapter
		));

		return Behaviors.same();

	}

	public sealed interface Command extends CborSerializable {}

	public record Call(
		boolean async,
		String url,
		byte[] jsonObject,
		ActorRef<Response> replyTo
	) implements Command {}

	private record ResponseWrapper(
		HttpProcessor.Response response,
		ActorRef<Response> replyTo
	) implements Command {}

	public sealed interface Response extends CborSerializable {}

	public record Body(byte[] jsonObject) implements Response {}

	public record Error(String error) implements Response {}

}
