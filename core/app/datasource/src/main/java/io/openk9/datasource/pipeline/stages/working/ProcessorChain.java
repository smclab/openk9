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

package io.openk9.datasource.pipeline.stages.working;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;

public class ProcessorChain extends AbstractBehavior<Processor.Command> {

	private final LinkedList<EntityTypeKey<Processor.Command>> processorTypeKeys;
	private final ActorRef<Processor.Response> processorResponseAdapter;
	private final ClusterSharding sharding;
	private SchedulerDTO scheduler;
	private ActorRef<Processor.Response> replyTo;

	public ProcessorChain(
		ActorContext<Processor.Command> context,
		LinkedList<EntityTypeKey<Processor.Command>> processorTypeKeys) {

		super(context);

		this.processorTypeKeys = processorTypeKeys;
		this.processorResponseAdapter = getContext().messageAdapter(
			Processor.Response.class, ProcessorResponse::new
		);
		this.sharding = ClusterSharding.get(getContext().getSystem());
	}

	public static Behavior<Processor.Command> create(
		LinkedList<EntityTypeKey<Processor.Command>> processorTypeKeys) {

		return Behaviors.setup(
			context -> new ProcessorChain(context, processorTypeKeys));
	}

	@Override
	public Receive<Processor.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Processor.Start.class, this::onStart)
			.onMessage(ProcessorResponse.class, this::onProcessorResponse)
			.build();
	}

	private Behavior<Processor.Command> onProcessorResponse(
		ProcessorResponse processorResponse) {

		var response = processorResponse.response();

		switch (response) {
			case Processor.Failure failure -> replyTo.tell(failure);
			case Processor.Skip skip -> replyTo.tell(skip);
			case Processor.Success success -> {
				try {
					var processKey = success.heldMessage().processKey();
					var processorTypeKey = this.processorTypeKeys.pop();

					var processor = sharding.entityRefFor(
						processorTypeKey,
						processKey.toString()
					);

					processor.tell(new Processor.Start(
						success.payload(),
						scheduler,
						success.heldMessage(),
						processorResponseAdapter
					));

				}
				catch (NoSuchElementException e) {
					replyTo.tell(new Processor.Success(
						success.payload(),
						success.scheduler(),
						success.heldMessage()
					));
				}
			}
		}

		return Behaviors.same();
	}

	private Behavior<Processor.Command> onStart(Processor.Start start) {

		this.scheduler = start.scheduler();
		this.replyTo = start.replyTo();
		var heldMessage = start.heldMessage();

		var processorType = this.processorTypeKeys.pop();

		var processor = sharding.entityRefFor(
			processorType, heldMessage.processKey().toString());

		processor.tell(new Processor.Start(
			start.ingestPayload(),
			scheduler,
			heldMessage,
			processorResponseAdapter
		));

		return Behaviors.same();
	}

	private record ProcessorResponse(Processor.Response response)
		implements Processor.Command {}
}
