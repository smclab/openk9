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

package io.openk9.ingestion.rabbitmq.example;

import io.openk9.ingestion.api.OutboundMessage;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.ingestion.api.SenderReactor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Component(
	immediate = true,
	service = Object.class,
	property = {
		"osgi.command.function=sender",
		"osgi.command.function=receive",
		"osgi.command.scope=projectq-rabbitmq"
	}
)
public class RabbitGOGOCommand {

	public void sender(String message, String queue) {

		_log.info("message: " + message + ", queue: " + queue);

		Flux<OutboundMessage> outboundFlux = Flux.range(1, 10)
			.map(i -> _outboundMessageFactory.createOutboundMessage(
				"amq.direct",
				"routing.key", (message + i).getBytes()
			));

		_sender.send(outboundFlux)
			.doOnError(e ->
				_log.error("Send failed", e))
			.subscribe();
	}

	public void receive() {
		_receiver
			.consumeAutoAck("test")
			.subscribe(
				delivery -> _log.info(
					String.format(
						"%s, props: %s", new String(delivery.getBody()),
					delivery.getProperties().toString())));
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private OutboundMessageFactory _outboundMessageFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ReceiverReactor _receiver;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private SenderReactor _sender;

	private static final Logger _log = LoggerFactory.getLogger(
		RabbitGOGOCommand.class);


}
