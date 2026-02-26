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

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;
import org.jboss.logging.Logger;

public class EmbeddingProcessor extends AbstractBehavior<Processor.Command> {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "embedding-processor");

	private static final Logger log = Logger.getLogger(EmbeddingProcessor.class);

	private final ShardingKey processKey;
	private ActorRef<Processor.Response> replyTo;
	private HeldMessage heldMessage;
	private SchedulerDTO scheduler;

	public EmbeddingProcessor(
		ActorContext<Processor.Command> context,
		ShardingKey processKey) {

		super(context);
		this.processKey = processKey;

	}

	public static Behavior<Processor.Command> create(ShardingKey shardingKey) {

		return Behaviors.setup(ctx ->
			new EmbeddingProcessor(ctx, shardingKey)
		);

	}

	@Override
	public Receive<Processor.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Processor.Start.class, this::onStart)
			.onMessage(EmbeddingResponse.class, this::onEmbeddingResponse)
			.build();
	}

	private Behavior<Processor.Command> onEmbeddingResponse(EmbeddingResponse response) {

		if (response.payload() != null) {

			replyTo.tell(new Processor.Success(response.payload, scheduler, heldMessage));

		}
		else if (response.throwable() != null) {

			replyTo.tell(new Processor.Failure(
				new DataProcessException(response.throwable), heldMessage));

		}
		else {

			replyTo.tell(new Processor.Failure(
				new DataProcessException("Empty response"), heldMessage));

		}

		return Behaviors.stopped();
	}

	private Behavior<Processor.Command> onStart(Processor.Start start) {
		var payload = start.ingestPayload();
		this.heldMessage = start.heldMessage();
		this.replyTo = start.replyTo();
		this.scheduler = start.scheduler();

		this.getContext().pipeToSelf(
			EmbeddingService.getEmbeddedPayload(
				processKey.tenantId(), processKey.scheduleId(), payload),
			EmbeddingResponse::new
		);

		return this;
	}

	private record EmbeddingResponse(byte[] payload, Throwable throwable)
		implements Processor.Command {}

}
