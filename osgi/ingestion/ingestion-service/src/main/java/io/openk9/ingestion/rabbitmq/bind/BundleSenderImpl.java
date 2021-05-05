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

package io.openk9.ingestion.rabbitmq.bind;

import io.openk9.ingestion.api.BundleSender;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

public class BundleSenderImpl implements BundleSender {

	public BundleSenderImpl(
		Sender sender,
		String exchange, String routingKey) {
		_sender = sender;
		_exchange = exchange;
		_routingKey = routingKey;
	}

	@Override
	public Publisher<Void> send(Publisher<byte[]> publisher) {
		return send(Flux.from(publisher));
	}

	@Override
	public Mono<Void> send(Flux<byte[]> publisher) {

		return _sender.send(publisher
			.map(bytes -> new OutboundMessage(_exchange, _routingKey, bytes)));

	}

	@Override
	public Mono<Void> send(Mono<byte[]> publisher) {
		return _sender.send(publisher
			.map(bytes -> new OutboundMessage(_exchange, _routingKey, bytes)));
	}

	private final Sender _sender;
	private final String _exchange;
	private final String _routingKey;

}
