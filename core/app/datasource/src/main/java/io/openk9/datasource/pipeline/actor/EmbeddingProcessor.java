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

package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.jayway.jsonpath.JsonPath;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

public class EmbeddingProcessor extends AbstractBehavior<Processor.Command> {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "embedding-processor");

	private static final Logger log = Logger.getLogger(EmbeddingProcessor.class);

	private final ShardingKey processKey;
	private final ActorRef<HttpSupervisor.Command> httpSupervisor;
	private final Deque<Processor.Command> lag = new ArrayDeque<>();
	private final ActorRef<HttpSupervisor.Response> httpSupervisorAdapter;
	private ActorRef<Processor.Response> replyTo;
	private HeldMessage heldMessage;
	private String apiUrl;
	private String apiKey;
	private String jsonPath;

	public EmbeddingProcessor(
		ActorContext<Processor.Command> context,
		ShardingKey processKey) {

		super(context);
		this.processKey = processKey;
		this.httpSupervisor = getContext().spawnAnonymous(
			HttpSupervisor.create(processKey.baseKey()));
		this.httpSupervisorAdapter = getContext().messageAdapter(
			HttpSupervisor.Response.class, HttpResponse::new);

	}

	@Override
	public Receive<Processor.Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Setup.INSTANCE, this::onSetup)
			.onAnyMessage(this::enqueue)
			.build();
	}

	public Behavior<Processor.Command> ready() {
		for (Processor.Command command : lag) {
			getContext().getSelf().tell(command);
		}

		return newReceiveBuilder()
			.onMessage(Processor.Start.class, this::onStart)
			.onMessage(HttpResponse.class, this::onHttpResponse)
			.build();
	}

	private Behavior<Processor.Command> enqueue(Processor.Command command) {
		lag.add(command);
		return this;
	}

	private Behavior<Processor.Command> onSetup() {
		// TODO gets config by shardingKey (tenant, schedule)
		// apiUrl, apiKey, JsonPath for field, Chunk Size, Chunk Type

		return ready();
	}

	private Behavior<Processor.Command> onStart(Processor.Start start) {
		var payload = start.ingestPayload();
		this.heldMessage = start.heldMessage();
		this.replyTo = start.replyTo();

		httpRequestCall(payload);

		return this;
	}

	private void httpRequestCall(byte[] payload) {
		var embeddingRequest = map(payload);
		var jsonObject = Json.encode(embeddingRequest).getBytes();

		httpSupervisor.tell(new HttpSupervisor.Call(
			true,
			apiUrl,
			jsonObject,
			LocalDateTime.now().plusMinutes(5),
			this.httpSupervisorAdapter
		));
	}

	private Behavior<Processor.Command> onHttpResponse(HttpResponse httpResponse) {

		var response = httpResponse.response();

		if (response instanceof HttpSupervisor.Body body) {

			var payload = body.jsonObject();

			replyTo.tell(new Processor.Success(payload, heldMessage));

		}
		else if (response instanceof HttpSupervisor.Error error) {

			replyTo.tell(new Processor.Failure(
				new DataProcessException(error.error()),
				heldMessage
			));

		}

		return Behaviors.stopped();
	}

	private EmbeddingRequest map(byte[] payload) {
		var chunk = new Chunk(2, ChunkType.DEFAULT);
		var json = new String(payload);

		String text = JsonPath.read(json, jsonPath);

		return new EmbeddingRequest(chunk, apiKey, text);
	}

	private enum Setup implements Processor.Command {
		INSTANCE
	}

	private enum ChunkType {
		DEFAULT
	}

	private record HttpResponse(HttpSupervisor.Response response)
		implements Processor.Command {}

	private record EmbeddingRequest(Chunk chunk, String apiKey, String text) {}

	private record Chunk(int size, ChunkType chunkType) {}

}
